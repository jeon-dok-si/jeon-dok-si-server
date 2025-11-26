package com.jeondoksi.jeondoksi.domain.book.repository;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.entity.BookAiSample;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookAiSampleRepository extends JpaRepository<BookAiSample, Long> {
    List<BookAiSample> findAllByBook(Book book);
}
