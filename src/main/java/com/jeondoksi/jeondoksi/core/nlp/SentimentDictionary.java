package com.jeondoksi.jeondoksi.core.nlp;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class SentimentDictionary {

    // 단어별 감성 점수 (1~3점)
    private final Map<String, Integer> sentimentMap;
    // 강조 부사 (2배 증폭)
    private final Map<String, Double> intensifiers;
    // 감성적인 어미 집합
    private final java.util.Set<String> emotionalEndings;

    public SentimentDictionary() {
        sentimentMap = new HashMap<>();
        intensifiers = new HashMap<>();
        emotionalEndings = new java.util.HashSet<>();
        initializeDictionary();
    }

    private void initializeDictionary() {
        // --- 긍정 (Positive) ---
        // 1점: 가벼운 긍정, 편안함, 일상적 행복
        addWords(1, "좋", "나쁘지 않", "괜찮", "편안", "무난", "적당", "깔끔", "소소", "다행",
                "안도", "차분", "산뜻", "양호", "수월", "순조", "무해", "단정", "아늑", "포근",
                "잔잔", "은은", "담백", "고요", "평온", "안정", "여유", "느긋", "포근", "따뜻",
                "온기", "훈훈", "싱그러", "풋풋", "말끔", "개운", "시원", "후련");

        // 2점: 일반적인 긍정, 기쁨, 사랑, 그리움(긍정적)
        addWords(2, "기쁘", "행복", "즐겁", "희망", "아름답", "훌륭", "멋지", "사랑", "감사", "평화",
                "성공", "승리", "열정", "용기", "지혜", "친절", "배려", "만족", "재미", "흥미",
                "신나", "뿌듯", "상쾌", "흐뭇", "든든", "설레", "벅차", "유쾌", "통쾌", "황홀",
                "활기", "생기", "명랑", "쾌활", "낙관", "기대", "보람", "자부", "애정", "호감",
                "반갑", "그립", "달콤", "감미", "우아", "고귀", "순수", "진실", "정직", "현명",
                "일렁", "뭉클", "애틋", "아련", "여운", "눈시울", "울컥", "먹먹", "심쿵", "감동",
                "위로", "치유", "보듬", "쓰담", "토닥", "다정", "자상", "따스", "화사", "찬란");

        // 3점: 강한 긍정, 격한 감동, 숭고함
        addWords(3, "최고", "환상", "경이", "전율", "압도", "완벽", "눈부시", "위대",
                "존경", "숭고", "열광", "도취", "매혹", "사랑스럽", "자랑스럽", "감격",
                "환희", "희열", "무아지경", "황홀경", "경탄", "찬사", "숭배", "갈망", "열망", "동경",
                "축복", "영광", "성스럽", "거룩", "장엄", "웅장", "비장", "결연", "확신",
                "사무치", "가슴 벅차", "터질 듯", "벅차오르", "눈물 나", "오열", "통곡"); // 긍정적 오열 포함

        // --- 부정 (Negative) ---
        // 1점: 가벼운 부정, 아쉬움, 권태
        addWords(1, "아쉽", "별로", "조금", "약간", "지루", "심심", "피곤", "귀찮", "답답",
                "씁쓸", "서운", "허전", "묘하", "찝찝", "난감", "곤란", "어색", "민망", "무안",
                "싱겁", "허탈", "무료", "따분", "식상", "진부", "평범", "미흡", "부족",
                "덤덤", "무심", "시큰둥", "떨떠름", "막막", "갑갑");

        // 2점: 일반적인 부정, 슬픔, 화남, 상처
        addWords(2, "슬프", "우울", "비참", "고통", "화나", "무섭", "끔찍", "나쁘", "절망", "실패",
                "패배", "분노", "증오", "공포", "불안", "걱정", "후회", "상처", "이별", "죽음",
                "짜증", "실망", "억울", "속상", "안타깝", "괴롭", "허무", "외롭", "쓸쓸",
                "분하", "원통", "야속", "서럽", "애석", "비통", "참담", "암담", "고독", "소외",
                "배신", "모욕", "수치", "부끄럽", "창피", "비겁", "비열", "잔인",
                "시리", "아프", "쓰라리", "저리", "미어지", "찢어지", "무너지", "주저앉");

        // 3점: 강한 부정, 파괴적 감정, 극심한 고통
        addWords(3, "절규", "경악", "치욕", "혐오", "저주", "파멸", "재앙",
                "지옥", "악몽", "소름", "처참", "참혹", "분개", "격분",
                "격노", "발광", "통탄", "개탄", "살의", "파괴", "멸망", "종말", "재난", "비극",
                "참사", "학살", "유린", "말살", "박살", "작살", "끔찍", "소름끼치", "치가 떨리",
                "피가 거꾸로", "억장이 무너", "하늘이 무너");

        // --- 강조 부사 (Intensifiers) ---
        intensifiers.put("매우", 2.0);
        intensifiers.put("너무", 2.0);
        intensifiers.put("정말", 2.0);
        intensifiers.put("진짜", 2.0);
        intensifiers.put("엄청", 2.0);
        intensifiers.put("몹시", 2.0);
        intensifiers.put("무척", 2.0);
        intensifiers.put("가장", 1.5);
        intensifiers.put("제일", 1.5);
        intensifiers.put("상당히", 1.5);
        intensifiers.put("꽤", 1.5);
        intensifiers.put("더욱", 1.5);
        intensifiers.put("훨씬", 1.5);
        intensifiers.put("심히", 1.5);
        intensifiers.put("지극히", 1.5);
        intensifiers.put("완전", 1.5);
        intensifiers.put("되게", 1.5);

        // --- 감성적인 어미 (Emotional Endings) ---
        // 독백, 감탄, 회상, 권유 등 감정이 실린 어미들
        emotionalEndings.add("구나");
        emotionalEndings.add("군요");
        emotionalEndings.add("네");
        emotionalEndings.add("네요");
        emotionalEndings.add("더라");
        emotionalEndings.add("던가");
        emotionalEndings.add("련만");
        emotionalEndings.add("을까");
        emotionalEndings.add("ㄹ까");
        emotionalEndings.add("죠");
        emotionalEndings.add("지요");
        emotionalEndings.add("나"); // ~하나? ~했나?
        emotionalEndings.add("니"); // ~하니?
        emotionalEndings.add("다오");
        emotionalEndings.add("소서");
        emotionalEndings.add("노라");
    }

    private void addWords(int score, String... words) {
        for (String word : words) {
            sentimentMap.put(word, score);
        }
    }

    public int getScore(String word) {
        return sentimentMap.getOrDefault(word, 0);
    }

    public double getIntensifierMultiplier(String word) {
        return intensifiers.getOrDefault(word, 1.0);
    }

    public boolean isPositive(String word) {
        return sentimentMap.containsKey(word);
    }

    public boolean isNegative(String word) {
        return sentimentMap.containsKey(word);
    }

    public boolean isEmotionalEnding(String ending) {
        // 어미의 끝부분이 emotionalEndings에 포함되는지 확인
        // 예: "했구나" -> "구나" 포함
        // Komoran이 어미를 분리해주므로 정확한 매칭을 우선 시도
        return emotionalEndings.contains(ending);
    }
}
