package com.jeondoksi.jeondoksi.domain.user.entity;

import com.jeondoksi.jeondoksi.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Getter
@DynamicUpdate
@NoArgsConstructor
@Table(name = "users") // MySQL 예약어 충돌 방지
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    // --- 게임 요소 ---
    @Column(nullable = false)
    @ColumnDefault("1")
    private int level = 1;

    @Column(name = "current_xp", nullable = false)
    @ColumnDefault("0")
    private int currentXp = 0;

    // --- 성향 점수 (Vector) ---
    @Column(name = "logic_stat", nullable = false)
    @ColumnDefault("0")
    private int logicStat = 0;

    @Column(name = "emotion_stat", nullable = false)
    @ColumnDefault("0")
    private int emotionStat = 0;

    @Column(name = "action_stat", nullable = false)
    @ColumnDefault("0")
    private int actionStat = 0;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.level = 1;
        this.currentXp = 0;
        this.logicStat = 0;
        this.emotionStat = 0;
        this.actionStat = 0;
    }

    // 경험치 획득 및 레벨업 로직 (문서 기반)
    public void gainExp(int exp) {
        this.currentXp += exp;
        // 레벨업 로직 (예: 100 * level 필요)
        int requiredXp = this.level * 100;
        while (this.currentXp >= requiredXp) {
            this.currentXp -= requiredXp;
            this.level++;
            requiredXp = this.level * 100;
        }
    }

    public void useExp(int exp) {
        if (this.currentXp < exp) {
            throw new IllegalArgumentException("Not enough XP");
        }
        this.currentXp -= exp;
    }

    // 성향 점수 업데이트
    public void updateStats(int logic, int emotion, int action) {
        this.logicStat += logic;
        this.emotionStat += emotion;
        this.actionStat += action;
    }
}