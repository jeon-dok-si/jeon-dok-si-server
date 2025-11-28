package com.jeondoksi.jeondoksi.domain.quiz.controller;

import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizResponse;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizResultResponse;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizSubmitRequest;
import com.jeondoksi.jeondoksi.domain.quiz.service.QuizService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Quiz", description = "퀴즈 API")
@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizService quizService;

    @Operation(summary = "퀴즈 생성/조회", description = "책 ISBN으로 퀴즈를 조회하거나 없으면 생성합니다.")
    @GetMapping("/{isbn}")
    public ApiResponse<QuizResponse> getQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String isbn) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(quizService.getQuiz(isbn, userId));
    }

    @Operation(summary = "퀴즈 제출 및 채점")
    @PostMapping("/submit")
    public ApiResponse<QuizResultResponse> submitQuiz(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody QuizSubmitRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(quizService.submitQuiz(userId, request));
    }
}
