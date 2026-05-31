package com.autoecole.saas.exam;

import com.autoecole.saas.exam.dto.ExamResponse;
import com.autoecole.saas.exam.dto.RegisterExamRequest;
import com.autoecole.saas.exam.dto.UpdateExamResultRequest;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;
    private final TenantRepository tenantRepository;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;

    public ExamService(ExamRepository examRepository,
            StudentRepository studentRepository,
            TenantRepository tenantRepository,
            org.springframework.context.ApplicationEventPublisher eventPublisher) {
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
        this.tenantRepository = tenantRepository;
        this.eventPublisher = eventPublisher;
    }

    public ExamResponse registerExam(RegisterExamRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Student does not belong to the current tenant");
        }

        Exam exam = new Exam();
        exam.setTenant(tenant);
        exam.setStudent(student);
        exam.setType(request.getType());
        exam.setDate(request.getDate());
        exam.setStatus("planned");

        Exam savedExam = examRepository.save(exam);
        
        String studentName = student.getUser() != null ? student.getUser().getFirstName() + " " + student.getUser().getLastName() : "Élève inconnu";
        eventPublisher.publishEvent(new com.autoecole.saas.audit.AuditLogEvent(this, tenantId, null, "Examen " + request.getType() + " planifié pour l'élève : " + studentName));
        
        return mapToResponse(savedExam);
    }

    public ExamResponse updateResult(UUID examId, UpdateExamResultRequest request) {
        UUID tenantId = TenantContext.getTenantId();

        Exam exam = examRepository.findById(examId)
                .orElseThrow(() -> new RuntimeException("Exam not found"));

        if (!exam.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized to modify this exam");
        }

        exam.setStatus("done");
        exam.setResult(request.getResult());
        exam.setScore(request.getScore());

        Exam updatedExam = examRepository.save(exam);
        return mapToResponse(updatedExam);
    }

    public List<ExamResponse> getExamsForStudent(UUID studentId) {
        UUID tenantId = TenantContext.getTenantId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized access to student's exams");
        }

        return examRepository.findByTenantIdAndStudentId(tenantId, studentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ExamResponse> getUpcomingExams() {
        UUID tenantId = TenantContext.getTenantId();
        return examRepository.findByTenantIdAndStatus(tenantId, "planned")
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private ExamResponse mapToResponse(Exam exam) {
        ExamResponse response = new ExamResponse();
        response.setId(exam.getId());
        response.setStudentId(exam.getStudent().getId());
        response.setType(exam.getType());
        response.setDate(exam.getDate());
        response.setStatus(exam.getStatus());
        response.setResult(exam.getResult());
        response.setScore(exam.getScore());
        return response;
    }
}
