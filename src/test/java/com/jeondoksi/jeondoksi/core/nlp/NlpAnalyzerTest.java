package com.jeondoksi.jeondoksi.core.nlp;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NlpAnalyzerTest {

    private NlpAnalyzer nlpAnalyzer;

    @BeforeEach
    void setUp() {
        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        SentimentDictionary sentimentDictionary = new SentimentDictionary();
        TextComplexityMetric textComplexityMetric = new TextComplexityMetric();
        nlpAnalyzer = new NlpAnalyzer(komoran, sentimentDictionary, textComplexityMetric);
    }

    @Test
    @DisplayName("NLP 성향 분석 테스트 - 긍정적이고 논리적인 글")
    void analyze_positive_logic() {
        // given
        String text = "이 책은 정말 훌륭하고 감동적이다. 따라서 나는 이 책을 강력하게 추천한다. 작가의 통찰력은 매우 깊다.";

        // when
        NlpAnalyzer.AnalysisResult result = nlpAnalyzer.analyze(text);

        // then
        System.out.println("Logic: " + result.getLogicScore());
        System.out.println("Emotion: " + result.getEmotionScore());
        System.out.println("Action: " + result.getActionScore());
        System.out.println("Type: " + result.getType());

        assertThat(result.getLogicScore()).isGreaterThan(0);
        assertThat(result.getEmotionScore()).isGreaterThan(0);
        assertThat(result.getType()).isNotNull();
    }
}
