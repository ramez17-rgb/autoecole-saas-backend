package com.autoecole.saas.mock.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public class SubmitMockTestRequest {

    // Map of Question UUID -> Selected Option String
    @NotNull(message = "Answers map is required")
    private Map<UUID, String> answers;

    public SubmitMockTestRequest() {
    }

    public Map<UUID, String> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<UUID, String> answers) {
        this.answers = answers;
    }
}
