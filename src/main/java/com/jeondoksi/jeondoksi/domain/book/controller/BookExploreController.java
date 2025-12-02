package com.jeondoksi.jeondoksi.domain.book.controller;

import com.jeondoksi.jeondoksi.domain.book.dto.AladinBookDto;
import com.jeondoksi.jeondoksi.domain.book.service.BookExploreService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookExploreController {

    private final BookExploreService bookExploreService;

    @GetMapping("/bestsellers")
    public ApiResponse<List<AladinBookDto>> getOverallBestsellers() {
        return ApiResponse.success(bookExploreService.getBestsellers(null));
    }

    @GetMapping("/bestsellers/{categoryId}")
    public ApiResponse<List<AladinBookDto>> getCategoryBestsellers(@PathVariable Integer categoryId) {
        return ApiResponse.success(bookExploreService.getBestsellers(categoryId));
    }
}
