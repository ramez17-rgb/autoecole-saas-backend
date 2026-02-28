package com.autoecole.saas.mock;

import com.autoecole.saas.mock.dto.MockTestResultResponse;
import com.autoecole.saas.mock.dto.SubmitMockTestRequest;
import com.autoecole.saas.question.dto.QuestionResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/student/mock-tests")
public class MockTestController {

    private final MockTestService mockTestService;

    public MockTestController(MockTestService mockTestService) {
        this.mockTestService = mockTestService;
    }

    @PostMapping("/start")
    public ResponseEntity<List<QuestionResponse>> startMockTest(@RequestParam UUID studentId) {
        List<QuestionResponse> questions = mockTestService.startMockTest(studentId);
        return ResponseEntity.ok(questions);
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<MockTestResultResponse> submitMockTest(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitMockTestRequest request) {
        MockTestResultResponse response = mockTestService.submitMockTest(id, request);
        return ResponseEntity.ok(response);
    }
}
