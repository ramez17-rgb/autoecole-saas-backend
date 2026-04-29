package com.autoecole.saas.mock;

import com.autoecole.saas.mock.dto.MockTestResultResponse;
import com.autoecole.saas.mock.dto.StartMockTestResponse;
import com.autoecole.saas.mock.dto.SubmitMockTestRequest;
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

    /**
     * Start a new mock test — returns 40 random questions (without correct answers).
     */
    @PostMapping("/start")
    public ResponseEntity<StartMockTestResponse> startMockTest(@RequestParam UUID studentId) {
        StartMockTestResponse response = mockTestService.startMockTest(studentId);
        return ResponseEntity.ok(response);
    }

    /**
     * Submit answers for a mock test — returns score, pass/fail, and full corrections.
     */
    @PostMapping("/{id}/submit")
    public ResponseEntity<MockTestResultResponse> submitMockTest(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitMockTestRequest request) {
        MockTestResultResponse response = mockTestService.submitMockTest(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieve a specific mock test result with detailed corrections.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MockTestResultResponse> getMockTestResult(@PathVariable UUID id) {
        MockTestResultResponse response = mockTestService.getMockTestResult(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all mock test results for a given student.
     */
    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<MockTestResultResponse>> getStudentMockTests(@PathVariable UUID studentId) {
        List<MockTestResultResponse> results = mockTestService.getStudentMockTests(studentId);
        return ResponseEntity.ok(results);
    }
}
