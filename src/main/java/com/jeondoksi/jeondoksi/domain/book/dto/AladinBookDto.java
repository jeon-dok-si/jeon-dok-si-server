package com.jeondoksi.jeondoksi.domain.book.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AladinBookDto {
    private String title;
    private String author;
    private String cover;
    private String link;
    private String isbn;
    private String description;
    private String pubDate;
    private String categoryName;
    private Integer bestRank; // 베스트셀러 순위
}
