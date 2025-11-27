package com.jeondoksi.jeondoksi.domain.quiz.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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
    private final OpenAiClient openAiClient;
    private final Gson gson;

    private static final double SIMILARITY_THRESHOLD = 0.8;

    @Transactional
    public QuizResponse getQuiz(String isbn) {
        Book book = bookRepository.findById(isbn)
                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

        Quiz quiz = quizRepository.findByBook(book)
                .orElseGet(() -> createQuiz(book));

        return QuizResponse.from(quiz);
    }

    @Transactional
    public Quiz createQuiz(Book book) {
        String jsonResponse = openAiClient.generateQuiz(book.getTitle(), book.getDescription());
        JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);

        Quiz quiz = Quiz.builder().book(book).build();

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
