package com.jeondoksi.jeondoksi.domain.report.controller;

import com.jeondoksi.jeondoksi.domain.report.dto.ReportRequest;
import com.jeondoksi.jeondoksi.domain.report.dto.ReportResponse;
import com.jeondoksi.jeondoksi.domain.report.service.ReportService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Report", description = "독후감 API")
@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "독후감 제출")
    @PostMapping
    public ApiResponse<Long> submitReport(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ReportRequest request) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(reportService.submitReport(userId, request));
    }

    @Operation(summary = "내 독후감 목록 조회")
    @GetMapping("/me")
    public ApiResponse<List<ReportResponse>> getMyReports(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(reportService.getMyReports(userId));
    }
}
