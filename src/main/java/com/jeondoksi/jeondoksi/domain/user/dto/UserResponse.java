package com.jeondoksi.jeondoksi.domain.user.dto;

import com.jeondoksi.jeondoksi.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponse {
    private Long userId;
    private String email;
    private String nickname;
    private Integer level;
    private Integer currentXp;
    private Integer requiredXp;
    private Double progressPercentage; // 레벨업 진행률 (%)
    private Stats stats;
    private String dominantType; // 주요 성향 타입 (PHILOSOPHER, EMPATH 등)
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class Stats {
        private Integer logic;
        private Integer emotion;
        private Integer action;
        private Integer total; // 합계
        private Integer average; // 평균
    }

    public static UserResponse from(User user) {
        int requiredXp = user.getLevel() * 100;
        double progress = (double) user.getCurrentXp() / requiredXp * 100;

        // 성향 합계 및 평균
        int totalStats = user.getLogicStat() + user.getEmotionStat() + user.getActionStat();
        int avgStats = totalStats / 3;

        // 주요 성향 판단
        String dominantType = determineDominantType(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .level(user.getLevel())
                .currentXp(user.getCurrentXp())
                .requiredXp(requiredXp)
                .progressPercentage(Math.round(progress * 10) / 10.0) // 소수점 1자리
                .stats(Stats.builder()
                        .logic(user.getLogicStat())
                        .emotion(user.getEmotionStat())
                        .action(user.getActionStat())
                        .total(totalStats)
                        .average(avgStats)
                        .build())
                .dominantType(dominantType)
                .createdAt(user.getCreatedAt())
                .build();
    }

    /**
     * 사용자의 주요 성향 타입 판단
     */
    private static String determineDominantType(User user) {
        int logic = user.getLogicStat();
        int emotion = user.getEmotionStat();
        int action = user.getActionStat();

        // 가장 높은 스탯 찾기
        int max = Math.max(logic, Math.max(emotion, action));

        if (logic == max && emotion == max && action == max) {
            return "BALANCED"; // 균형잡힌 독자
        } else if (logic == max && emotion == max) {
            return "PHILOSOPHER"; // 논리 + 감정
        } else if (logic == max && action == max) {
            return "ANALYST"; // 논리 + 행동
        } else if (emotion == max && action == max) {
            return "ACTIVIST"; // 감정 + 행동
        } else if (logic == max) {
            return "THINKER"; // 논리형
        } else if (emotion == max) {
            return "EMPATH"; // 감정형
        } else if (action == max) {
            return "DOER"; // 실천형
        } else {
            return "READER"; // 기본
        }
    }
}
