package com.jeondoksi.jeondoksi.domain.book.service;

import com.jeondoksi.jeondoksi.domain.book.client.NaverBookClient;
import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.cache.annotation.Cacheable;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BookService {

    private final NaverBookClient naverBookClient;
    private final BookRepository bookRepository;
    private final kr.co.shineware.nlp.komoran.core.Komoran komoran;

    @Transactional
    @Cacheable(value = "book_search", key = "#query", unless = "#result.isEmpty()")
    public List<BookSearchResponse> searchBooks(String query) {
        List<BookSearchResponse> books = naverBookClient.searchBooks(query);

        // 검색된 책 정보를 DB에 저장 (이미 존재하면 무시)
        for (BookSearchResponse dto : books) {
            if (!bookRepository.existsById(dto.getIsbn())) {
                String keywords = extractKeywords(dto.getDescription());

                Book book = Book.builder()
                        .isbn(dto.getIsbn())
                        .title(dto.getTitle())
                        .author(dto.getAuthor())
                        .thumbnail(dto.getThumbnail())
                        .description(dto.getDescription())
                        .keywords(keywords)
                        .build();
                bookRepository.save(book);
            }
        }

        return books;
    }

    private String extractKeywords(String description) {
        if (description == null || description.isEmpty()) {
            return "";
        }
        try {
            kr.co.shineware.nlp.komoran.model.KomoranResult result = komoran.analyze(description);
            List<String> nouns = result.getNouns();
            return String.join(",", nouns);
        } catch (Exception e) {
            return "";
        }
    }
}
