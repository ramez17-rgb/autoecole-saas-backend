package com.autoecole.saas.mock;

import com.autoecole.saas.question.Question;
import com.autoecole.saas.question.QuestionOption;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mock_test_answers")
public class MockTestAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mock_test_id", nullable = false)
    private MockTest mockTest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @ManyToOne(optional = false)
    @JoinColumn(name = "selected_option_id", nullable = false)
    private QuestionOption selectedOption;

    @Column(name = "is_correct")
    private Boolean isCorrect;

    public MockTestAnswer() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public MockTest getMockTest() {
        return mockTest;
    }

    public void setMockTest(MockTest mockTest) {
        this.mockTest = mockTest;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public QuestionOption getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(QuestionOption selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }
}
