package com.jeondoksi.jeondoksi.domain.book.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchResponse {
    private String isbn;
    private String title;
    private String author;
    private String thumbnail;
    private String description;
}
