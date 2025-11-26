package com.jeondoksi.jeondoksi.domain.report.service;

import com.jeondoksi.jeondoksi.core.nlp.NlpAnalyzer;
import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.report.dto.ReportRequest;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.repository.ReportRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @InjectMocks
    private ReportService reportService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private com.jeondoksi.jeondoksi.domain.book.repository.BookAiSampleRepository bookAiSampleRepository;

    @Mock
    private NlpAnalyzer nlpAnalyzer;

    @Test
    @DisplayName("독후감 제출 성공")
    void submitReport_success() {
        // given
        Long userId = 1L;
        ReportRequest request = new ReportRequest();
        ReflectionTestUtils.setField(request, "isbn", "1234567890");
        ReflectionTestUtils.setField(request, "content", "정말 감동적인 책이었다.");

        User user = User.builder().email("test@test.com").build();
        Book book = Book.builder().isbn("1234567890").title("Test Book").build();

        NlpAnalyzer.AnalysisResult analysisResult = NlpAnalyzer.AnalysisResult.builder()
                .logicScore(50)
                .emotionScore(80)
                .actionScore(30)
                .type("EMPATH")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bookRepository.findById(request.getIsbn())).willReturn(Optional.of(book));
        given(bookAiSampleRepository.findAllByBook(book)).willReturn(java.util.Collections.emptyList());
        given(nlpAnalyzer.analyze(request.getContent())).willReturn(analysisResult);

        Report report = Report.builder()
                .user(user)
                .book(book)
                .content(request.getContent())
                .build();
        ReflectionTestUtils.setField(report, "reportId", 100L);

        given(reportRepository.save(any())).willReturn(report);

        // when
        Long reportId = reportService.submitReport(userId, request);

        // then
        assertThat(reportId).isEqualTo(100L);
        verify(nlpAnalyzer).analyze(request.getContent());
        verify(reportRepository).save(any());
    }

    @Test
    @DisplayName("독후감 제출 실패 - AI 작성 의심")
    void submitReport_fail_ai_detected() {
        // given
        Long userId = 1L;
        ReportRequest request = new ReportRequest();
        ReflectionTestUtils.setField(request, "isbn", "1234567890");
        ReflectionTestUtils.setField(request, "content", "AI가 쓴 것 같은 내용");

        User user = User.builder().email("test@test.com").build();
        Book book = Book.builder().isbn("1234567890").title("Test Book").build();

        com.jeondoksi.jeondoksi.domain.book.entity.BookAiSample sample = new com.jeondoksi.jeondoksi.domain.book.entity.BookAiSample();
        ReflectionTestUtils.setField(sample, "content", "AI가 쓴 것 같은 내용");

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(bookRepository.findById(request.getIsbn())).willReturn(Optional.of(book));
        given(bookAiSampleRepository.findAllByBook(book)).willReturn(java.util.List.of(sample));
        given(nlpAnalyzer.calculateSimilarity(any(), any())).willReturn(0.9);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> reportService.submitReport(userId, request))
                .isInstanceOf(com.jeondoksi.jeondoksi.global.error.BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode",
                        com.jeondoksi.jeondoksi.global.error.ErrorCode.AI_GENERATED_CONTENT_DETECTED);
    }
}
