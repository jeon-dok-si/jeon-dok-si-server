package com.jeondoksi.jeondoksi.domain.boss.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class CreateBossRequest {
    private String name;
    private String description;
    private int level;
    private long maxHp;
    private String imageUrl;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
}
