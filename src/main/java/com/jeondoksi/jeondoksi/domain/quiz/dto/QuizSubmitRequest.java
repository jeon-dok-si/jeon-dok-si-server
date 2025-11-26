package com.jeondoksi.jeondoksi.domain.quiz.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class QuizSubmitRequest {
    private Long quizId;
    private List<AnswerDto> answers;

    @Getter
    @NoArgsConstructor
    public static class AnswerDto {
        private Long questionId;
        private String answer;
    }
}
