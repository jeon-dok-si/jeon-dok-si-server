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

    @Column(nullable = false)
    @ColumnDefault("0")
    private int point = 0;

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

    @Version
    private Long version;

    @Builder
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.level = 1;
        this.currentXp = 0;
        this.point = 0;
        this.logicStat = 0;
        this.emotionStat = 0;
        this.actionStat = 0;
    }

    /**
     * 경험치 획득 및 레벨업 로직
     * 
     * @param exp 획득할 경험치
     * @return 레벨업 여부 (true: 레벨업 함, false: 안 함)
     */
    public boolean gainExp(int exp) {
        this.currentXp += exp;
        this.point += exp; // 포인트도 함께 증가

        boolean leveledUp = false;
        int requiredXp = getRequiredXpForNextLevel();

        while (this.currentXp >= requiredXp) {
            this.currentXp -= requiredXp;
            this.level++;
            leveledUp = true;
            requiredXp = getRequiredXpForNextLevel();
        }

        return leveledUp;
    }

    private int getRequiredXpForNextLevel() {
        return this.level * 100;
    }

    public void usePoint(int amount) {
        if (this.point < amount) {
            throw new IllegalArgumentException("Not enough Points");
        }
        this.point -= amount;
    }

    // 성향 점수 업데이트 (EMA: 지수 이동 평균 적용)
    // alpha = 0.3 (새로운 값의 가중치)
    public void updateStats(int logic, int emotion, int action) {
        if (this.logicStat == 0 && this.emotionStat == 0 && this.actionStat == 0) {
            // 초기 상태면 바로 대입
            this.logicStat = logic;
            this.emotionStat = emotion;
            this.actionStat = action;
        } else {
            // 기존 값과 새로운 값의 가중 평균 (0~100 유지)
            this.logicStat = (int) (this.logicStat * 0.7 + logic * 0.3);
            this.emotionStat = (int) (this.emotionStat * 0.7 + emotion * 0.3);
            this.actionStat = (int) (this.actionStat * 0.7 + action * 0.3);
        }
    }
}