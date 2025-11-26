package com.jeondoksi.jeondoksi.global.error;

import lombok.Getter;

/**
 * 비즈니스 로직 예외 기본 클래스
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
