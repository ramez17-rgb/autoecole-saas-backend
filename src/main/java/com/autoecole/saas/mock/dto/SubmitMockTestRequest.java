package com.autoecole.saas.mock.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;
import java.util.UUID;

public class SubmitMockTestRequest {

    // Map of Question UUID -> Selected Option UUID
    @NotNull(message = "Answers map is required")
    private Map<UUID, UUID> answers;

    public SubmitMockTestRequest() {
    }

    public Map<UUID, UUID> getAnswers() {
        return answers;
    }

    public void setAnswers(Map<UUID, UUID> answers) {
        this.answers = answers;
    }
}
