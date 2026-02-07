package com.team.ehr.controller;

import com.team.ehr.dto.ChatAnswerRequest;
import com.team.ehr.dto.ChatAnswerResponse;
import com.team.ehr.dto.ChatQuestionDto;
import com.team.ehr.service.ChatQuestionService;
import com.team.ehr.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ehr/chat")
public class ChatController {

    private final ChatQuestionService questionService;
    private final ChatService chatService;

    public ChatController(ChatQuestionService questionService, ChatService chatService) {
        this.questionService = questionService;
        this.chatService = chatService;
    }

    @GetMapping("/questions")
    @PreAuthorize("isAuthenticated()")
    public List<ChatQuestionDto> questions() {
        return questionService.getQuestions();
    }

    @PostMapping("/answer")
    @PreAuthorize("isAuthenticated()")
    public ChatAnswerResponse answer(@Valid @RequestBody ChatAnswerRequest request) {
        return chatService.answer(request);
    }
}
