package com.autoecole.saas.exam.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateExamResultRequest {
    @NotBlank(message = "Result status is required (success, fail, or absent)")
    private String result;

    private Integer score;

    public UpdateExamResultRequest() {
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}
