package com.jeondoksi.jeondoksi.domain.report.dto;

import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.entity.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportResponse {
    private Long reportId;
    private String bookTitle;
    private String content;
    private ReportStatus status;
    private AnalysisResult analysis;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class AnalysisResult {
        private int logicScore;
        private int emotionScore;
        private int actionScore;
        private String type;
    }

    private String bookThumbnail;

    public static ReportResponse from(Report report) {
        return ReportResponse.builder()
                .reportId(report.getReportId())
                .bookTitle(report.getBook().getTitle())
                .bookThumbnail(report.getBook().getThumbnail())
                .content(report.getContent())
                .status(report.getStatus())
                .analysis(AnalysisResult.builder()
                        .logicScore(report.getLogicScore())
                        .emotionScore(report.getEmotionScore())
                        .actionScore(report.getActionScore())
                        .type(report.getAnalysisType())
                        .build())
                .createdAt(report.getCreatedAt())
                .build();
    }
}
