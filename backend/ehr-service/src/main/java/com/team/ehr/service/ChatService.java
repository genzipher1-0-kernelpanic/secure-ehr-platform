package com.team.ehr.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.team.ehr.dto.ChatAnswerRequest;
import com.team.ehr.dto.ChatAnswerResponse;
import com.team.ehr.dto.ChatQuestionDto;
import com.team.ehr.entity.EhrCategory;
import com.team.ehr.exception.BadRequestException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final ChatQuestionService questionService;
    private final AccessControlService accessControlService;
    private final EhrRecordService ehrRecordService;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;

    public ChatService(ChatQuestionService questionService,
                       AccessControlService accessControlService,
                       EhrRecordService ehrRecordService,
                       GeminiClient geminiClient,
                       ObjectMapper objectMapper) {
        this.questionService = questionService;
        this.accessControlService = accessControlService;
        this.ehrRecordService = ehrRecordService;
        this.geminiClient = geminiClient;
        this.objectMapper = objectMapper;
    }

    public ChatAnswerResponse answer(ChatAnswerRequest request) {
        ChatQuestionDto question = questionService.findById(request.getQuestionId())
                .orElseThrow(() -> new BadRequestException("Unknown question"));

        accessControlService.assertDoctorAssigned(request.getPatientId());

        Map<String, Object> safeContext = buildSafeContext(request.getPatientId());
        String prompt = buildPrompt(question.getText(), safeContext);
        String answer = geminiClient.generate(prompt);
        return new ChatAnswerResponse(question.getText(), answer);
    }

    private Map<String, Object> buildSafeContext(Long patientId) {
        Map<String, Object> context = new LinkedHashMap<>();

        JsonNode clinical = ehrRecordService.readPatient(patientId, EhrCategory.CLINICAL, null).getClinical().getData();
        JsonNode treatments = ehrRecordService.readPatient(patientId, EhrCategory.TREATMENTS, null).getTreatments().getData();

        context.put("allergies", readField(clinical, "allergies"));
        context.put("conditions", readField(clinical, "conditions"));
        context.put("vitals", readField(clinical, "vitals"));
        context.put("clinicalNotes", readField(clinical, "clinicalNotes"));
        context.put("medications", readField(treatments, "medications"));
        context.put("procedures", readField(treatments, "procedures"));
        context.put("carePlans", readField(treatments, "carePlans"));

        return context;
    }

    private Object readField(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return objectMapper.convertValue(value, Object.class);
    }

    private String buildPrompt(String question, Map<String, Object> safeContext) {
        ObjectNode payload = objectMapper.createObjectNode();
        payload.put("question", question);
        payload.set("patientContext", objectMapper.valueToTree(safeContext));
        return "You are a clinical assistant. Use only the provided patientContext and do not infer identity. "
                + "Answer briefly and safely.\n\n"
                + payload.toPrettyString();
    }
}
