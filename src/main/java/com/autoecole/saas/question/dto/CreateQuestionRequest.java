package com.autoecole.saas.question.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class CreateQuestionRequest {

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Question text cannot be blank")
    private String questionText;

    private String explanation;

    private String imageUrl;

    private String difficulty;

    @NotEmpty(message = "At least two options are required")
    @Valid
    private List<CreateQuestionOptionRequest> options;

    public CreateQuestionRequest() {
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

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public List<CreateQuestionOptionRequest> getOptions() {
        return options;
    }

    public void setOptions(List<CreateQuestionOptionRequest> options) {
        this.options = options;
    }
}
