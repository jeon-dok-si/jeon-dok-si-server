package com.jeondoksi.jeondoksi.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * 전역 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "C003", "인증되지 않은 사용자입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "C004", "권한이 없습니다."),

    // Book
    BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "B001", "책을 찾을 수 없습니다."),

    // User
    EMAIL_DUPLICATION(HttpStatus.BAD_REQUEST, "U001", "이미 존재하는 이메일입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U002", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "U003", "비밀번호가 일치하지 않습니다."),

    // Report
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "독후감을 찾을 수 없습니다."),
    AI_GENERATED_CONTENT_DETECTED(HttpStatus.BAD_REQUEST, "R002", "AI가 작성한 것으로 의심되는 내용이 감지되었습니다."),
    CONTENT_TOO_SHORT(HttpStatus.BAD_REQUEST, "R003", "내용이 너무 짧습니다. 50자 이상 작성해주세요."),
    CONTENT_REPETITIVE(HttpStatus.BAD_REQUEST, "R004", "반복되는 문자가 너무 많습니다."),
    CONTENT_PLAGIARISM(HttpStatus.BAD_REQUEST, "R005", "책 소개글과 너무 유사합니다. 본인의 생각을 작성해주세요."),
    CONTENT_IRRELEVANT(HttpStatus.BAD_REQUEST, "R006", "책 내용과 관련이 없어 보입니다. 올바른 독후감을 작성해주세요."),
    CONTENT_SELF_PLAGIARISM(HttpStatus.BAD_REQUEST, "R007", "이전에 작성한 독후감과 너무 유사합니다."),

    // Quiz
    QUIZ_NOT_FOUND(HttpStatus.NOT_FOUND, "Q001", "퀴즈를 찾을 수 없습니다."),

    // Gamification
    NOT_ENOUGH_XP(HttpStatus.BAD_REQUEST, "G001", "경험치가 부족합니다."),
    NOT_ENOUGH_POINT(HttpStatus.BAD_REQUEST, "G003", "포인트가 부족합니다."),
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "G002", "아이템을 찾을 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
