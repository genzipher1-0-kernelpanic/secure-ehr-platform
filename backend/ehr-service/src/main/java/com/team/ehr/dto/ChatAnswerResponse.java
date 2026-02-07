package com.team.ehr.dto;

public class ChatAnswerResponse {

    private String question;
    private String answer;

    public ChatAnswerResponse() {
    }

    public ChatAnswerResponse(String question, String answer) {
        this.question = question;
        this.answer = answer;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}
