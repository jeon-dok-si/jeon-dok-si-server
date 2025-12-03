package com.jeondoksi.jeondoksi.domain.guild.controller;

import com.jeondoksi.jeondoksi.domain.guild.dto.CreateGuildRequest;
import com.jeondoksi.jeondoksi.domain.guild.dto.GuildResponse;
import com.jeondoksi.jeondoksi.domain.guild.dto.JoinGuildRequest;
import com.jeondoksi.jeondoksi.domain.guild.service.GuildService;
import com.jeondoksi.jeondoksi.global.config.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/guilds")
@RequiredArgsConstructor
public class GuildController {

    private final GuildService guildService;

    @PostMapping
    public ResponseEntity<GuildResponse> createGuild(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody CreateGuildRequest request) {
        GuildResponse response = guildService.createGuild(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Page<GuildResponse>> getGuilds(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<GuildResponse> response = guildService.getGuilds(keyword, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{guildId}")
    public ResponseEntity<GuildResponse> getGuildDetail(@PathVariable Long guildId) {
        GuildResponse response = guildService.getGuildDetail(guildId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{guildId}/join")
    public ResponseEntity<Void> joinGuild(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long guildId,
            @RequestBody(required = false) JoinGuildRequest request) {
        String password = (request != null) ? request.getPassword() : null;
        guildService.joinGuild(userDetails.getUser(), guildId, password);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/join-by-code")
    public ResponseEntity<Void> joinGuildByCode(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody JoinGuildRequest request) {
        guildService.joinGuildByCode(userDetails.getUser(), request.getJoinCode());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{guildId}/leave")
    public ResponseEntity<Void> leaveGuild(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long guildId) {
        guildService.leaveGuild(userDetails.getUser(), guildId);
        return ResponseEntity.ok().build();
    }
}
