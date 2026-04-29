package com.autoecole.saas.session;

import com.autoecole.saas.monitor.Monitor;
import com.autoecole.saas.monitor.MonitorRepository;
import com.autoecole.saas.session.dto.CreateSessionRequest;
import com.autoecole.saas.session.dto.SessionResponse;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final MonitorRepository monitorRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;

    public SessionService(SessionRepository sessionRepository,
            MonitorRepository monitorRepository,
            StudentRepository studentRepository,
            TenantRepository tenantRepository) {
        this.sessionRepository = sessionRepository;
        this.monitorRepository = monitorRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {
        UUID tenantId = getTenantId();
        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Monitor monitor = monitorRepository.findById(request.getMonitorId())
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        if (!monitor.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Monitor does not belong to your tenant");
        }

        List<Student> students = studentRepository.findAllById(request.getStudentIds());
        if (students.size() != request.getStudentIds().size()) {
            throw new RuntimeException("One or more students not found");
        }

        for (Student student : students) {
            if (!student.getTenant().getId().equals(tenantId)) {
                throw new RuntimeException("Student " + student.getId() + " does not belong to your tenant");
            }
        }

        // Validate Overlaps
        if (sessionRepository.hasOverlappingMonitorSession(monitor.getId(), request.getSessionDate(),
                request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("Monitor already has an overlapping session at this time");
        }

        if (sessionRepository.hasOverlappingStudentSession(request.getStudentIds(), request.getSessionDate(),
                request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("One or more students already have an overlapping session at this time");
        }

        Session session = new Session();
        session.setTenant(tenant);
        session.setMonitor(monitor);
        session.setSessionDate(request.getSessionDate());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setStudents(new ArrayList<>(students));
        // Status defaults to PLANNED

        Session savedSession = sessionRepository.save(session);
        return mapToResponse(savedSession);
    }

    @Transactional
    public SessionResponse addStudentToSession(UUID sessionId, UUID studentId) {
        Session session = getSessionForTenant(sessionId);

        if (session.getStatus() != SessionStatus.PLANNED) {
            throw new RuntimeException("Only PLANNED sessions can be modified");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(session.getTenant().getId())) {
            throw new RuntimeException("Student does not belong to your tenant");
        }

        if (session.getStudents().contains(student)) {
            throw new RuntimeException("Student is already in this session");
        }

        if (sessionRepository.hasOverlappingStudentSession(List.of(studentId), session.getSessionDate(),
                session.getStartTime(), session.getEndTime(), session.getId())) {
            throw new RuntimeException("Student already has an overlapping session at this time");
        }

        session.getStudents().add(student);
        return mapToResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse removeStudentFromSession(UUID sessionId, UUID studentId) {
        Session session = getSessionForTenant(sessionId);

        if (session.getStatus() != SessionStatus.PLANNED) {
            throw new RuntimeException("Only PLANNED sessions can be modified");
        }

        Student studentToRemove = session.getStudents().stream()
                .filter(s -> s.getId().equals(studentId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Student is not in this session"));

        session.getStudents().remove(studentToRemove);
        return mapToResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse markSessionCompleted(UUID sessionId) {
        Session session = getSessionForTenant(sessionId);

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new RuntimeException("A cancelled session cannot be marked as completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        return mapToResponse(sessionRepository.save(session));
    }

    @Transactional
    public SessionResponse cancelSession(UUID sessionId) {
        Session session = getSessionForTenant(sessionId);

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new RuntimeException("A completed session cannot be cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return mapToResponse(sessionRepository.save(session));
    }

    public List<SessionResponse> getFilteredSessions(UUID monitorId, UUID studentId, LocalDate fromDate,
            LocalDate toDate, SessionStatus status) {
        UUID tenantId = getTenantId();
        return sessionRepository.findFilteredSessions(tenantId, monitorId, studentId, fromDate, toDate, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getStudentSessions(UUID studentId) {
        // Student is authenticated — look up their sessions directly by studentId.
        // No tenant check needed: studentId comes from their own authenticated profile.
        studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        return sessionRepository.findByStudents_IdOrderBySessionDateDescStartTimeDesc(studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getMonitorSessions(UUID monitorId) {
        UUID tenantId = getTenantId();
        Monitor monitor = monitorRepository.findById(monitorId)
                .orElseThrow(() -> new RuntimeException("Monitor not found"));

        if (!monitor.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to monitor's sessions");
        }

        return sessionRepository.findByMonitor_IdOrderBySessionDateDescStartTimeDesc(monitorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Session getSessionForTenant(UUID sessionId) {
        UUID tenantId = getTenantId();
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Access denied: Session belongs to another tenant");
        }
        return session;
    }

    private UUID getTenantId() {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }
        return tenantId;
    }

    private SessionResponse mapToResponse(Session session) {
        SessionResponse response = new SessionResponse();
        response.setId(session.getId());
        response.setMonitorId(session.getMonitor().getId());
        
        String mFirstName = session.getMonitor().getUser() != null ? session.getMonitor().getUser().getFirstName() : session.getMonitor().getFirstName();
        String mLastName = session.getMonitor().getUser() != null ? session.getMonitor().getUser().getLastName() : session.getMonitor().getLastName();
        response.setMonitorName(((mFirstName != null ? mFirstName : "") + " " + (mLastName != null ? mLastName : "")).trim());
        
        response.setStudentIds(session.getStudents().stream().map(Student::getId).collect(Collectors.toList()));
        response.setStudentNames(session.getStudents().stream().map(s -> {
            String sFirstName = s.getUser() != null ? s.getUser().getFirstName() : s.getFirstName();
            String sLastName = s.getUser() != null ? s.getUser().getLastName() : s.getLastName();
            return ((sFirstName != null ? sFirstName : "") + " " + (sLastName != null ? sLastName : "")).trim();
        }).collect(Collectors.toList()));
        
        response.setSessionDate(session.getSessionDate());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setStatus(session.getStatus().name());
        response.setNotes(session.getNotes());
        return response;
    }
}
