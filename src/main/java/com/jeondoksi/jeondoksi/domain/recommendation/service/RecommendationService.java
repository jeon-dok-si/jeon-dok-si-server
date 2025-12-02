package com.jeondoksi.jeondoksi.domain.recommendation.service;

import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.recommendation.dto.RecommendationResponse;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.entity.ReportStatus;
import com.jeondoksi.jeondoksi.domain.report.repository.ReportRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecommendationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    private final Random random = new Random();

    /**
     * 사용자 맞춤 도서 추천
     * - 독후감 없음: 랜덤 베스트셀러 3권
     * - 독후감 있음: TF-IDF + 사용자 성향 기반 상위 10권 중 랜덤 3권
     */
    public List<RecommendationResponse> recommendBooks(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 1. 사용자가 작성한 승인된 독후감 조회
        List<Report> reports = reportRepository.findAllByUser(user).stream()
                .filter(report -> report.getStatus() == ReportStatus.APPROVED)
                .collect(Collectors.toList());

        // 2. 읽은 책 ISBN 및 정규화된 제목 수집
        Set<String> readIsbns = reports.stream()
                .map(report -> report.getBook().getIsbn())
                .collect(Collectors.toSet());

        Set<String> readNormalizedTitles = reports.stream()
                .map(report -> normalizeTitle(report.getBook().getTitle()))
                .collect(Collectors.toSet());

        // 3. 읽지 않은 책 조회 (후보군)
        List<Book> allBooks = bookRepository.findAll();

        // 3-1. 읽은 책 제외 (ISBN 및 정규화된 제목 기준 + 유사도 기준)
        List<Book> unreadBooks = allBooks.stream()
                .filter(book -> !readIsbns.contains(book.getIsbn()))
                .filter(book -> {
                    String title = normalizeTitle(book.getTitle());
                    // 1. 완전 일치 제외
                    if (readNormalizedTitles.contains(title)) {
                        return false;
                    }
                    // 2. 유사도 60% 이상 제외 (시리즈물, 비슷한 제목 방지)
                    for (String readTitle : readNormalizedTitles) {
                        if (calculateStringSimilarity(title, readTitle) >= 0.6) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());

        // 3-2. 중복된 책 제거 (정규화된 제목 기준, 제목이 가장 짧은 것 우선)
        Map<String, Book> uniqueBooksMap = new HashMap<>();
        for (Book book : unreadBooks) {
            String normalizedTitle = normalizeTitle(book.getTitle());
            if (!uniqueBooksMap.containsKey(normalizedTitle)) {
                uniqueBooksMap.put(normalizedTitle, book);
            } else {
                // 이미 있는 책보다 현재 책의 제목이 더 짧으면 교체 (원본에 가까울 확률 높음)
                Book existingBook = uniqueBooksMap.get(normalizedTitle);
                if (book.getTitle().length() < existingBook.getTitle().length()) {
                    uniqueBooksMap.put(normalizedTitle, book);
                }
            }
        }

        List<Book> candidateBooks = new ArrayList<>(uniqueBooksMap.values());

        // 4. 독후감 여부에 따른 전략 분기
        if (reports.isEmpty()) {
            return recommendForNewUser(candidateBooks);
        } else {
            return recommendForExistingUser(user, reports, candidateBooks);
        }
    }

    /**
     * 책 제목 정규화
     * - 괄호 및 괄호 안의 내용 제거
     * - 앞뒤 공백 제거
     * 예: "불편한 편의점 (벚꽃 에디션)" -> "불편한 편의점"
     */
    private String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        // 괄호 (...) 또는 [...] 및 그 안의 내용 제거
        return title.replaceAll("\\(.*?\\)|\\[.*?\\]", "").trim();
    }

    /**
     * 신규 사용자 추천 (독후감 0개)
     * - 전체 책 중 랜덤 3권 선택
     * - 매번 다른 책 추천
     */
    private List<RecommendationResponse> recommendForNewUser(List<Book> candidateBooks) {
        log.info("신규 사용자 추천: 랜덤 베스트셀러 3권");

        if (candidateBooks.size() <= 3) {
            return candidateBooks.stream()
                    .map(book -> RecommendationResponse.of(book, "인기 도서 추천"))
                    .collect(Collectors.toList());
        }

        // 랜덤하게 3권 선택
        Collections.shuffle(candidateBooks, random);
        return candidateBooks.stream()
                .limit(3)
                .map(book -> RecommendationResponse.of(book, "베스트셀러 추천"))
                .collect(Collectors.toList());
    }

    /**
     * 기존 사용자 추천 (독후감 1개 이상)
     * - TF-IDF + 사용자 성향 기반 점수 계산
     * - 상위 10권 중 랜덤 3권 선택 → 매번 다른 결과
     */
    private List<RecommendationResponse> recommendForExistingUser(
            User user, List<Report> reports, List<Book> candidateBooks) {

        log.info("기존 사용자 추천: TF-IDF + 성향 기반");

        // 1. 사용자 프로필 생성 (키워드 통합)
        List<String> userKeywords = new ArrayList<>();
        for (Report report : reports) {
            String keywords = report.getBook().getKeywords();
            if (keywords != null && !keywords.isEmpty()) {
                userKeywords.addAll(Arrays.asList(keywords.split(",")));
            }
        }

        // 2. 키워드가 없으면 신규 사용자 로직 사용
        if (userKeywords.isEmpty()) {
            log.warn("독후감은 있지만 키워드 없음 → 신규 사용자 로직 적용");
            return recommendForNewUser(candidateBooks);
        }

        // 3. TF-IDF 계산
        Map<Book, Double> bookScores = calculateTFIDFScores(userKeywords, candidateBooks);

        // 4. 사용자 성향 기반 가중치 적용
        Map<Book, Double> finalScores = applyUserPersonalityWeight(bookScores, user, reports);

        // 5. 전체 후보군 정렬 (점수 내림차순)
        List<Book> sortedCandidates = finalScores.entrySet().stream()
                .sorted(Map.Entry.<Book, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // 6. Top 10 (Fit) 선정
        List<Book> top10Books = sortedCandidates.stream()
                .limit(10)
                .collect(Collectors.toList());

        // 7. 나머지에서 5권 랜덤 (Discovery) 선정
        List<Book> remainingBooks = sortedCandidates.stream()
                .skip(10)
                .collect(Collectors.toList());

        Collections.shuffle(remainingBooks, random);
        List<Book> discoveryBooks = remainingBooks.stream()
                .limit(5)
                .collect(Collectors.toList());

        // 8. Pool 생성 (Top 10 + Discovery 5)
        List<Book> pool = new ArrayList<>(top10Books);
        pool.addAll(discoveryBooks);

        // 9. Pool에서 3권 랜덤 선택 (상호 유사도 필터링 적용)
        if (pool.isEmpty()) {
            return recommendForNewUser(candidateBooks);
        }

        Collections.shuffle(pool, random);
        List<Book> selectedBooks = new ArrayList<>();

        for (Book candidate : pool) {
            if (selectedBooks.size() >= 3)
                break;

            boolean isSimilar = false;
            String candidateTitle = normalizeTitle(candidate.getTitle());

            for (Book selected : selectedBooks) {
                String selectedTitle = normalizeTitle(selected.getTitle());
                if (calculateStringSimilarity(candidateTitle, selectedTitle) >= 0.6) {
                    isSimilar = true;
                    break;
                }
            }

            if (!isSimilar) {
                selectedBooks.add(candidate);
            }
        }

        // 만약 3권을 못 채웠다면, 남은 것 중에서 중복되지 않게 채움 (유사도 무시)
        if (selectedBooks.size() < 3) {
            for (Book candidate : pool) {
                if (selectedBooks.size() >= 3)
                    break;
                if (!selectedBooks.contains(candidate)) {
                    selectedBooks.add(candidate);
                }
            }
        }

        // 10. 응답 생성
        return selectedBooks.stream()
                .map(book -> {
                    double score = finalScores.get(book);
                    // Top 10에 포함되면 Fit, 아니면 Discovery
                    boolean isFit = top10Books.contains(book);
                    String reason = generateReason(score, isFit);
                    return RecommendationResponse.of(book, reason);
                })
                .collect(Collectors.toList());
    }

    /**
     * TF-IDF 기반 유사도 점수 계산
     */
    private Map<Book, Double> calculateTFIDFScores(List<String> userKeywords, List<Book> candidateBooks) {
        // 전체 문서 집합
        List<List<String>> documents = new ArrayList<>();
        documents.add(userKeywords); // Doc 0: User Profile

        for (Book book : candidateBooks) {
            String k = book.getKeywords();
            if (k != null && !k.isEmpty()) {
                documents.add(Arrays.asList(k.split(",")));
            } else {
                documents.add(new ArrayList<>());
            }
        }

        // Vocabulary 구축
        Set<String> vocabulary = new HashSet<>();
        for (List<String> doc : documents) {
            vocabulary.addAll(doc);
        }
        List<String> vocabList = new ArrayList<>(vocabulary);

        // IDF 계산
        Map<String, Double> idfMap = new HashMap<>();
        int totalDocs = documents.size();
        for (String term : vocabList) {
            int docsWithTerm = 0;
            for (List<String> doc : documents) {
                if (doc.contains(term)) {
                    docsWithTerm++;
                }
            }
            double idf = Math.log((double) totalDocs / (docsWithTerm + 1)); // smoothing
            idfMap.put(term, idf);
        }

        // TF-IDF 벡터 생성
        List<double[]> vectors = new ArrayList<>();
        for (List<String> doc : documents) {
            double[] vector = new double[vocabList.size()];
            for (int i = 0; i < vocabList.size(); i++) {
                String term = vocabList.get(i);
                double tf = (double) Collections.frequency(doc, term);
                vector[i] = tf * idfMap.get(term);
            }
            vectors.add(vector);
        }

        double[] userVector = vectors.get(0);

        // 유사도 계산
        Map<Book, Double> scores = new HashMap<>();
        for (int i = 0; i < candidateBooks.size(); i++) {
            Book book = candidateBooks.get(i);
            double[] bookVector = vectors.get(i + 1); // +1 because 0 is user
            double similarity = cosineSimilarity(userVector, bookVector);
            scores.put(book, similarity * 100); // 백분율로 변환
        }

        return scores;
    }

    /**
     * 사용자 성향 기반 가중치 적용
     * - PHILOSOPHER: 논리적 키워드 가중치 증가
     * - EMPATH: 감정적 키워드 가중치 증가
     * - ACTIVIST: 행동 키워드 가중치 증가
     */
    private Map<Book, Double> applyUserPersonalityWeight(
            Map<Book, Double> baseScores, User user, List<Report> reports) {

        if (reports.isEmpty()) {
            return baseScores;
        }

        // 사용자의 평균 성향 타입 분석
        Map<String, Integer> typeCount = new HashMap<>();
        for (Report report : reports) {
            String type = report.getAnalysisType();
            if (type != null) {
                typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
            }
        }

        // 가장 많은 성향 타입 찾기
        String dominantType = typeCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("READER");

        // 성향에 따른 키워드 가중치 (간단한 예시)
        Map<Book, Double> weightedScores = new HashMap<>();
        for (Map.Entry<Book, Double> entry : baseScores.entrySet()) {
            Book book = entry.getKey();
            double baseScore = entry.getValue();

            // 책의 키워드에 성향 관련 단어가 있으면 가중치 부여
            double weight = 1.0;
            String keywords = book.getKeywords();
            if (keywords != null) {
                switch (dominantType) {
                    case "PHILOSOPHER":
                    case "ANALYST":
                        if (keywords.contains("철학") || keywords.contains("사상") ||
                                keywords.contains("논리") || keywords.contains("분석")) {
                            weight = 1.2;
                        }
                        break;
                    case "EMPATH":
                        if (keywords.contains("감정") || keywords.contains("사랑") ||
                                keywords.contains("우정") || keywords.contains("가족")) {
                            weight = 1.2;
                        }
                        break;
                    case "ACTIVIST":
                        if (keywords.contains("행동") || keywords.contains("변화") ||
                                keywords.contains("실천") || keywords.contains("도전")) {
                            weight = 1.2;
                        }
                        break;
                }
            }

            weightedScores.put(book, baseScore * weight);
        }

        return weightedScores;
    }

    /**
     * 추천 이유 생성
     */
    /**
     * 추천 이유 생성
     */
    private String generateReason(double score, boolean isFit) {
        if (!isFit) {
            return "새로운 취향을 발견해보세요!";
        }
        if (score >= 80) {
            return String.format("당신의 취향과 %.1f%% 일치", score);
        } else if (score >= 60) {
            return String.format("당신에게 추천 (%.1f%% 일치)", score);
        } else {
            return String.format("이런 책은 어떠세요? (%.1f%% 일치)", score);
        }
    }

    /**
     * 코사인 유사도 계산
     */
    private double cosineSimilarity(double[] v1, double[] v2) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            normA += Math.pow(v1[i], 2);
            normB += Math.pow(v2[i], 2);
        }
        if (normA == 0 || normB == 0) {
            return 0.0;
        }
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
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

    @lombok.Value
    private static class BookSimilarity {
        Book book;
        double similarity;
    }
}
