package com.jeondoksi.jeondoksi.domain.report.entity;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "report")
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_isbn", nullable = false)
    private Book book;

    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status; // PENDING, APPROVED, REJECTED

    // --- 분석 결과 ---
    @Column(name = "logic_score")
    private int logicScore;

    @Column(name = "emotion_score")
    private int emotionScore;

    @Column(name = "action_score")
    private int actionScore;

    @Column(name = "analysis_type")
    private String analysisType; // ANALYST, PHILOSOPHER, etc.

    @Lob
    @Column(columnDefinition = "TEXT")
    private String feedback;

    @Builder
    public Report(User user, Book book, String content) {
        this.user = user;
        this.book = book;
        this.content = content;
        this.status = ReportStatus.PENDING;
    }

    public void updateAnalysisResult(int logic, int emotion, int action, String type, String feedback) {
        this.logicScore = logic;
        this.emotionScore = emotion;
        this.actionScore = action;
        this.analysisType = type;
        this.feedback = feedback;
    }

    public void approve() {
        this.status = ReportStatus.APPROVED;
    }

    public void reject() {
        this.status = ReportStatus.REJECTED;
    }
}