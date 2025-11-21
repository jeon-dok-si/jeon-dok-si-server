package com.jeondoksi.jeondoksi.domain.book.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

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

    // --- 책 성향 지수 ---
    @Column(name = "logic_score", nullable = false)
    @ColumnDefault("0")
    private int logicScore = 0;

    @Column(name = "emotion_score", nullable = false)
    @ColumnDefault("0")
    private int emotionScore = 0;

    @Column(name = "action_score", nullable = false)
    @ColumnDefault("0")
    private int actionScore = 0;

    // --- 연관 관계 ---
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookAiSample> samples = new ArrayList<>();
}