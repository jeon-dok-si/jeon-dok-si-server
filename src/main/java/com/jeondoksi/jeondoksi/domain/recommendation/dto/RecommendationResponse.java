package com.jeondoksi.jeondoksi.domain.recommendation.dto;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RecommendationResponse {
    private String isbn;
    private String title;
    private String author;
    private String thumbnail;
    private String description;
    private String reason;

    public static RecommendationResponse of(Book book, String reason) {
        return RecommendationResponse.builder()
                .isbn(book.getIsbn())
                .title(book.getTitle())
                .author(book.getAuthor())
                .thumbnail(book.getThumbnail())
                .description(book.getDescription())
                .reason(reason)
                .build();
    }
}
