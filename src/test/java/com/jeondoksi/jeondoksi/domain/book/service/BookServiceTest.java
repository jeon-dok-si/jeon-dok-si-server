package com.jeondoksi.jeondoksi.domain.book.service;

import com.jeondoksi.jeondoksi.domain.book.client.NaverBookClient;
import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @InjectMocks
    private BookService bookService;

    @Mock
    private NaverBookClient naverBookClient;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private Komoran komoran;

    @Mock
    private KomoranResult komoranResult;

    @Test
    @DisplayName("도서 검색 및 저장 테스트")
    void searchBooks_success() {
        // given
        String query = "해리포터";
        BookSearchResponse bookDto = BookSearchResponse.builder()
                .isbn("1234567890")
                .title("해리포터와 마법사의 돌")
                .author("J.K. 롤링")
                .thumbnail("thumbnail.jpg")
                .description("해리포터 시리즈 1권")
                .build();

        given(naverBookClient.searchBooks(query)).willReturn(List.of(bookDto));
        given(bookRepository.existsById(bookDto.getIsbn())).willReturn(false);
        given(komoran.analyze(anyString())).willReturn(komoranResult);
        given(komoranResult.getNouns()).willReturn(List.of("해리포터", "마법"));

        // when
        List<BookSearchResponse> result = bookService.searchBooks(query);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo(bookDto.getTitle());

        // 저장이 호출되었는지 검증
        verify(bookRepository, times(1)).save(any());
    }
}
