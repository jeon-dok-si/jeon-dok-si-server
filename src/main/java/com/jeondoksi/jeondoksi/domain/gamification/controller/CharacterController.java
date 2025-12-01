package com.jeondoksi.jeondoksi.domain.gamification.controller;

import com.jeondoksi.jeondoksi.domain.gamification.dto.CharacterResponse;
import com.jeondoksi.jeondoksi.domain.gamification.service.CharacterService;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Character", description = "캐릭터 및 가챠 API")
@RestController
@RequestMapping("/api/v1/characters")
@RequiredArgsConstructor
public class CharacterController {

    private final CharacterService characterService;
    private final UserRepository userRepository;

    @Operation(summary = "캐릭터 뽑기 (100 Point 소모)")
    @PostMapping("/draw")
    public ApiResponse<CharacterResponse> drawCharacter(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        if (user.getPoint() < 100) {
            throw new BusinessException(ErrorCode.NOT_ENOUGH_POINT);
        }
        user.usePoint(100);

        return ApiResponse.success(CharacterResponse.from(characterService.drawCharacter(user)));
    }

    @Operation(summary = "내 캐릭터 목록 조회")
    @GetMapping
    public ApiResponse<List<CharacterResponse>> getMyCharacters(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        List<CharacterResponse> responses = characterService.getMyCharacters(user).stream()
                .map(CharacterResponse::from)
                .collect(Collectors.toList());

        return ApiResponse.success(responses);
    }

    @Operation(summary = "캐릭터 경험치 추가 (테스트용)")
    @PostMapping("/{characterId}/exp")
    public ApiResponse<Void> addExp(
            @PathVariable Long characterId,
            @RequestParam int amount) {
        characterService.gainExp(characterId, amount);
        return ApiResponse.success(null);
    }

    @Operation(summary = "캐릭터 장착 (대표 캐릭터 설정)")
    @PostMapping("/{characterId}/equip")
    public ApiResponse<Void> equipCharacter(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long characterId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        characterService.equipCharacter(userId, characterId);
        return ApiResponse.success(null);
    }
}
