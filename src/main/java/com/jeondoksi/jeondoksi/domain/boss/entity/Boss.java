package com.jeondoksi.jeondoksi.domain.boss.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "boss")
public class Boss extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boss_id")
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @ColumnDefault("1")
    private int level;

    @Column(name = "max_hp", nullable = false)
    private long maxHp;

    @Column(name = "current_hp", nullable = false)
    private long currentHp;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "is_active", nullable = false)
    @ColumnDefault("true")
    private boolean isActive;

    @Column(name = "start_at")
    private LocalDateTime startAt;

    @Column(name = "end_at")
    private LocalDateTime endAt;

    @Version
    private Long version;

    @Builder
    public Boss(String name, String description, int level, long maxHp, String imageUrl, LocalDateTime startAt,
            LocalDateTime endAt) {
        this.name = name;
        this.description = description;
        this.level = level;
        this.maxHp = maxHp;
        this.currentHp = maxHp;
        this.imageUrl = imageUrl;
        this.isActive = true;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void takeDamage(long damage) {
        this.currentHp -= damage;
        if (this.currentHp <= 0) {
            this.currentHp = 0;
            this.isActive = false;
        }
    }
}
