package com.jeondoksi.jeondoksi.domain.gamification.controller;

import com.jeondoksi.jeondoksi.domain.gamification.dto.GachaResponse;
import com.jeondoksi.jeondoksi.domain.gamification.dto.InventoryResponse;
import com.jeondoksi.jeondoksi.domain.gamification.service.RewardService;
import com.jeondoksi.jeondoksi.global.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Gamification", description = "게임화 및 보상 API")
@RestController
@RequestMapping("/api/v1/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final RewardService rewardService;

    @Operation(summary = "아이템 뽑기 (100 XP 소모)")
    @PostMapping("/gacha")
    public ApiResponse<GachaResponse> drawItem(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(rewardService.drawItem(userId));
    }

    @Operation(summary = "내 인벤토리 조회")
    @GetMapping("/inventory")
    public ApiResponse<List<InventoryResponse>> getMyInventory(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = Long.parseLong(userDetails.getUsername());
        return ApiResponse.success(rewardService.getMyInventory(userId));
    }

    @Operation(summary = "아이템 장착")
    @PostMapping("/inventory/{invenId}/equip")
    public ApiResponse<Void> equipItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long invenId) {
        Long userId = Long.parseLong(userDetails.getUsername());
        rewardService.equipItem(userId, invenId);
        return ApiResponse.success(null);
    }
}
