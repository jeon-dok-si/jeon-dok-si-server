package com.jeondoksi.jeondoksi.domain.quiz.repository;

import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
}
