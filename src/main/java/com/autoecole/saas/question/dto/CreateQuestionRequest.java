package com.autoecole.saas.question.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateQuestionRequest {

    @NotBlank(message = "Category cannot be blank")
    private String category;

    @NotBlank(message = "Question text cannot be blank")
    private String questionText;

    @NotBlank(message = "Correct answer cannot be blank")
    private String correctAnswer;

    @NotEmpty(message = "At least two options are required")
    private List<String> options;

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

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }
}
