# 전독시 (Jeondoksi) - AI 기반 독서 관리 플랫폼

> 독후감 작성부터 AI 추천, 게임화까지  
> NLP 분석과 TF-IDF 기반 추천 시스템을 갖춘 스마트 독서 플랫폼

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0%2B-blue.svg)](https://www.mysql.com/)

---

## 프로젝트 개요

전독시(Jeondoksi)는 AI 기술을 활용한 독서 관리 플랫폼입니다. 사용자의 독후감을 NLP로 분석하여 독서 성향을 파악하고, TF-IDF 알고리즘으로 맞춤형 도서를 추천합니다.

### 주요 기능

#### 1. 3단계 독후감 검증 시스템
- Step 0: 전처리 (최소 길이, 반복 문자)
- Step 1: AI 작성 탐지 (OpenAI API)
- Step 2: 유사도 검증 (AI 샘플, 책 줄거리, 자가 복제)

#### 2. NLP 기반 독서 성향 분석
- 3가지 지표: Logic, Emotion, Action
- 5가지 성향 타입: PHILOSOPHER, ANALYST, EMPATH, ACTIVIST, READER
- Komoran 형태소 분석기 활용

#### 3. TF-IDF 도서 추천 시스템
- 사용자 독후감 키워드 분석
- 책 설명 키워드와 코사인 유사도 계산
- 사용자 성향 기반 가중치 적용
- 상위 10권 중 랜덤 3권 추천 (매번 다른 결과)
- 신규 사용자: 랜덤 베스트셀러 3권

#### 4. AI 퀴즈 자동 생성 및 유연한 채점
- OpenAI API를 통한 퀴즈 자동 생성
- 객관식, OX, 단답형 문제
- 50점 이상 통과 시 100 XP 획득
- 단답형 유연 채점: 유사도 80% 이상 정답, 부분 점수 부여
- OX/객관식은 엄격 채점 (프론트 버튼 처리)

#### 5. 게임화 요소
- 레벨업 시스템 (XP 누적)
- 아이템 가챠 (Common, Rare, Epic)
- 인벤토리 및 아이템 장착

---

## 기술 스택

### Backend
- Framework: Spring Boot 4.0.0
- Language: Java 21
- ORM: JPA (Hibernate)
- Security: Spring Security + JWT
- API Documentation: Swagger (SpringDoc OpenAPI 3.0)

### Database
- RDBMS: MySQL 8.0+
- Charset: utf8mb4
- Naming: snake_case

### External APIs
- OpenAI: AI 탐지 및 퀴즈 생성
- Naver Book API: 도서 검색
- Aladin API: 보조 도서 정보

### NLP & ML
- Komoran: 한국어 형태소 분석
- Custom NLP Engine: 논리/감정/행동 지표 산출
- TF-IDF: 문서 유사도 및 추천

---

## 프로젝트 구조

```
jeondoksi/
├── src/main/java/com/jeondoksi/jeondoksi/
│   ├── core/
│   │   └── nlp/
│   │       ├── NlpAnalyzer.java
│   │       ├── TextSimilarity.java
│   │       ├── TextComplexityMetric.java
│   │       └── SentimentDictionary.java
│   │
│   ├── domain/
│   │   ├── auth/
│   │   ├── book/
│   │   │   ├── entity/Book.java
│   │   │   ├── entity/BookAiSample.java
│   │   │   └── service/BookService.java
│   │   ├── report/
│   │   │   ├── entity/Report.java
│   │   │   └── service/ReportService.java
│   │   ├── quiz/
│   │   │   └── client/OpenAiClient.java
│   │   ├── recommendation/
│   │   │   └── service/RecommendationService.java
│   │   ├── gamification/
│   │   └── user/
│   │
│   └── global/
│       ├── config/
│       │   ├── SecurityConfig.java
│       │   ├── KomoranConfig.java
│       │   └── SwaggerConfig.java
│       └── error/
│
├── src/main/resources/
│   ├── application.yml
│   └── data.sql
│
├── POSTMAN_GUIDE.md
├── SETUP_GUIDE.md
└── README.md
```

---

## 빠른 시작

### 사전 요구사항

- Java 21+
- MySQL 8.0+
- Gradle 8.5+
- OpenAI API Key (필수)
- Naver Book API Key (권장)

### 설치 및 실행

```bash
# 저장소 클론
git clone https://github.com/yourusername/jeondoksi.git
cd jeondoksi

# MySQL 데이터베이스 생성
mysql -u root -p
CREATE DATABASE jeondoksi CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
exit;

# 환경 설정 (API 키 설정)
cd src/main/resources
cp application-example.yml application.yml

# application.yml 편집
# 다음 항목들을 실제 값으로 변경:
# - spring.datasource.username
# - spring.datasource.password
# - jwt.secret (256비트 이상)
# - openai.api-key (필수)
# - naver.client-id
# - naver.client-secret

# 자세한 설정 방법: SETUP_GUIDE.md 참고

# 빌드 및 실행
./gradlew clean build
./gradlew bootRun

# 서버 시작: http://localhost:8080
# Swagger UI: http://localhost:8080/swagger-ui/index.html
```

### 초기 데이터 확인

```sql
SELECT COUNT(*) FROM book;

# 테스트 계정 생성 (Postman 사용)
POST http://localhost:8080/api/v1/auth/signup
{
  "email": "test@jeondoksi.com",
  "password": "password123!",
  "nickname": "독서왕"
}
```

---

## API 사용 가이드

### 인증이 필요한 모든 요청

```http
Authorization: Bearer <JWT_TOKEN>
```

### 주요 엔드포인트

| 기능 | Method | Endpoint | 인증 |
|------|--------|----------|------|
| 회원가입 | POST | `/api/v1/auth/signup` | ❌ |
| 로그인 | POST | `/api/v1/auth/login` | ❌ |
| 내 정보 조회 | GET | `/api/v1/users/me` | ✅ |
| 도서 검색 | GET | `/api/v1/books/search?query={query}` | ✅ |
| 독후감 작성 | POST | `/api/v1/reports` | ✅ |
| 독후감 조회 | GET | `/api/v1/reports/me` | ✅ |
| 퀴즈 조회 | GET | `/api/v1/quizzes/{isbn}` | ✅ |
| 퀴즈 제출 | POST | `/api/v1/quizzes/submit` | ✅ |
| 가챠 뽑기 | POST | `/api/v1/gamification/gacha` | ✅ |
| 인벤토리 | GET | `/api/v1/gamification/inventory` | ✅ |
| AI 추천 | GET | `/api/v1/recommendations` | ✅ |

완전한 API 테스트 가이드: [POSTMAN_GUIDE.md](POSTMAN_GUIDE.md)

---

## 독후감 검증 파이프라인

### Step 0: 전처리
```
최소 길이: 50자 (공백 제외)
반복 문자: 동일 문자 10회 이상 차단
```

### Step 1: AI 탐지
```
OpenAI API 호출
AI 작성 확률 >= 0.9 차단
API 실패 시 통과 (Fallback)
```

### Step 2: 유사도 검증
```
AI 샘플과 비교 (>=0.85 차단)
책 줄거리 비교 (표절: >=0.85, 무관: <0.02)
최근 독후감 3건 비교 (>=0.85 차단)
  - 독후감 0개: AI 샘플만 검증 (신규 사용자)
  - 독후감 1-3개: 있는 만큼만 비교
```

### NLP 분석
```
logicScore: 논리적 구조 (접속사, 문장 복잡도)
emotionScore: 감정 표현 (긍정/부정 어휘)
actionScore: 행동 지향성 (동사, 청유형)
analysisType: 5가지 성향 분류
```

---

## 데이터베이스 설계

### user
```sql
user_id (PK)
email (UNIQUE)
password (BCrypt)
nickname
level, exp
logic_stat, emotion_stat, action_stat
```

### book
```sql
isbn (PK)
title, author, thumbnail
description
keywords (TEXT)
logic_tendency, emotion_tendency, action_tendency
```

### book_ai_sample
```sql
sample_id (PK)
book_isbn (FK)
style_prompt (Logical/Emotional/Action)
content
```

### report
```sql
report_id (PK)
user_id (FK)
book_isbn (FK)
content (TEXT)
logic_score, emotion_score, action_score
analysis_type (ENUM)
status (PENDING/APPROVED/REJECTED)
```

### quiz
```sql
quiz_id (PK)
book_isbn (FK)
questions (JSON)
```

### inventory
```sql
inven_id (PK)
user_id (FK)
item_name, rarity, category
equipped (BOOLEAN)
```

---

## 핵심 알고리즘

### TF-IDF 추천 시스템

```java
// 1. 사용자 프로필 생성
List<String> userKeywords = getUserKeywordsFromReports(userId);

// 2. Vocabulary 구축
Set<String> vocabulary = buildVocabulary(allBooks, userKeywords);

// 3. IDF 계산
Map<String, Double> idf = calculateIDF(vocabulary, allBooks);

// 4. TF-IDF 벡터 생성
Map<String, Double> userVector = calculateTFIDF(userKeywords, idf);
Map<String, Double> bookVector = calculateTFIDF(bookKeywords, idf);

// 5. 코사인 유사도
double similarity = cosineSimilarity(userVector, bookVector);

// 6. 상위 10권 중 랜덤 3권 반환 (이미 읽은 책 제외)
return topK(similarities, 10).shuffle().limit(3);
```

### NLP 성향 분석

```java
// Komoran 형태소 분석
List<Token> tokens = komoran.analyze(content);

// 지표 계산
logicScore = TextComplexityMetric.calculate(tokens);
emotionScore = SentimentDictionary.analyze(tokens);
actionScore = countVerbs(tokens) + countImperativeEndings(tokens);

// 성향 분류
if (logicScore > 70 && emotionScore > 70) return PHILOSOPHER;
if (logicScore > 70) return ANALYST;
if (emotionScore > 70) return EMPATH;
if (actionScore > 70) return ACTIVIST;
return READER;
```

---

## 보안

### JWT 인증
- Access Token 만료: 24시간
- HS256 알고리즘
- Bearer 스킴

### 비밀번호
- BCrypt 해싱
- Salt 자동 생성

### API 보호
- Spring Security FilterChain
- `/auth/**` 공개, 나머지 인증 필요

---

## 성능

### 응답 시간
- 일반 조회: ~100ms
- 도서 검색 (외부 API): ~500ms
- 독후감 분석 (NLP): ~300ms
- AI 탐지 (OpenAI): ~2-3초
- 퀴즈 생성 (OpenAI): ~5-10초
- 추천 계산 (TF-IDF): ~200ms

### 최적화 전략
- 퀴즈 DB 캐싱 (재생성 방지)
- 도서 정보 자동 저장 (검색 시)
- JPA 쿼리 최적화

---

## 향후 개선 사항

- Redis 캐싱 (추천 결과, 퀴즈)
- ElasticSearch (도서 검색 개선)
- WebSocket (실시간 알림)
- S3 (썸네일 이미지 저장)
- Docker & Kubernetes (배포)
- GraphQL (유연한 API)
- 소셜 로그인 (OAuth 2.0)
- 독서 통계 대시보드

---

## 기여

기여는 언제나 환영합니다!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

---

## 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다.

---

## 문의

프로젝트 관련 문의사항은 GitHub Issues를 이용해주세요.
