package com.autoecole.saas.question;

import com.autoecole.saas.question.dto.CreateQuestionRequest;
import com.autoecole.saas.question.dto.QuestionOptionDto;
import com.autoecole.saas.question.dto.QuestionResponse;
import com.autoecole.saas.tenant.Tenant;
import com.autoecole.saas.tenant.TenantContext;
import com.autoecole.saas.tenant.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final TenantRepository tenantRepository;

    public QuestionService(QuestionRepository questionRepository, TenantRepository tenantRepository) {
        this.questionRepository = questionRepository;
        this.tenantRepository = tenantRepository;
    }

    public QuestionResponse createQuestion(CreateQuestionRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        if (tenantId == null) {
            throw new RuntimeException("No tenant context found");
        }

        Tenant tenant = tenantRepository.findById(tenantId)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));

        Question question = new Question();
        question.setTenant(tenant);
        question.setCategory(request.getCategory());
        question.setQuestionText(request.getQuestionText());
        question.setCorrectAnswer(request.getCorrectAnswer());

        for (String optionText : request.getOptions()) {
            QuestionOption option = new QuestionOption();
            option.setOptionText(optionText);
            question.addOption(option);
        }

        Question savedQuestion = questionRepository.save(question);
        return mapToResponse(savedQuestion);
    }

    public List<QuestionResponse> getQuestionsByTenant() {
        UUID tenantId = TenantContext.getTenantId();
        return questionRepository.findByTenantId(tenantId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public void deleteQuestion(UUID questionId) {
        UUID tenantId = TenantContext.getTenantId();
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Question not found"));

        if (!question.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        questionRepository.delete(question);
    }

    private QuestionResponse mapToResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setCategory(question.getCategory());
        response.setQuestionText(question.getQuestionText());

        List<QuestionOptionDto> optionDtos = question.getOptions().stream()
                .map(opt -> new QuestionOptionDto(opt.getId(), opt.getOptionText()))
                .collect(Collectors.toList());

        response.setOptions(optionDtos);
        return response;
    }
}
