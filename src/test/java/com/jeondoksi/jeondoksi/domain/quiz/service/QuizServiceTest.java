package com.jeondoksi.jeondoksi.domain.quiz.service;

import com.google.gson.Gson;
import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.quiz.client.OpenAiClient;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizResponse;
import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import com.jeondoksi.jeondoksi.domain.quiz.repository.QuizRepository;
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

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @InjectMocks
    private QuizService quizService;

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private OpenAiClient openAiClient;

    @Mock
    private Gson gson;

    @Test
    @DisplayName("퀴즈 조회 성공 - 이미 존재하는 경우")
    void getQuiz_existing() {
        // given
        String isbn = "1234567890";
        Book book = Book.builder().isbn(isbn).title("Test Book").build();
        Quiz quiz = Quiz.builder().book(book).build();
        ReflectionTestUtils.setField(quiz, "quizId", 1L);

        given(bookRepository.findById(isbn)).willReturn(Optional.of(book));
        given(quizRepository.findByBook(book)).willReturn(Optional.of(quiz));

        // when
        QuizResponse response = quizService.getQuiz(isbn);

        // then
        assertThat(response.getQuizId()).isEqualTo(1L);
        assertThat(response.getBookTitle()).isEqualTo("Test Book");
    }
}
