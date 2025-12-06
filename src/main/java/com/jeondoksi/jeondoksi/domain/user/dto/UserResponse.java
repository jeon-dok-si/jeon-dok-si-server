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

        // NlpAnalyzer와 동일한 로직 적용 (60점 기준)

        // 1. 감성형 (EMPATH) - 우선순위 최상
        // 감성 점수가 60점 이상이고, 다른 점수보다 높거나 같을 때
        if (emotion >= 60 && emotion >= logic && emotion >= action)
            return "EMPATH";

        // 2. 사색하는 철학자 (PHILOSOPHER) - 논리 + 감성
        if (logic >= 60 && emotion >= 60)
            return "PHILOSOPHER";

        // 3. 용의주도한 전략가 (STRATEGIST) - 논리 + 행동
        if (logic >= 60 && action >= 60)
            return "STRATEGIST";

        // 4. 영감을 주는 모험가 (VISIONARY) - 감성 + 행동
        if (emotion >= 60 && action >= 60)
            return "VISIONARY";

        // 5. 냉철한 분석가 (ANALYST) - 논리 단독
        if (logic >= 60)
            return "ANALYST";

        // 6. 행동하는 실천가 (ACTIVIST) - 행동 단독
        if (action >= 60)
            return "ACTIVIST";

        // 7. 성실한 독서가 (READER) - 기본
        return "READER";
    }
}
