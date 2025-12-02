package com.jeondoksi.jeondoksi.domain.user.dto;

import com.jeondoksi.jeondoksi.domain.gamification.dto.CharacterResponse;
import com.jeondoksi.jeondoksi.domain.gamification.entity.Character;
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
    private Integer point; // 포인트 추가
    private Stats stats;
    private String dominantType; // 주요 성향 타입 (PHILOSOPHER, EMPATH 등)
    private CharacterResponse character; // 장착한 캐릭터 정보 (경험치 포함)
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

    public static UserResponse from(User user, Character character) {
        // 성향 합계 및 평균
        int totalStats = user.getLogicStat() + user.getEmotionStat() + user.getActionStat();
        int avgStats = totalStats / 3;

        // 주요 성향 판단
        String dominantType = determineDominantType(user);

        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .point(user.getPoint()) // 포인트 매핑
                .stats(Stats.builder()
                        .logic(user.getLogicStat())
                        .emotion(user.getEmotionStat())
                        .action(user.getActionStat())
                        .total(totalStats)
                        .average(avgStats)
                        .build())
                .dominantType(dominantType)
                .character(character != null ? CharacterResponse.from(character) : null)
                .createdAt(user.getCreatedAt())
                .build();
    }

    // 하위 호환성을 위해 기존 메서드 유지 (character 없이 호출 시 null 처리)
    public static UserResponse from(User user) {
        return from(user, null);
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
