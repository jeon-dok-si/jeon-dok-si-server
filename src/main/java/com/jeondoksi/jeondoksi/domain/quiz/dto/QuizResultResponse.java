package com.jeondoksi.jeondoksi.domain.quiz.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class QuizResultResponse {
    private Long logId;
    private boolean isSolved;
    private int score;
    private int gainedExp;
}
