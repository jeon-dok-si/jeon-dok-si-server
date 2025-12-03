package com.jeondoksi.jeondoksi.domain.boss.controller;

import com.jeondoksi.jeondoksi.domain.boss.dto.BossResponse;
import com.jeondoksi.jeondoksi.domain.boss.dto.CreateBossRequest;
import com.jeondoksi.jeondoksi.domain.boss.service.BossService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bosses")
@RequiredArgsConstructor
public class BossController {

    private final BossService bossService;

    @PostMapping
    public ResponseEntity<BossResponse> createBoss(@RequestBody CreateBossRequest request) {
        // TODO: Add Admin check
        BossResponse response = bossService.createBoss(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<List<BossResponse>> getActiveBosses() {
        List<BossResponse> response = bossService.getActiveBosses();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{bossId}")
    public ResponseEntity<BossResponse> getBossDetail(@PathVariable Long bossId) {
        BossResponse response = bossService.getBossDetail(bossId);
        return ResponseEntity.ok(response);
    }
}
