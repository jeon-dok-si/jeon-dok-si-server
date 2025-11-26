package com.jeondoksi.jeondoksi.core.nlp;

import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextComplexityMetric {

    // 논리적 접속사 목록
    private final List<String> logicalConjunctions = List.of(
            "따라서", "그러므로", "왜냐하면", "반면에", "하지만", "그러나", "즉", "결국", "비록", "또한");

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

        // 점수 계산 (가중치 적용)
        double score = (avgLength * 0.5) + (hardNounCount * 5) + (conjunctionCount * 10);
        return (int) Math.min(score, 100); // Max 100
    }
}
