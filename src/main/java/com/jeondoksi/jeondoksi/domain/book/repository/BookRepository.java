package com.jeondoksi.jeondoksi.domain.book.repository;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {
    List<Book> findAllByIsbnNotIn(List<String> isbns);
}
