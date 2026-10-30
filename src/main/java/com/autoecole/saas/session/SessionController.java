package com.autoecole.saas.session;

import com.autoecole.saas.session.dto.CreateSessionRequest;
import com.autoecole.saas.session.dto.SessionResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // ============================================
    // ADMIN / TENANT ENDPOINTS
    // ============================================

    @PostMapping("/sessions")
    public ResponseEntity<SessionResponse> createSession(@Valid @RequestBody CreateSessionRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/sessions/{id}/students/{studentId}")
    public ResponseEntity<SessionResponse> addStudentToSession(
            @PathVariable UUID id,
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(sessionService.addStudentToSession(id, studentId));
    }

    @DeleteMapping("/sessions/{id}/students/{studentId}")
    public ResponseEntity<SessionResponse> removeStudentFromSession(
            @PathVariable UUID id,
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(sessionService.removeStudentFromSession(id, studentId));
    }

    @PutMapping("/sessions/{id}/complete")
    public ResponseEntity<SessionResponse> markCompleted(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.markSessionCompleted(id));
    }

    @PutMapping("/sessions/{id}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(@PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.cancelSession(id));
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionResponse>> getFilteredSessions(
            @RequestParam(required = false) UUID monitorId,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) SessionStatus status) {
        return ResponseEntity.ok(sessionService.getFilteredSessions(monitorId, studentId, fromDate, toDate, status));
    }

    // ============================================
    // STUDENT ENDPOINTS
    // ============================================

    @GetMapping("/student/sessions")
    public ResponseEntity<List<SessionResponse>> getStudentSessions(@RequestParam UUID studentId) {
        // Here we read studentId from RequestParam (simulating auth principal, or
        // actual specific API path like /api/student/{id}/sessions)
        return ResponseEntity.ok(sessionService.getStudentSessions(studentId));
    }

    // ============================================
    // MONITOR ENDPOINTS
    // ============================================

    @GetMapping("/monitor/sessions")
    public ResponseEntity<List<SessionResponse>> getMonitorSessions(@RequestParam UUID monitorId) {
        return ResponseEntity.ok(sessionService.getMonitorSessions(monitorId));
    }

    /**
     * Get detailed info about a student from a session in the monitor's planning.
     * The monitor must own the session (tenant check enforced in service).
     */
    @GetMapping("/monitor/sessions/{sessionId}/students/{studentId}/details")
    public ResponseEntity<java.util.Map<String, Object>> getStudentDetailsForMonitor(
            @PathVariable UUID sessionId,
            @PathVariable UUID studentId) {
        return ResponseEntity.ok(sessionService.getStudentDetailsForMonitor(sessionId, studentId));
    }
}
