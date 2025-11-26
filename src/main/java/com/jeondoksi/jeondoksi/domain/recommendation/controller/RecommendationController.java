package com.jeondoksi.jeondoksi.domain.recommendation.controller;

import com.jeondoksi.jeondoksi.domain.recommendation.dto.RecommendationResponse;
import com.jeondoksi.jeondoksi.domain.recommendation.service.RecommendationService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Recommendation", description = "AI 도서 추천 API")
@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @Operation(summary = "도서 추천 받기", description = "사용자의 독후감 기록을 바탕으로 AI가 도서를 추천합니다.")
    @GetMapping
    public ApiResponse<List<RecommendationResponse>> recommendBooks(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(recommendationService.recommendBooks(userId));
    }
}
