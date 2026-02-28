package com.autoecole.saas.question.dto;

import java.util.UUID;

public class QuestionOptionDto {
    private UUID id;
    private String optionText;

    public QuestionOptionDto() {
    }

    public QuestionOptionDto(UUID id, String optionText) {
        this.id = id;
        this.optionText = optionText;
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
}
