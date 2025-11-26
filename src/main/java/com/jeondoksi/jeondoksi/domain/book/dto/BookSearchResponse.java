package com.jeondoksi.jeondoksi.domain.book.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BookSearchResponse {
    private String isbn;
    private String title;
    private String author;
    private String thumbnail;
    private String description;
}
