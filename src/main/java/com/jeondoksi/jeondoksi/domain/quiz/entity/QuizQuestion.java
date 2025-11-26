package com.jeondoksi.jeondoksi.domain.quiz.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "quiz_question")
public class QuizQuestion extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "question_id")
    private Long questionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "question_no", nullable = false)
    private int questionNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizType type;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String question;

    @Lob
    @Column(name = "options_json", columnDefinition = "TEXT")
    private String optionsJson;

    @Column(nullable = false, length = 255)
    private String answer;

    @Builder
    public QuizQuestion(Quiz quiz, int questionNo, QuizType type, String question, String optionsJson, String answer) {
        this.quiz = quiz;
        this.questionNo = questionNo;
        this.type = type;
        this.question = question;
        this.optionsJson = optionsJson;
        this.answer = answer;
    }
}