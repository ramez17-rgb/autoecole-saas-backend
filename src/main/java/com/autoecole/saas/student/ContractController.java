package com.autoecole.saas.student;

import com.autoecole.saas.student.dto.ContractRequest;
import com.autoecole.saas.student.dto.ContractResponse;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/contracts")
public class ContractController {

    private final ContractService contractService;

    @Autowired
    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping
    public ContractResponse createContract(@RequestBody ContractRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UUID userId = currentUser.getId();

        return contractService.createContract(tenantId, userId, request);
    }
}
