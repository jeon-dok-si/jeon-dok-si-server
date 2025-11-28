package com.jeondoksi.jeondoksi.domain.report.dto;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReportDetailResponse {
    private Long reportId;
    private BookInfo book;
    private String userContent;
    private AnalysisResultInfo analysisResult;
    private LocalDateTime createdAt;

    public static ReportDetailResponse from(Report report) {
        return ReportDetailResponse.builder()
                .reportId(report.getReportId())
                .book(BookInfo.from(report.getBook()))
                .userContent(report.getContent())
                .analysisResult(AnalysisResultInfo.from(report))
                .createdAt(report.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class BookInfo {
        private String isbn;
        private String title;
        private String author;
        private String thumbnail;

        public static BookInfo from(Book book) {
            return BookInfo.builder()
                    .isbn(book.getIsbn())
                    .title(book.getTitle())
                    .author(book.getAuthor())
                    .thumbnail(book.getThumbnail())
                    .build();
        }
    }

    @Getter
    @Builder
    public static class AnalysisResultInfo {
        private String type;
        private String typeName;
        private Scores scores;
        private String feedback;

        public static AnalysisResultInfo from(Report report) {
            String type = report.getAnalysisType();
            return AnalysisResultInfo.builder()
                    .type(type)
                    .typeName(mapTypeName(type))
                    .scores(Scores.builder()
                            .logic(report.getLogicScore())
                            .emotion(report.getEmotionScore())
                            .action(report.getActionScore())
                            .build())
                    .feedback(report.getFeedback())
                    .build();
        }

        private static String mapTypeName(String type) {
            if (type == null)
                return "알 수 없음";
            switch (type) {
                case "PHILOSOPHER":
                    return "사색하는 철학자";
                case "ANALYST":
                    return "냉철한 분석가";
                case "EMPATH":
                    return "감성적인 공감러";
                case "ACTIVIST":
                    return "행동하는 실천가";
                case "READER":
                    return "성실한 독서가";
                default:
                    return "알 수 없음";
            }
        }
    }

    @Getter
    @Builder
    public static class Scores {
        private int logic;
        private int emotion;
        private int action;
    }
}
