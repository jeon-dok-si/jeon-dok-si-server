package com.jeondoksi.jeondoksi.core.nlp;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class TextComplexityMetric {

    // 논리적 접속사 목록 (확장)
    private final List<String> logicalConjunctions = List.of(
            "따라서", "그러므로", "왜냐하면", "반면에", "하지만", "그러나", "즉", "결국", "비록", "또한",
            "게다가", "더불어", "오히려", "도리어", "바꾸어", "말하면", "요컨대", "결론적으로", "때문에", "덕분에");

    public int calculateLogicScore(String text, KomoranResult analyzeResult) {
        // 1. 평균 문장 길이 (단순화: 마침표 기준)
        String[] sentences = text.split("[.!?]");
        double avgLength = 0;
        if (sentences.length > 0) {
            avgLength = (double) text.length() / sentences.length;
        }

        // 2. 4음절 이상 명사 비율 (고난이도 어휘)
        List<String> nouns = analyzeResult.getNouns();
        long hardNounCount = nouns.stream()
                .filter(noun -> noun.length() >= 4)
                .count();

        // 3. 논리 접속사 빈도
        long conjunctionCount = logicalConjunctions.stream()
                .filter(text::contains)
                .count();

        // 4. 어휘 다양성 (TTR: Type-Token Ratio)
        double ttr = calculateTTR(nouns);

        // 점수 계산 (가중치 적용)
        // TTR: 0.5~0.8 사이가 일반적. 100점 만점 환산 시 30점 비중
        // 문장 길이: 50자 내외가 적당. 20점 비중
        // 접속사: 10개 이상이면 만점. 20점 비중
        // 고난이도 어휘: 10개 이상이면 만점. 30점 비중

        double ttrScore = ttr * 30; // Max 30
        double lengthScore = Math.min(avgLength, 50) * 0.4; // Max 20
        double conjunctionScore = Math.min(conjunctionCount, 10) * 2; // Max 20
        double vocabularyScore = Math.min(hardNounCount, 10) * 3; // Max 30

        double totalScore = ttrScore + lengthScore + conjunctionScore + vocabularyScore;
        return (int) Math.min(totalScore, 100);
    }

    private double calculateTTR(List<String> tokens) {
        if (tokens.isEmpty()) {
            return 0.0;
        }
        Set<String> uniqueTokens = new HashSet<>(tokens);
        return (double) uniqueTokens.size() / tokens.size();
    }
}
