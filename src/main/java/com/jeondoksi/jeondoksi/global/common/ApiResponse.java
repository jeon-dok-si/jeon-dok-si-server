package com.jeondoksi.jeondoksi.global.common;

import lombok.Getter;

/**
 * 공통 API 응답 포맷
 * 
 * @param <T> 데이터 타입
 */
@Getter
public class ApiResponse<T> {
    private final boolean success;
    private final T data;
    private final String message;
    private final String errorCode;

    private ApiResponse(boolean success, T data, String message, String errorCode) {
        this.success = success;
        this.data = data;
        this.message = message;
        this.errorCode = errorCode;
    }

    // 성공 응답 (데이터 있음)
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    // 성공 응답 (데이터 없음)
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(true, null, null, null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> failure(String errorCode, String message) {
        return new ApiResponse<>(false, null, message, errorCode);
    }
}
