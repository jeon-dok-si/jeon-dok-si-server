package com.jeondoksi.jeondoksi.domain.quiz.dto;

import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizQuestion;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizType;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class QuizResponse {
    private Long quizId;
    private String bookTitle;
    private List<QuestionDto> questions;

    @Getter
    @Builder
    public static class QuestionDto {
        private Long questionId;
        private int questionNo;
        private QuizType type;
        private String question;
        private String optionsJson; // 프론트에서 파싱해서 사용
    }

    public static QuizResponse from(Quiz quiz) {
        return QuizResponse.builder()
                .quizId(quiz.getQuizId())
                .bookTitle(quiz.getBook().getTitle())
                .questions(quiz.getQuestions().stream()
                        .map(q -> QuestionDto.builder()
                                .questionId(q.getQuestionId())
                                .questionNo(q.getQuestionNo())
                                .type(q.getType())
                                .question(q.getQuestion())
                                .optionsJson(q.getOptionsJson())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
