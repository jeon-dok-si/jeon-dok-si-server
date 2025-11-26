package com.jeondoksi.jeondoksi.domain.book.controller;

import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.domain.book.service.BookService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Book", description = "도서 API")
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @Operation(summary = "도서 검색")
    @GetMapping("/search")
    public ApiResponse<List<BookSearchResponse>> searchBooks(@RequestParam String query) {
        return ApiResponse.success(bookService.searchBooks(query));
    }
}
