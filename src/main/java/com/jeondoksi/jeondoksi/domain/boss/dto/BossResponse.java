package com.jeondoksi.jeondoksi.domain.boss.dto;

import com.jeondoksi.jeondoksi.domain.boss.entity.Boss;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BossResponse {
    private Long id;
    private String name;
    private String description;
    private int level;
    private long maxHp;
    private long currentHp;
    private String imageUrl;
    private boolean isActive;
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public BossResponse(Boss boss) {
        this.id = boss.getId();
        this.name = boss.getName();
        this.description = boss.getDescription();
        this.level = boss.getLevel();
        this.maxHp = boss.getMaxHp();
        this.currentHp = boss.getCurrentHp();
        this.imageUrl = boss.getImageUrl();
        this.isActive = boss.isActive();
        this.startAt = boss.getStartAt();
        this.endAt = boss.getEndAt();
    }
}
