package com.autoecole.saas.student;

import com.autoecole.saas.audit.AuditService;
import com.autoecole.saas.student.dto.ContractRequest;
import com.autoecole.saas.student.dto.ContractResponse;
import com.autoecole.saas.tenant.TenantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ContractService {

    private final StudentContractRepository contractRepository;
    private final StudentRepository studentRepository;
    private final AuditService auditService;

    @Autowired
    public ContractService(StudentContractRepository contractRepository,
            StudentRepository studentRepository,
            AuditService auditService) {
        this.contractRepository = contractRepository;
        this.studentRepository = studentRepository;
        this.auditService = auditService;
    }

    @Transactional
    public ContractResponse createContract(UUID tenantId, UUID userId, ContractRequest request) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Student does not belong to the tenant");
        }

        StudentContract newContract = new StudentContract();
        newContract.setTenant(student.getTenant());
        newContract.setStudent(student);
        newContract.setTotalPrice(request.getTotalPrice());

        StudentContract savedContract = contractRepository.save(newContract);

        auditService.log(tenantId, userId, "CONTRACT_CREATED");

        return new ContractResponse(
                savedContract.getId(),
                student.getId(),
                savedContract.getTotalPrice(),
                savedContract.getCreatedAt());
    }
}
