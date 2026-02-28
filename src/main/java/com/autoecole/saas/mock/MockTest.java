package com.autoecole.saas.mock;

import com.autoecole.saas.student.Student;
import com.autoecole.saas.tenant.Tenant;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mock_tests")
public class MockTest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    private Integer score;

    @Column(name = "total_questions", nullable = false)
    private Integer totalQuestions = 40;

    private Boolean passed;

    @Column(name = "taken_at", nullable = false, updatable = false)
    private LocalDateTime takenAt = LocalDateTime.now();

    @OneToMany(mappedBy = "mockTest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MockTestAnswer> answers = new ArrayList<>();

    public MockTest() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Tenant getTenant() {
        return tenant;
    }

    public void setTenant(Tenant tenant) {
        this.tenant = tenant;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Boolean getPassed() {
        return passed;
    }

    public void setPassed(Boolean passed) {
        this.passed = passed;
    }

    public LocalDateTime getTakenAt() {
        return takenAt;
    }

    public void setTakenAt(LocalDateTime takenAt) {
        this.takenAt = takenAt;
    }

    public List<MockTestAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<MockTestAnswer> answers) {
        this.answers.clear();
        if (answers != null) {
            this.answers.addAll(answers);
        }
    }

    public void addAnswer(MockTestAnswer answer) {
        answers.add(answer);
        answer.setMockTest(this);
    }
}
