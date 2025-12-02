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

        // 1. Logic Score (Improved)
        int logicScore = textComplexityMetric.calculateLogicScore(text, analyzeResult);

        // 2. Emotion Score (Refined based on User Feedback)
        // A. Vocabulary Score (70% Weight)
        double vocabScoreRaw = 0;
        int totalWords = Math.max(tokens.size(), 1);

        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String morph = token.getMorph();

            int wordScore = sentimentDictionary.getScore(morph);
            if (wordScore > 0) {
                // Check for intensifier in previous token
                double multiplier = 1.0;
                if (i > 0) {
                    String prevMorph = tokens.get(i - 1).getMorph();
                    multiplier = sentimentDictionary.getIntensifierMultiplier(prevMorph);
                }
                vocabScoreRaw += (wordScore * multiplier);
            }
        }

        // 단어 점수 정규화
        // 목표: 감성 단어가 10% 정도면 100점 (기존 15% -> 10%로 완화)
        double vocabRatio = vocabScoreRaw / totalWords;
        double vocabScore = Math.min(vocabRatio * 1000, 100); // 0.1 * 1000 = 100

        // B. Modifier Density (30% Weight)
        int modifierCount = 0;

        for (Token token : tokens) {
            String pos = token.getPos();
            if (pos.startsWith("VA") || pos.startsWith("MAG")) { // 형용사, 일반부사
                modifierCount++;
            }
        }
        // 밀도 점수: 전체 단어 중 15%가 수식어면 100점
        double densityRatio = (double) modifierCount / totalWords;
        double densityScore = Math.min(densityRatio * 666, 100); // 0.15 * 666 ~= 100

        // Final Emotion Score Calculation (Weighted Sum)
        // Vocab: 70%, Density: 30%
        double totalEmotionScore = (vocabScore * 0.7) + (densityScore * 0.3);

        // 보정: 감성 단어가 하나도 없으면 점수를 대폭 낮춤 (False Positive 방지)
        if (vocabScoreRaw == 0) {
            totalEmotionScore *= 0.2;
        }

        int emotionScore = (int) totalEmotionScore;
        emotionScore = Math.min(emotionScore, 100);

        // 3. Action Score (Refined)
        int actionCount = 0;
        for (Token token : tokens) {
            String pos = token.getPos();
            String morph = token.getMorph();

            // 청유형 어미 (-자, -ㅂ시다), 의지 (-겠다, -ㄹ게)
            if (pos.equals("EC") || pos.equals("EF")) {
                if (morph.endsWith("자") || morph.endsWith("시다") || morph.endsWith("게") || morph.endsWith("다")) {
                    actionCount++;
                }
            }
            // 선어말어미 (의지)
            if (pos.equals("EP") && (morph.equals("겠") || morph.equals("리"))) {
                actionCount++;
            }
            // 동사 (일반 행동)
            if (pos.startsWith("VV")) {
                actionCount++;
            }
        }
        int actionScore = (int) (((double) actionCount / totalWords) * 250); // 가중치 하향 (기존 800 -> 400)
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
        // 감성형 기준 완화 (60 -> 50)
        if (emotion >= 50 && emotion >= logic && emotion >= action)
            return "EMPATH"; // 감성형 (우선순위 높임)

        if (logic >= 60 && emotion >= 60)
            return "PHILOSOPHER"; // 사색형
        if (logic >= 60)
            return "ANALYST"; // 탐구형
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
