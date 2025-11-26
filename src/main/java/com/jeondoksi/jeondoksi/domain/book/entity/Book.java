package com.jeondoksi.jeondoksi.domain.book.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "book")
public class Book extends BaseTimeEntity {

    @Id
    @Column(name = "isbn", length = 20)
    private String isbn; // PK

    @Column(nullable = false, length = 255)
    private String title;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(length = 500)
    private String thumbnail;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String keywords; // TF-IDF 분석을 위한 키워드 (쉼표로 구분)

    // --- 연관 관계 ---
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookAiSample> samples = new ArrayList<>();

    @Builder
    public Book(String isbn, String title, String author, String thumbnail, String description, String keywords) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.thumbnail = thumbnail;
        this.description = description;
        this.keywords = keywords;
    }

    public void updateKeywords(String keywords) {
        this.keywords = keywords;
    }
}