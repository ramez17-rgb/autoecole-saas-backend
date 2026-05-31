package com.autoecole.saas.mock.dto;

import com.autoecole.saas.question.dto.QuestionResponse;
import java.util.List;
import java.util.UUID;

/**
 * Response returned when a student starts a mock test.
 * Contains the mock test ID (needed for submission) and the list of questions.
 */
public class StartMockTestResponse {

    private UUID mockTestId;
    private List<QuestionResponse> questions;

    public StartMockTestResponse() {
    }

    public StartMockTestResponse(UUID mockTestId, List<QuestionResponse> questions) {
        this.mockTestId = mockTestId;
        this.questions = questions;
    }

    public UUID getMockTestId() {
        return mockTestId;
    }

    public void setMockTestId(UUID mockTestId) {
        this.mockTestId = mockTestId;
    }

    public List<QuestionResponse> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionResponse> questions) {
        this.questions = questions;
    }
}
