package com.jeondoksi.jeondoksi.core.nlp;

import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import kr.co.shineware.nlp.komoran.model.Token;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NlpAnalyzer {

    private final Komoran komoran;
    private final SentimentDictionary sentimentDictionary;
    private final TextComplexityMetric textComplexityMetric;

    @Getter
    @Builder
    public static class AnalysisResult {
        private int logicScore;
        private int emotionScore;
        private int actionScore;
        private String type; // ANALYST, PHILOSOPHER, EMPATH, ACTIVIST
    }

    public AnalysisResult analyze(String text) {
        KomoranResult analyzeResult = komoran.analyze(text);
        List<Token> tokens = analyzeResult.getTokenList();

        // 1. Logic Score
        int logicScore = textComplexityMetric.calculateLogicScore(text, analyzeResult);

        // 2. Emotion Score
        int emotionCount = 0;
        int totalWords = tokens.size();
        for (Token token : tokens) {
            String morph = token.getMorph();
            // 단순 포함 여부로 확인 (어근 매칭을 위해)
            if (sentimentDictionary.isPositive(morph) || sentimentDictionary.isNegative(morph)) {
                emotionCount++;
            }
        }
        int emotionScore = (int) (((double) emotionCount / totalWords) * 500); // 가중치 증폭
        emotionScore = Math.min(emotionScore, 100);

        // 3. Action Score (청유형 어미, 동사 비율)
        int actionCount = 0;
        for (Token token : tokens) {
            String pos = token.getPos();
            String morph = token.getMorph();
            // 동사(VV)이거나 청유형 어미(EC - 자, 하자 등 단순 매칭 어렵지만 예시로)
            if (pos.startsWith("VV") || morph.endsWith("자") || morph.endsWith("다")) {
                actionCount++;
            }
        }
        int actionScore = (int) (((double) actionCount / totalWords) * 300);
        actionScore = Math.min(actionScore, 100);

        // 4. Type Classification
        String type = determineType(logicScore, emotionScore, actionScore);

        return AnalysisResult.builder()
                .logicScore(logicScore)
                .emotionScore(emotionScore)
                .actionScore(actionScore)
                .type(type)
                .build();
    }

    private String determineType(int logic, int emotion, int action) {
        if (logic >= 60 && emotion >= 60)
            return "PHILOSOPHER"; // 사색형
        if (logic >= 60)
            return "ANALYST"; // 탐구형
        if (emotion >= 60)
            return "EMPATH"; // 감성형
        if (action >= 60)
            return "ACTIVIST"; // 실천형
        return "READER"; // 일반 독서가
    }

    public double calculateSimilarity(String text1, String text2) {
        List<String> tokens1 = komoran.analyze(text1).getNouns();
        List<String> tokens2 = komoran.analyze(text2).getNouns();

        java.util.Set<String> allTokens = new java.util.HashSet<>();
        allTokens.addAll(tokens1);
        allTokens.addAll(tokens2);

        java.util.Map<String, Integer> vector1 = new java.util.HashMap<>();
        java.util.Map<String, Integer> vector2 = new java.util.HashMap<>();

        for (String token : allTokens) {
            vector1.put(token, java.util.Collections.frequency(tokens1, token));
            vector2.put(token, java.util.Collections.frequency(tokens2, token));
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (String token : allTokens) {
            int v1 = vector1.get(token);
            int v2 = vector2.get(token);
            dotProduct += v1 * v2;
            norm1 += Math.pow(v1, 2);
            norm2 += Math.pow(v2, 2);
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
