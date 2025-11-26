package com.jeondoksi.jeondoksi.domain.quiz.repository;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByBook(Book book);
}
