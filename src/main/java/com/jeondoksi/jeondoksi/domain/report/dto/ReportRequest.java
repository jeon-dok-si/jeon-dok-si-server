package com.jeondoksi.jeondoksi.domain.report.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ReportRequest {

    @NotBlank(message = "ISBN은 필수입니다.")
    private String isbn;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;
}
