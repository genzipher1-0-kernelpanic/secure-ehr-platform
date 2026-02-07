package com.team.ehr.service;

import com.team.ehr.config.ChatQuestionProperties;
import com.team.ehr.dto.ChatQuestionDto;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class ChatQuestionService {

    private final ChatQuestionProperties properties;

    public ChatQuestionService(ChatQuestionProperties properties) {
        this.properties = properties;
    }

    public List<ChatQuestionDto> getQuestions() {
        return properties.getQuestions().stream()
                .map(q -> new ChatQuestionDto(q.getId(), q.getText()))
                .toList();
    }

    public Optional<ChatQuestionDto> findById(String id) {
        return properties.getQuestions().stream()
                .filter(q -> q.getId().equals(id))
                .findFirst()
                .map(q -> new ChatQuestionDto(q.getId(), q.getText()));
    }
}
