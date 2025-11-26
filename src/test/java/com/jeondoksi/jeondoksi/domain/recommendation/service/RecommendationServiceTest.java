package com.jeondoksi.jeondoksi.domain.recommendation.service;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.recommendation.dto.RecommendationResponse;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.entity.ReportStatus;
import com.jeondoksi.jeondoksi.domain.report.repository.ReportRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {

    @InjectMocks
    private RecommendationService recommendationService;

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Test
    @DisplayName("도서 추천 성공 - TF-IDF 키워드 유사도 기반")
    void recommendBooks_success() {
        // given
        Long userId = 1L;
        User user = User.builder().email("test@test.com").build();

        // 읽은 책 (키워드: 마법, 모험)
        Book readBook = Book.builder()
                .isbn("111")
                .title("Harry Potter")
                .author("Rowling")
                .description("Magic Adventure")
                .keywords("마법,모험")
                .build();

        Report report = Report.builder().user(user).book(readBook).content("Good").build();
        report.approve(); // 승인된 독후감만 반영

        // 추천 후보 책들
        // 1. 유사한 책 (키워드: 마법, 학교) -> '마법' 겹침
        Book similarBook = Book.builder()
                .isbn("222")
                .title("Magic School")
                .author("Author 2")
                .keywords("마법,학교")
                .build();

        // 2. 다른 책 (키워드: 과학, 우주) -> 겹침 없음
        Book differentBook = Book.builder()
                .isbn("333")
                .title("Science Space")
                .author("Author 3")
                .keywords("과학,우주")
                .build();

        given(userRepository.findById(userId)).willReturn(Optional.of(user));
        given(reportRepository.findAllByUser(user)).willReturn(List.of(report));
        given(bookRepository.findAllByIsbnNotIn(anyList())).willReturn(List.of(similarBook, differentBook));

        // when
        List<RecommendationResponse> responses = recommendationService.recommendBooks(userId);

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getTitle()).isEqualTo("Magic School"); // 유사한 책이 먼저 나와야 함
        assertThat(responses.get(1).getTitle()).isEqualTo("Science Space");
    }
}
