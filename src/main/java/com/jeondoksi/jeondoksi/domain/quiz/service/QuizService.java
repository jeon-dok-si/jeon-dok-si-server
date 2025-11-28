package com.jeondoksi.jeondoksi.domain.quiz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jeondoksi.jeondoksi.domain.book.client.NaverBookClient;
import com.jeondoksi.jeondoksi.domain.book.dto.BookSearchResponse;
import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.quiz.client.OpenAiClient;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizResponse;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizResultResponse;
import com.jeondoksi.jeondoksi.domain.quiz.dto.QuizSubmitRequest;
import com.jeondoksi.jeondoksi.domain.quiz.entity.Quiz;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizLog;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizQuestion;
import com.jeondoksi.jeondoksi.domain.quiz.entity.QuizType;
import com.jeondoksi.jeondoksi.domain.quiz.repository.QuizLogRepository;
import com.jeondoksi.jeondoksi.domain.quiz.repository.QuizRepository;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.repository.ReportRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizLogRepository quizLogRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final ReportRepository reportRepository;
    private final OpenAiClient openAiClient;
    private final NaverBookClient naverBookClient;
    private final Gson gson;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Transactional
    public QuizResponse getQuiz(String isbn, Long userId) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Quiz quiz = quizRepository.findByBookAndUser(book, user)
                .orElseGet(() -> createQuiz(book, user));

        return QuizResponse.from(quiz);
    }

    @Transactional
    public Quiz createQuiz(Book book, User user) {
        // 1. 네이버 API로 책 상세 정보 가져오기
        String naverDescription = "";
        try {
            List<BookSearchResponse> searchResults = naverBookClient.searchBooks(book.getTitle());
            if (!searchResults.isEmpty()) {
                naverDescription = searchResults.get(0).getDescription();
            }
        } catch (Exception e) {
            log.warn("Failed to fetch Naver book description", e);
        }

        // 2. 사용자의 감상문(Report) 가져오기
        String userReview = "";
        Optional<Report> report = reportRepository.findByBookAndUser(book, user);
        if (report.isPresent()) {
            userReview = report.get().getContent();
        }

        // 3. 프롬프트 생성
        String prompt = createQuizPrompt(book, naverDescription, userReview);

        // 4. GPT 호출
        String jsonResponse = openAiClient.generateQuiz(prompt);
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);

        Quiz quiz = Quiz.builder()
                .book(book)
                .user(user)
                .build();

        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject obj = jsonArray.get(i).getAsJsonObject();
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .questionNo(obj.get("questionNo").getAsInt())
                    .type(QuizType.valueOf(obj.get("type").getAsString()))
                    .question(obj.get("question").getAsString())
                    .optionsJson(obj.has("options") ? gson.toJson(obj.get("options")) : null)
                    .answer(obj.get("answer").getAsString())
                    .build();
            quiz.getQuestions().add(question);
        }

        return quizRepository.save(quiz);
    }

    private String createQuizPrompt(Book book, String naverDescription, String userReview) {
        StringBuilder sb = new StringBuilder();
        sb.append("책 제목: ").append(book.getTitle()).append("\n");
        sb.append("기본 줄거리: ").append(book.getDescription()).append("\n");

        if (!naverDescription.isEmpty()) {
            sb.append("참고용 상세 소개(질문 생성 금지, 오직 책 식별용): ").append(naverDescription).append("\n");
        }

        if (!userReview.isEmpty()) {
            sb.append("사용자 감상문: ").append(userReview).append("\n");
        }

        sb.append("\n위 책의 내용을 바탕으로 퀴즈 5문제를 만들어줘.\n");
        sb.append("중요한 요구사항 (반드시 준수):\n");
        sb.append("1. '네이버 API'나 '상세 소개'에만 나오는 내용(출판일, 몇 번째 소설인지, 베스트셀러 순위, 작가의 다른 작품 등)은 절대 문제로 내지 마.\n");
        sb.append("2. 오직 책의 '줄거리', '등장인물', '책 내부의 사건', '주제'에 관한 내용으로만 문제를 만들어.\n");
        sb.append("3. 난이도는 '하'로 설정해. 책을 읽은 사람이라면 누구나 기억할 만한 굵직한 사건이나 핵심 인물에 대해 물어봐.\n");
        sb.append(
                "4. 사용자가 작성한 감상문 내용이 있다면, 그 감상문에서 언급된 사건이나 인물을 중심으로 문제를 1~2개 출제해줘. (단, '감상문에서 뭐라고 했나?'를 묻지 말고, 감상문이 다루는 '책의 내용'을 물어봐)\n");
        sb.append("5. 너무 지엽적인 내용(몇 페이지, 아주 사소한 소품 등)은 피하고, 전체적인 흐름을 묻는 문제를 내줘.\n");

        sb.append("\n[예시 가이드]\n");
        sb.append("나쁜 질문 (절대 금지):\n");
        sb.append("- \"이 책은 작가의 몇 번째 작품인가?\" (책 외적 정보)\n");
        sb.append("- \"이 책은 출간 후 몇 달 만에 베스트셀러가 되었나?\" (책 외적 정보)\n");
        sb.append("- \"주인공이 34페이지에서 먹은 음식은?\" (너무 지엽적)\n");
        sb.append("- \"감상문에서 사용자가 가장 감동받았다고 한 문장은?\" (감상문 자체를 묻지 말 것)\n");
        sb.append("\n좋은 질문 (권장):\n");
        sb.append("- \"주인공이 결국 선택한 결말은 무엇인가?\" (핵심 줄거리)\n");
        sb.append("- \"이 소설의 배경이 되는 도시는?\" (주요 설정)\n");
        sb.append("- \"주인공과 갈등을 빚는 주요 인물은 누구인가?\" (인물 관계)\n");
        sb.append("- \"(감상문 기반) 사용자가 인상 깊게 본 '마지막 장면'에서 주인공은 어떤 행동을 했는가?\" (감상문이 주목한 책 내용)\n");

        sb.append("\nJSON 형식으로 반환해줘. 형식은 다음과 같아:\n" +
                "[\n" +
                "  {\n" +
                "    \"questionNo\": 1,\n" +
                "    \"type\": \"MULTIPLE\",\n" +
                "    \"question\": \"질문 내용\",\n" +
                "    \"options\": [\"보기1\", \"보기2\", \"보기3\", \"보기4\", \"보기5\"],\n" +
                "    \"answer\": \"정답 보기\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionNo\": 3,\n" +
                "    \"type\": \"OX\",\n" +
                "    \"question\": \"질문 내용\",\n" +
                "    \"options\": [\"O\", \"X\"],\n" +
                "    \"answer\": \"O\"\n" +
                "  },\n" +
                "  {\n" +
                "    \"questionNo\": 5,\n" +
                "    \"type\": \"SHORT\",\n" +
                "    \"question\": \"질문 내용\",\n" +
                "    \"options\": [],\n" +
                "    \"answer\": \"단답형 정답\"\n" +
                "  }\n" +
                "]\n" +
                "총 5문제이고, 다음 규칙을 반드시 지켜줘:\n" +
                "1. 1번, 2번 문제는 객관식(MULTIPLE)이며, 보기는 반드시 5개여야 해.\n" +
                "2. 3번, 4번 문제는 OX퀴즈(OX)여야 해.\n" +
                "3. 5번 문제는 단답형(SHORT)이어야 해.\n" +
                "한국어로 작성해줘.");

        return sb.toString();
    }

    @Transactional
    public QuizResultResponse submitQuiz(Long userId, QuizSubmitRequest request) {
        log.info("Quiz submission request - userId: {}, quizId: {}", userId, request.getQuizId());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new BusinessException(ErrorCode.QUIZ_NOT_FOUND));

        // 이미 푼 기록이 있는지 확인 (중복 보상 방지)
        boolean alreadySolved = quizLogRepository.existsByUserAndQuizAndIsSolvedTrue(user, quiz);
        log.info("Already solved: {}", alreadySolved);

        int score = calculateScore(quiz, request.getAnswers());
        boolean isSolved = score >= 60;
        int gainedExp = 0;

        log.info("Quiz result - score: {}, isSolved: {}", score, isSolved);

        if (isSolved && !alreadySolved) {
            gainedExp = 100; // 통과 시 100 XP
            boolean leveledUp = user.gainExp(gainedExp);
            log.info("User gained {} exp. Current XP: {}, Point: {}", gainedExp, user.getCurrentXp(), user.getPoint());

            if (leveledUp) {
                log.info("User leveled up! New Level: {}", user.getLevel());
                // TODO: 레벨업 알림 또는 추가 보상 로직
            }
        }

        QuizLog logEntity = QuizLog.builder()
                .user(user)
                .quiz(quiz)
                .isSolved(isSolved)
                .score(score)
                .build();
        quizLogRepository.save(logEntity);

        return QuizResultResponse.builder()
                .logId(logEntity.getLogId())
                .isSolved(isSolved)
                .score(score)
                .gainedExp(gainedExp)
                .build();
    }

    private int calculateScore(Quiz quiz, List<QuizSubmitRequest.AnswerDto> answers) {
        Map<Long, String> answerMap = answers.stream()
                .collect(Collectors.toMap(QuizSubmitRequest.AnswerDto::getQuestionId,
                        QuizSubmitRequest.AnswerDto::getAnswer));

        double totalScore = 0.0;
        int totalQuestions = quiz.getQuestions().size();

        if (totalQuestions == 0) {
            return 0;
        }

        for (QuizQuestion question : quiz.getQuestions()) {
            String userAnswer = answerMap.get(question.getQuestionId());
            // userAnswer가 null인 경우 (사용자가 답변하지 않은 경우) 0점 처리
            totalScore += gradeAnswer(question, userAnswer);
        }

        return (int) ((totalScore / totalQuestions) * 100);
    }

    /**
     * 개별 문제 채점 (유형별 차등 점수)
     * 
     * @return 0.0 ~ 1.0 (0% ~ 100%)
     */
    private double gradeAnswer(QuizQuestion question, String userAnswer) {
        String correctAnswer = question.getAnswer();
        QuizType type = question.getType();

        // 1. 전처리: 공백 제거, 소문자 변환
        String normalizedUser = normalize(userAnswer);
        String normalizedCorrect = normalize(correctAnswer);

        // 2. 완전 일치 → 100%
        if (normalizedUser.equals(normalizedCorrect)) {
            return 1.0;
        }

        // 3. 문제 유형별 채점
        switch (type) {
            case OX:
            case MULTIPLE:
                // 프론트에서 정확한 값을 보내므로 완전 일치만 확인
                return 0.0;

            case SHORT:
                // 단답형만 후하게 채점 (유사도 + 부분 점수)
                return gradeShortAnswer(normalizedUser, normalizedCorrect, userAnswer, correctAnswer);

            default:
                return 0.0;
        }
    }

    /**
     * 단답형 문제 채점 (후함)
     * - 포함 관계 검사
     * - 유사도 80% 이상 → 정답
     * - 유사도 60% 이상 → 부분 점수 (70%)
     * - 유사도 40% 이상 → 부분 점수 (30%)
     */
    private double gradeShortAnswer(String normalizedUser, String normalizedCorrect,
            String originalUser, String originalCorrect) {
        // 1. 포함 관계 검사 (한글 조사 제거)
        String koreanUser = normalizeKorean(originalUser);
        String koreanCorrect = normalizeKorean(originalCorrect);

        if (koreanCorrect.contains(koreanUser) || koreanUser.contains(koreanCorrect)) {
            log.debug("포함 관계 인정: '{}' ↔ '{}'", koreanUser, koreanCorrect);
            return 1.0;
        }

        // 2. 유사도 검사
        double similarity = calculateStringSimilarity(normalizedUser, normalizedCorrect);

        if (similarity >= SIMILARITY_THRESHOLD) {
            log.debug("유사도 {}로 정답 인정: '{}' ≈ '{}'",
                    String.format("%.1f%%", similarity * 100), originalUser, originalCorrect);
            return 1.0; // 80% 이상 → 정답
        } else if (similarity >= 0.6) {
            log.debug("유사도 {}로 부분 점수: '{}' ≈ '{}'",
                    String.format("%.1f%%", similarity * 100), originalUser, originalCorrect);
            return 0.7; // 60% 이상 → 70% 부분 점수
        } else if (similarity >= 0.4) {
            log.debug("유사도 {}로 소량 부분 점수: '{}' ≈ '{}'",
                    String.format("%.1f%%", similarity * 100), originalUser, originalCorrect);
            return 0.3; // 40% 이상 → 30% 부분 점수
        }

        return 0.0;
    }

    /**
     * 문자열 정규화 (공백/특수문자 제거, 소문자 변환)
     */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .toLowerCase()
                .replaceAll("[\\s\\p{Punct}]", ""); // 공백, 특수문자 제거
    }

    /**
     * 한글 정규화 (조사 제거, 공백 제거)
     */
    private String normalizeKorean(String text) {
        if (text == null) {
            return "";
        }
        return text.trim()
                .replaceAll("[은는이가을를에게의와과]", "") // 조사 제거
                .replaceAll("\\s", ""); // 공백 제거
    }

    /**
     * 문자열 유사도 계산 (Levenshtein Distance)
     * 
     * @return 0.0 ~ 1.0
     */
    private double calculateStringSimilarity(String s1, String s2) {
        if (s1 == null || s2 == null) {
            return 0.0;
        }

        int maxLen = Math.max(s1.length(), s2.length());
        if (maxLen == 0) {
            return 1.0;
        }

        int distance = levenshteinDistance(s1, s2);
        return 1.0 - ((double) distance / maxLen);
    }

    /**
     * Levenshtein Distance 계산 (편집 거리)
     */
    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            dp[i][0] = i;
        }
        for (int j = 0; j <= s2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1, // 삭제
                        dp[i][j - 1] + 1), // 삽입
                        dp[i - 1][j - 1] + cost); // 교체
            }
        }

        return dp[s1.length()][s2.length()];
    }
}
