package com.autoecole.saas.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateQuestionOptionRequest {

    @NotBlank(message = "Option text cannot be blank")
    private String optionText;

    @NotNull(message = "isCorrect must be specified")
    private Boolean isCorrect;

    public CreateQuestionOptionRequest() {
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
