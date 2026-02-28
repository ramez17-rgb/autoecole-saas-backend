package com.autoecole.saas.exam;

import com.autoecole.saas.exam.dto.ExamResponse;
import com.autoecole.saas.exam.dto.RegisterExamRequest;
import com.autoecole.saas.exam.dto.UpdateExamResultRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/exams")
public class ExamController {

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @PostMapping("/register")
    public ResponseEntity<ExamResponse> registerExam(@Valid @RequestBody RegisterExamRequest request) {
        ExamResponse response = examService.registerExam(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/result")
    public ResponseEntity<ExamResponse> updateResult(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateExamResultRequest request) {
        ExamResponse response = examService.updateResult(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<ExamResponse>> getStudentExams(@PathVariable UUID studentId) {
        List<ExamResponse> response = examService.getExamsForStudent(studentId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<ExamResponse>> getUpcomingExams() {
        List<ExamResponse> response = examService.getUpcomingExams();
        return ResponseEntity.ok(response);
    }
}
