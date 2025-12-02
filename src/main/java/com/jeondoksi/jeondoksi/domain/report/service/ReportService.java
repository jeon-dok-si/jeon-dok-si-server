package com.jeondoksi.jeondoksi.domain.report.service;

import com.jeondoksi.jeondoksi.core.nlp.NlpAnalyzer;
import com.jeondoksi.jeondoksi.domain.book.entity.Book;
import com.jeondoksi.jeondoksi.domain.book.repository.BookRepository;
import com.jeondoksi.jeondoksi.domain.report.dto.ReportRequest;
import com.jeondoksi.jeondoksi.domain.report.dto.ReportResponse;
import com.jeondoksi.jeondoksi.domain.report.entity.Report;
import com.jeondoksi.jeondoksi.domain.report.repository.ReportRepository;
import com.jeondoksi.jeondoksi.domain.user.entity.User;
import com.jeondoksi.jeondoksi.domain.user.repository.UserRepository;
import com.jeondoksi.jeondoksi.global.error.BusinessException;
import com.jeondoksi.jeondoksi.global.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

        private final ReportRepository reportRepository;
        private final UserRepository userRepository;
        private final BookRepository bookRepository;
        private final com.jeondoksi.jeondoksi.domain.book.repository.BookAiSampleRepository bookAiSampleRepository;
        private final com.jeondoksi.jeondoksi.domain.gamification.service.CharacterService characterService;
        private final NlpAnalyzer nlpAnalyzer;

        // 유사도 임계치 상수 (후하게 설정)
        private static final double AI_SIMILARITY_THRESHOLD = 0.85; // AI 샘플/자가복제 유사도 (원래 0.6 -> 0.85)
        private static final double MIN_BOOK_SIMILARITY = 0.02; // 책 줄거리 최소 유사도 (원래 0.05 -> 0.02)
        private static final double AI_PROBABILITY_THRESHOLD = 0.9; // AI 작성 확률 임계치 (원래 0.85 -> 0.9)
        private static final int MIN_CONTENT_LENGTH = 50; // 최소 글자 수
        private static final String REPETITION_PATTERN = "(.)\\1{9,}"; // 동일 문자 10회 이상 반복

        @Lazy
        private final com.jeondoksi.jeondoksi.domain.quiz.client.OpenAiClient openAiClient;

        @Transactional
        public com.jeondoksi.jeondoksi.domain.report.dto.ReportDetailResponse submitReport(Long userId,
                        ReportRequest request) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                Book book = bookRepository.findById(request.getIsbn())
                                .orElseThrow(() -> new BusinessException(ErrorCode.BOOK_NOT_FOUND));

                String content = request.getContent();

                // ============================================
                // Step 0: 전처리 및 기초 유효성 검사
                // ============================================
                validatePreProcessing(content);

                // ============================================
                // Step 1: AI 생성 여부 탐지
                // ============================================
                validateAiGeneration(content);

                // ============================================
                // Step 2: 앙상블 적대적 검증 (유사도 검사)
                // ============================================
                validateSimilarity(content, book, user);

                // ============================================
                // 검증 통과 - NLP 분석 및 저장
                // ============================================
                NlpAnalyzer.AnalysisResult analysis = nlpAnalyzer.analyze(content);
                String feedback = openAiClient.generateFeedback(content, book.getTitle());

                Report report = Report.builder()
                                .user(user)
                                .book(book)
                                .content(content)
                                .build();

                report.updateAnalysisResult(
                                analysis.getLogicScore(),
                                analysis.getEmotionScore(),
                                analysis.getActionScore(),
                                analysis.getType(),
                                feedback);

                // 사용자 스탯 업데이트 및 경험치 지급
                user.updateStats(analysis.getLogicScore(), analysis.getEmotionScore(), analysis.getActionScore());
                user.addPoint(50);
                characterService.gainExpForEquippedCharacter(user, 50);

                // 검증 통과 시 자동 승인
                report.approve();

                reportRepository.save(report);
                return com.jeondoksi.jeondoksi.domain.report.dto.ReportDetailResponse.from(report);
        }

        /**
         * Step 0: 전처리 검증
         * - 최소 길이 검사 (50자)
         * - 반복 문자 검사 (동일 문자 10회 이상)
         */
        private void validatePreProcessing(String content) {
                // 공백 제외 길이 검사
                String contentWithoutSpaces = content.replaceAll("\\s", "");
                if (contentWithoutSpaces.length() < MIN_CONTENT_LENGTH) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
                }

                // 반복 문자 검사
                if (content.matches(".*" + REPETITION_PATTERN + ".*")) {
                        throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
                }
        }

        /**
         * Step 1: AI 생성 여부 탐지
         * - OpenAI API를 통해 AI 작성 확률 분석
         * - 임계치(0.9) 이상이면 차단
         * - API 실패 시 통과 처리 (Fallback)
         */
        private void validateAiGeneration(String content) {
                try {
                        double aiProbability = openAiClient.detectAiGenerated(content);
                        if (aiProbability >= AI_PROBABILITY_THRESHOLD) {
                                throw new BusinessException(ErrorCode.AI_GENERATED_CONTENT_DETECTED);
                        }
                } catch (BusinessException e) {
                        // BusinessException은 그대로 throw
                        throw e;
                } catch (Exception e) {
                        // 기타 예외는 로그만 남기고 통과 (UX 우선)
                        // OpenAiClient의 detectAiGenerated에서 이미 0.0 반환하므로 여기 도달 안 함
                }
        }

        /**
         * Step 2: 앙상블 적대적 검증 (유사도 검사)
         * 1. AI 샘플과의 유사도 (0.85 이상 차단)
         * 2. 책 줄거리와의 유사도 (0.02 미만 차단, 0.85 이상 차단)
         * 3. 최근 독후감과의 유사도 (자가 복제 검사)
         * - 독후감이 없으면: AI 샘플 검증만 수행
         * - 독후감이 1~3개 있으면: 해당 개수만큼만 비교
         */
        private void validateSimilarity(String content, Book book, User user) {
                // 1. AI 샘플과 비교
                List<com.jeondoksi.jeondoksi.domain.book.entity.BookAiSample> samples = bookAiSampleRepository
                                .findAllByBook(book);
                for (com.jeondoksi.jeondoksi.domain.book.entity.BookAiSample sample : samples) {
                        double similarity = nlpAnalyzer.calculateSimilarity(content, sample.getContent());
                        if (similarity >= AI_SIMILARITY_THRESHOLD) {
                                throw new BusinessException(ErrorCode.AI_GENERATED_CONTENT_DETECTED);
                        }
                }

                // 2. 책 줄거리와 비교
                if (book.getDescription() != null && !book.getDescription().isEmpty()) {
                        double bookSimilarity = nlpAnalyzer.calculateSimilarity(content, book.getDescription());

                        // 너무 유사 (표절)
                        if (bookSimilarity >= AI_SIMILARITY_THRESHOLD) {
                                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
                        }

                        // 너무 무관 (스팸)
                        if (bookSimilarity < MIN_BOOK_SIMILARITY) {
                                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
                        }
                }

                // 3. 최근 독후감과 비교 (자가 복제 검사)
                // 신규 사용자는 독후감이 없을 수 있으므로 유연하게 처리
                List<Report> recentReports = reportRepository.findTop3ByUserOrderByCreatedAtDesc(user);

                if (recentReports.isEmpty()) {
                        // 독후감이 하나도 없는 경우: AI 샘플 검증만으로 충분 (신규 사용자)
                        // 이미 1번에서 AI 샘플 검증 완료했으므로 추가 검증 불필요
                        return;
                }

                // 독후감이 있는 경우: 있는 만큼만 비교 (1개, 2개, 또는 3개)
                for (Report pastReport : recentReports) {
                        double similarity = nlpAnalyzer.calculateSimilarity(content, pastReport.getContent());
                        if (similarity >= AI_SIMILARITY_THRESHOLD) {
                                throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
                        }
                }
        }

        public List<ReportResponse> getMyReports(Long userId) {
                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

                return reportRepository.findAllByUser(user).stream()
                                .map(ReportResponse::from)
                                .collect(Collectors.toList());
        }

        public com.jeondoksi.jeondoksi.domain.report.dto.ReportDetailResponse getReportDetail(Long reportId) {
                Report report = reportRepository.findById(reportId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.REPORT_NOT_FOUND));
                return com.jeondoksi.jeondoksi.domain.report.dto.ReportDetailResponse.from(report);
        }
}
