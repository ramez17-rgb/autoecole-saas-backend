package com.autoecole.saas.question.dto;

import java.util.List;
import java.util.UUID;

public class QuestionResponse {
    private UUID id;
    private String category;
    private String questionText;
    private List<QuestionOptionDto> options;

    // We do not return the correct answer here to prevent cheating

    public QuestionResponse() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
