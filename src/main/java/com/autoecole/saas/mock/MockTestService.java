package com.autoecole.saas.mock;

import com.autoecole.saas.mock.dto.AnswerCorrectionDto;
import com.autoecole.saas.mock.dto.MockTestResultResponse;
import com.autoecole.saas.mock.dto.StartMockTestResponse;
import com.autoecole.saas.mock.dto.SubmitMockTestRequest;
import com.autoecole.saas.question.Question;
import com.autoecole.saas.question.QuestionOption;
import com.autoecole.saas.question.QuestionOptionRepository;
import com.autoecole.saas.question.QuestionRepository;
import com.autoecole.saas.question.dto.QuestionOptionDto;
import com.autoecole.saas.question.dto.QuestionResponse;
import com.autoecole.saas.student.Student;
import com.autoecole.saas.student.StudentRepository;
import com.autoecole.saas.tenant.TenantContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MockTestService {

    private final MockTestRepository mockTestRepository;
    private final QuestionRepository questionRepository;
    private final QuestionOptionRepository questionOptionRepository;
    private final StudentRepository studentRepository;

    public MockTestService(MockTestRepository mockTestRepository,
            QuestionRepository questionRepository,
            QuestionOptionRepository questionOptionRepository,
            StudentRepository studentRepository) {
        this.mockTestRepository = mockTestRepository;
        this.questionRepository = questionRepository;
        this.questionOptionRepository = questionOptionRepository;
        this.studentRepository = studentRepository;
    }

    @Transactional
    public StartMockTestResponse startMockTest(UUID studentId) {
        // Derive tenant from the student entity — no TenantContext needed for student-facing endpoint
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        UUID tenantId = student.getTenant().getId();

        // Fetch random questions from the student's own tenant
        List<Question> randomQuestions = questionRepository.findRandomQuestionsByTenant(tenantId, 40);

        MockTest mockTest = new MockTest();
        mockTest.setTenant(student.getTenant());
        mockTest.setStudent(student);
        mockTest.setTotalQuestions(randomQuestions.size());
        MockTest savedMockTest = mockTestRepository.save(mockTest);

        // Return test ID + questions without correct answer info (student-facing)
        List<QuestionResponse> questionResponses = randomQuestions.stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        return new StartMockTestResponse(savedMockTest.getId(), questionResponses);
    }

    @Transactional
    public MockTestResultResponse submitMockTest(UUID mockTestId, SubmitMockTestRequest request) {
        MockTest mockTest = mockTestRepository.findById(mockTestId)
                .orElseThrow(() -> new RuntimeException("Mock test not found"));

        // No tenant check needed: mock test ID is a secret UUID, only the student knows it

        if (mockTest.getScore() != null) {
            throw new RuntimeException("Mock test already submitted");
        }

        int score = 0;
        Map<UUID, UUID> submittedAnswers = request.getAnswers();
        List<AnswerCorrectionDto> corrections = new ArrayList<>();

        for (Map.Entry<UUID, UUID> entry : submittedAnswers.entrySet()) {
            UUID questionId = entry.getKey();
            UUID selectedOptionId = entry.getValue();

            Question question = questionRepository.findById(questionId)
                    .orElse(null);
            QuestionOption selectedOption = questionOptionRepository.findById(selectedOptionId)
                    .orElse(null);

            if (question != null && selectedOption != null) {
                // Verify the selected option belongs to this question
                if (!selectedOption.getQuestion().getId().equals(questionId)) {
                    throw new RuntimeException("Selected option does not belong to question " + questionId);
                }

                boolean isCorrect = Boolean.TRUE.equals(selectedOption.getIsCorrect());

                // Store the answer
                MockTestAnswer answerRecord = new MockTestAnswer();
                answerRecord.setQuestion(question);
                answerRecord.setSelectedOption(selectedOption);
                answerRecord.setIsCorrect(isCorrect);
                mockTest.addAnswer(answerRecord);

                if (isCorrect) {
                    score++;
                }

                // Build correction DTO
                AnswerCorrectionDto correction = new AnswerCorrectionDto();
                correction.setQuestionId(questionId);
                correction.setQuestionText(question.getQuestionText());
                correction.setCategory(question.getCategory());
                correction.setImageUrl(question.getImageUrl());
                correction.setSelectedOptionId(selectedOptionId);
                correction.setSelectedOptionText(selectedOption.getOptionText());
                correction.setIsCorrect(isCorrect);
                correction.setExplanation(question.getExplanation());

                // Find the correct option for this question
                QuestionOption correctOption = question.getOptions().stream()
                        .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                        .findFirst()
                        .orElse(null);
                if (correctOption != null) {
                    correction.setCorrectOptionId(correctOption.getId());
                    correction.setCorrectOptionText(correctOption.getOptionText());
                }

                corrections.add(correction);
            }
        }

        mockTest.setScore(score);
        boolean passed = score >= (mockTest.getTotalQuestions() * 0.75); // 30/40 is passing
        mockTest.setPassed(passed);

        MockTest savedMockTest = mockTestRepository.save(mockTest);

        return mapToResultResponse(savedMockTest, corrections);
    }

    /**
     * Retrieve a previously taken mock test with full correction details.
     */
    @Transactional(readOnly = true)
    public MockTestResultResponse getMockTestResult(UUID mockTestId) {
        MockTest mockTest = mockTestRepository.findById(mockTestId)
                .orElseThrow(() -> new RuntimeException("Mock test not found"));

        // No tenant check: mock test ID is UUID, accessible only if you know it
        List<AnswerCorrectionDto> corrections = mockTest.getAnswers().stream()
                .map(this::mapToCorrectionDto)
                .collect(Collectors.toList());

        return mapToResultResponse(mockTest, corrections);
    }

    /**
     * Get all mock test results for a student.
     */
    @Transactional(readOnly = true)
    public List<MockTestResultResponse> getStudentMockTests(UUID studentId) {
        // Derive tenant from the student entity
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        UUID tenantId = student.getTenant().getId();
        List<MockTest> tests = mockTestRepository.findByTenantIdAndStudentId(tenantId, studentId);
        return tests.stream()
                .map(test -> {
                    List<AnswerCorrectionDto> corrections = test.getAnswers().stream()
                            .map(this::mapToCorrectionDto)
                            .collect(Collectors.toList());
                    return mapToResultResponse(test, corrections);
                })
                .collect(Collectors.toList());
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        QuestionResponse response = new QuestionResponse();
        response.setId(question.getId());
        response.setCategory(question.getCategory());
        response.setQuestionText(question.getQuestionText());
        response.setImageUrl(question.getImageUrl());
        response.setDifficulty(question.getDifficulty());
        // Do NOT expose explanation or isCorrect to prevent cheating during the test

        List<QuestionOptionDto> optionDtos = question.getOptions().stream()
                .map(opt -> new QuestionOptionDto(opt.getId(), opt.getOptionText()))
                .collect(Collectors.toList());

        response.setOptions(optionDtos);
        return response;
    }

    private AnswerCorrectionDto mapToCorrectionDto(MockTestAnswer answer) {
        AnswerCorrectionDto dto = new AnswerCorrectionDto();
        Question question = answer.getQuestion();
        QuestionOption selectedOption = answer.getSelectedOption();

        dto.setQuestionId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setCategory(question.getCategory());
        dto.setImageUrl(question.getImageUrl());
        dto.setSelectedOptionId(selectedOption.getId());
        dto.setSelectedOptionText(selectedOption.getOptionText());
        dto.setIsCorrect(answer.getIsCorrect());
        dto.setExplanation(question.getExplanation());

        // Find the correct option
        QuestionOption correctOption = question.getOptions().stream()
                .filter(opt -> Boolean.TRUE.equals(opt.getIsCorrect()))
                .findFirst()
                .orElse(null);
        if (correctOption != null) {
            dto.setCorrectOptionId(correctOption.getId());
            dto.setCorrectOptionText(correctOption.getOptionText());
        }

        return dto;
    }

    private MockTestResultResponse mapToResultResponse(MockTest mockTest, List<AnswerCorrectionDto> corrections) {
        MockTestResultResponse response = new MockTestResultResponse();
        response.setId(mockTest.getId());
        response.setStudentId(mockTest.getStudent().getId());
        response.setScore(mockTest.getScore());
        response.setTotalQuestions(mockTest.getTotalQuestions());
        response.setPassed(mockTest.getPassed());
        response.setTakenAt(mockTest.getTakenAt());
        response.setCorrections(corrections);
        return response;
    }
}
