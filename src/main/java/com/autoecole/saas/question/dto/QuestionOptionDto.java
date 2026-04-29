package com.autoecole.saas.question.dto;

import java.util.UUID;

public class QuestionOptionDto {
    private UUID id;
    private String optionText;
    private Boolean isCorrect;

    public QuestionOptionDto() {
    }

    public QuestionOptionDto(UUID id, String optionText) {
        this.id = id;
        this.optionText = optionText;
    }

    public QuestionOptionDto(UUID id, String optionText, Boolean isCorrect) {
        this.id = id;
        this.optionText = optionText;
        this.isCorrect = isCorrect;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getOptionText() {
        return optionText;
    }

    public void setOptionText(String optionText) {
        this.optionText = optionText;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean isCorrect) {
        this.isCorrect = isCorrect;
    }
}
