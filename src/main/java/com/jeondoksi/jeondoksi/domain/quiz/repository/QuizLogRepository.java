package com.jeondoksi.jeondoksi.domain.quiz.repository;

import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizLog;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizLogRepository extends JpaRepository<QuizLog, Long> {
    boolean existsByUserAndQuizAndIsSolvedTrue(User user, Quiz quiz);
}
