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
        // 목표: 감성 단어가 7% 정도면 100점 (기존 10% -> 7%로 대폭 완화)
        double vocabRatio = vocabScoreRaw / totalWords;
        double vocabScore = Math.min(vocabRatio * 1500, 100); // 0.066 * 1500 = 100

        // B. Modifier Density (30% Weight)
        int modifierCount = 0;

        for (Token token : tokens) {
            String pos = token.getPos();
            if (pos.startsWith("VA") || pos.startsWith("MAG")) { // 형용사, 일반부사
                modifierCount++;
            }
        }
        // 밀도 점수: 전체 단어 중 10%가 수식어면 100점 (기존 15% -> 10%로 완화)
        double densityRatio = (double) modifierCount / totalWords;
        double densityScore = Math.min(densityRatio * 1000, 100); // 0.1 * 1000 = 100

        // Final Emotion Score Calculation (Weighted Sum)
        // Vocab: 70%, Density: 30%
        double totalEmotionScore = (vocabScore * 0.7) + (densityScore * 0.3);

        // 보정: 감성 단어가 하나도 없으면 점수를 대폭 낮춤 (False Positive 방지)
        if (vocabScoreRaw == 0) {
            totalEmotionScore *= 0.2;
        }

        int emotionScore = (int) totalEmotionScore;
        emotionScore = Math.min(emotionScore, 100);

        // 3. Action Score (Refined with Strong Verbs)
        double actionWeightedScore = 0;
        for (int i = 0; i < tokens.size(); i++) {
            Token token = tokens.get(i);
            String pos = token.getPos();
            String morph = token.getMorph();

            // 1. Strong Action Verbs (3.0 points)
            if (sentimentDictionary.isStrongActionVerb(morph)) {
                actionWeightedScore += 3.0;
            }
            // 2. Generic Verbs (0.5 points) - Only if not a strong verb
            else if (pos.startsWith("VV")) {
                actionWeightedScore += 0.5;
            }

            // 3. Intent/Will Patterns (2.0 points)
            // 청유형 어미 (-자, -ㅂ시다), 의지 (-겠다, -ㄹ게)
            if (pos.equals("EC") || pos.equals("EF")) {
                if (morph.endsWith("자") || morph.endsWith("시다") || morph.endsWith("게") || morph.endsWith("다")) {
                    actionWeightedScore += 2.0;
                }
            }
            // 선어말어미 (의지)
            if (pos.equals("EP") && (morph.equals("겠") || morph.equals("리"))) {
                actionWeightedScore += 2.0;
            }

            // 특수 행동 패턴 (User Request)
            if (i > 0) {
                Token prevToken = tokens.get(i - 1);
                String prevPos = prevToken.getPos();
                String prevMorph = prevToken.getMorph();

                // 1. 소망/갈망 (-고 싶다)
                // 패턴: EC(-고) + VX(싶)
                if (pos.equals("VX") && morph.equals("싶") && prevPos.equals("EC") && prevMorph.endsWith("고")) {
                    actionWeightedScore += 2.0;
                }

                // 2. 의도/노력 (-려 하다)
                // 패턴: EC(-려/-려고) + VX(하)
                if (pos.equals("VX") && morph.startsWith("하") && prevPos.equals("EC")
                        && (prevMorph.endsWith("려") || prevMorph.endsWith("려고"))) {
                    actionWeightedScore += 2.0;
                }

                // 3. 미래/의지 (-ㄹ 것이다)
                // 패턴: ETM(-ㄹ/-을) + NNB(것)
                if (pos.equals("NNB") && morph.equals("것") && prevPos.equals("ETM")
                        && (prevMorph.endsWith("ㄹ") || prevMorph.endsWith("을"))) {
                    actionWeightedScore += 2.0;
                }
            }
        }
        // 가중치 점수 정규화
        // 목표: 가중치 합이 전체 단어 수의 20% 정도면 100점
        // 예: 100단어 중 5개가 Strong Verb(15점) + 2개가 Intent(4점) + 5개가 Generic(2.5점) = 21.5점
        // -> 100점
        int actionScore = (int) ((actionWeightedScore / totalWords) * 500);
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
