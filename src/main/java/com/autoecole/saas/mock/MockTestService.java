package com.autoecole.saas.mock;

import com.autoecole.saas.mock.dto.MockTestResultResponse;
import com.autoecole.saas.mock.dto.SubmitMockTestRequest;
import com.autoecole.saas.question.Question;
import com.autoecole.saas.question.QuestionRepository;
import com.autoecole.saas.question.dto.QuestionResponse;
import com.autoecole.saas.question.dto.QuestionOptionDto;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MockTestService {

    private final MockTestRepository mockTestRepository;
    private final QuestionRepository questionRepository;
    private final StudentRepository studentRepository;

    public MockTestService(MockTestRepository mockTestRepository,
            QuestionRepository questionRepository,
            StudentRepository studentRepository) {
        this.mockTestRepository = mockTestRepository;
        this.questionRepository = questionRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public List<QuestionResponse> startMockTest(UUID studentId) {
        UUID tenantId = TenantContext.getTenantId();

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!student.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        MockTest mockTest = new MockTest();
        mockTest.setTenant(student.getTenant());
        mockTest.setStudent(student);
        mockTest.setTotalQuestions(40);
        // Saved locally without score or passed.
        // Answers will be submitted later.
        mockTestRepository.save(mockTest);

        // Fetch 40 random questions
        List<Question> randomQuestions = questionRepository.findRandomQuestionsByTenant(tenantId, 40);

        return randomQuestions.stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MockTestResultResponse submitMockTest(UUID mockTestId, SubmitMockTestRequest request) {
        UUID tenantId = TenantContext.getTenantId();
        MockTest mockTest = mockTestRepository.findById(mockTestId)
                .orElseThrow(() -> new RuntimeException("Mock test not found"));

        if (!mockTest.getTenant().getId().equals(tenantId)) {
            throw new RuntimeException("Unauthorized");
        }

        if (mockTest.getScore() != null) {
            throw new RuntimeException("Mock test already submitted");
        }

        int score = 0;
        Map<UUID, String> submittedAnswers = request.getAnswers();

        // Calculate score
        for (Map.Entry<UUID, String> entry : submittedAnswers.entrySet()) {
            UUID questionId = entry.getKey();
            String selectedAnswer = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElse(null);

            if (question != null) {
                MockTestAnswer answerRecord = new MockTestAnswer();
                answerRecord.setQuestion(question);
                answerRecord.setSelectedAnswer(selectedAnswer);

                boolean isCorrect = question.getCorrectAnswer().equalsIgnoreCase(selectedAnswer);
                answerRecord.setIsCorrect(isCorrect);

                if (isCorrect) {
                    score++;
                }

                mockTest.addAnswer(answerRecord);
            }
        }

        mockTest.setScore(score);
        boolean passed = score >= (mockTest.getTotalQuestions() * 0.75); // Let's say 30/40 is passing
        mockTest.setPassed(passed);

        MockTest savedMockTest = mockTestRepository.save(mockTest);

        return mapToResultResponse(savedMockTest);
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
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

    private MockTestResultResponse mapToResultResponse(MockTest mockTest) {
        MockTestResultResponse response = new MockTestResultResponse();
        response.setId(mockTest.getId());
        response.setStudentId(mockTest.getStudent().getId());
        response.setScore(mockTest.getScore());
        response.setTotalQuestions(mockTest.getTotalQuestions());
        response.setPassed(mockTest.getPassed());
        response.setTakenAt(mockTest.getTakenAt());
        return response;
    }
}
