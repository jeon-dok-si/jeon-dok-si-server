package com.jeondoksi.jeondoksi.core.nlp;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class SentimentDictionary {

    // 긍정 단어 사전 (예시 - 어근 위주)
    private final Set<String> positiveWords = new HashSet<>(Arrays.asList(
            "기쁘", "행복", "즐겁", "희망", "아름답", "감동", "훌륭", "멋지", "좋", "사랑",
            "감사", "평화", "성공", "승리", "열정", "용기", "지혜", "친절", "배려", "만족"));

    // 부정 단어 사전 (예시 - 어근 위주)
    private final Set<String> negativeWords = new HashSet<>(Arrays.asList(
            "슬프", "우울", "비참", "고통", "화나", "무섭", "끔찍", "나쁘", "절망", "실패",
            "패배", "분노", "증오", "공포", "불안", "걱정", "후회", "상처", "이별", "죽음"));

    public boolean isPositive(String word) {
        return positiveWords.contains(word);
    }

    public boolean isNegative(String word) {
        return negativeWords.contains(word);
    }
}
