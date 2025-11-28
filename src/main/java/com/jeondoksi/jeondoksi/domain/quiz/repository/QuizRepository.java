package com.jeondoksi.jeondoksi.domain.quiz.repository;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Optional<Quiz> findByBook(Book book);

    Optional<Quiz> findByBookAndUser(Book book, User user);
}
