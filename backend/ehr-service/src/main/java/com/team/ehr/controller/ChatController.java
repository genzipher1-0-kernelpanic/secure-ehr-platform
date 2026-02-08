package com.team.ehr.controller;

import com.team.ehr.dto.ChatAnswerRequest;
import com.team.ehr.dto.ChatAnswerResponse;
import com.team.ehr.dto.ChatQuestionDto;
import com.team.ehr.service.ChatQuestionService;
import com.team.ehr.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ehr/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final ChatQuestionService questionService;
    private final ChatService chatService;

    public ChatController(ChatQuestionService questionService, ChatService chatService) {
        this.questionService = questionService;
        this.chatService = chatService;
    }

    @GetMapping("/questions")
    public List<ChatQuestionDto> questions() {
        log.info("Chat questions");
        return questionService.getQuestions();
    }

    @PostMapping("/answer")
    public ChatAnswerResponse answer(@Valid @RequestBody ChatAnswerRequest request) {
        log.info("Chat answer patientId={} questionId={}", request.getPatientId(), request.getQuestionId());
        return chatService.answer(request);
    }
}
