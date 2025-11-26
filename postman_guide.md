# 📚 Jeondoksi API - 완벽한 Postman 테스트 가이드

> **전독시(Jeondoksi)** - AI 기반 독서 관리 및 독후감 분석 플랫폼  
> 이 가이드는 모든 API를 단계별로 완벽하게 테스트할 수 있도록 작성되었습니다.

---

## 📋 목차
1. [사전 준비](#1-사전-준비)
2. [인증 플로우](#2-인증-플로우-authentication)
3. [도서 검색](#3-도서-검색-book-search)
4. [독후감 제출 및 조회](#4-독후감-제출-및-조회-report)
5. [퀴즈 시스템](#5-퀴즈-시스템-quiz)
6. [게임화 기능](#6-게임화-기능-gamification)
7. [AI 추천](#7-ai-추천-recommendation)
8. [에러 처리](#8-에러-처리)
9. [고급 테스트 시나리오](#9-고급-테스트-시나리오)

---

## 1. 사전 준비

### 1.1 서버 실행
```bash
# IntelliJ에서 JeondoksiApplication 실행
# 또는 터미널에서:
./gradlew bootRun
```
- **기본 포트**: `8080`
- **베이스 URL**: `http://localhost:8080`

### 1.2 Postman 설정
1. **Postman 설치**: [다운로드](https://www.postman.com/downloads/)
2. **환경 변수 설정** (권장):
   - `baseUrl`: `http://localhost:8080`
   - `token`: `<로그인 후 설정>`

### 1.3 필수 확인 사항
- ✅ MySQL 8.0+ 실행 중
- ✅ `application.yml`에 OpenAI API 키 설정
- ✅ `application.yml`에 Naver API 키 설정
- ✅ 데이터베이스 초기화 완료 (`data.sql` 적용)

---

## 2. 인증 플로우 (Authentication)

### 2.1 회원가입 (Sign Up)

**📌 첫 번째 요청 - 사용자 등록**

```http
POST http://localhost:8080/api/v1/auth/signup
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "test@jeondoksi.com",
  "password": "password123!",
  "nickname": "독서왕김독자"
}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "email": "test@jeondoksi.com",
    "nickname": "독서왕김독자"
  },
  "message": null,
  "errorCode": null
}
```

**❌ 실패 케이스:**
- `U001`: 이미 존재하는 이메일
- `C002`: 잘못된 입력값 (이메일 형식, 비밀번호 길이 등)

---

### 2.2 로그인 (Login)

**📌 JWT 토큰 발급**

```http
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json
```

**Request Body:**
```json
{
  "email": "test@jeondoksi.com",
  "password": "password123!"
}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzAwMDAwMDAwLCJleHAiOjE3MDAwODY0MDB9.xxxxx",
    "tokenType": "Bearer"
  },
  "message": null,
  "errorCode": null
}
```

**🔑 중요: 토큰 저장**
```
1. accessToken 복사
2. Postman 환경 변수에 저장: {{token}}
3. 이후 모든 요청의 Header에 포함
```

---

### 2.3 인증 헤더 설정

**모든 보호된 엔드포인트 요청 시 필수:**

| Key | Value |
|-----|-------|
| `Authorization` | `Bearer {{token}}` |

**Postman 설정 방법:**
1. Headers 탭 클릭
2. Key: `Authorization`
3. Value: `Bearer eyJhbGciOiJIUzI1NiJ9...` (공백 주의!)

---

## 3. 도서 검색 (Book Search)

### 3.1 도서 검색 (Naver Book API)

```http
GET http://localhost:8080/api/v1/books/search?query=채식주의자
Authorization: Bearer {{token}}
```

**Query Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `query` | String | ✅ | 검색할 도서명 또는 저자명 |

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "isbn": "9788936434595",
      "title": "채식주의자",
      "author": "한강",
      "thumbnail": "https://...",
      "description": "인터내셔널 부커상...",
      "keywords": "인터내셔널 부커상, 산클레멘테 문학상, 상처받은 영혼, 식물적 상상력, 가부장의 폭력"
    }
  ],
  "message": null,
  "errorCode": null
}
```

**💡 TIP:**
- 검색 후 `isbn` 값을 복사해두세요 (독후감 작성 시 필요)
- 검색 시 자동으로 DB에 저장됩니다
- `keywords`는 TF-IDF 추천에 사용됩니다

---

## 4. 독후감 제출 및 조회 (Report)

### 4.1 독후감 작성 (Submit Report)

**📌 가장 중요한 기능 - 3단계 검증 파이프라인 적용**

```http
POST http://localhost:8080/api/v1/reports
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "isbn": "9788936434595",
  "content": "한강 작가의 '채식주의자'를 읽고 깊은 감명을 받았습니다. 주인공 영혜가 겪는 내적 갈등과 사회의 억압 구조가 매우 인상 깊었습니다. 특히 식물이 되고자 하는 욕망이 단순히 비정상적인 것이 아니라, 인간 본성에 대한 깊은 성찰을 담고 있다고 느꼈습니다. 작가의 섬세한 문체와 상징적 표현이 돋보였으며, 가부장제의 폭력성을 날카롭게 비판하는 동시에 예술적 아름다움을 잃지 않았습니다."
}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": 1,  // Report ID
  "message": null,
  "errorCode": null
}
```

**🔍 백그라운드 처리:**
1. **Step 0: 전처리 검증**
   - 최소 길이 50자 검사 (공백 제외)
   - 반복 문자 검사 (10회 이상 반복 차단)

2. **Step 1: AI 생성 탐지**
   - OpenAI API로 AI 작성 확률 분석
   - 확률 ≥ 0.9 차단

3. **Step 2: 유사도 검증**
   - AI 샘플과 비교 (≥0.85 차단)
   - 책 줄거리와 비교 (표절/무관 차단)
   - 최근 독후감 3건과 비교 (자가 복제 차단)

4. **NLP 분석 수행**
   - `logicScore`: 논리적 구조 분석
   - `emotionScore`: 감정 표현 분석
   - `actionScore`: 행동 지향성 분석
   - `analysisType`: PHILOSOPHER, ANALYST, EMPATH, ACTIVIST, READER

5. **사용자 스탯 업데이트 및 경험치 지급**
   - 50 XP 획득
   - 레벨업 가능

---

### 4.2 독후감 작성 실패 케이스

#### ❌ Case 1: 너무 짧은 글
```json
{
  "isbn": "9788936434595",
  "content": "좋은 책이었습니다."  // 50자 미만
}
```
**Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "message": "잘못된 입력값입니다.",
  "errorCode": "C002"
}
```

#### ❌ Case 2: 반복 문자
```json
{
  "isbn": "9788936434595",
  "content": "정말 재미있었습니다ㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋㅋ 추천합니다"
}
```
**Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "message": "잘못된 입력값입니다.",
  "errorCode": "C002"
}
```

#### ❌ Case 3: AI가 작성한 글
```json
{
  "isbn": "9788936434595",
  "content": "This novel presents a thought-provoking exploration of societal norms and individual identity. The protagonist's journey challenges conventional perspectives..."
}
```
**Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "message": "AI가 작성한 것으로 의심되는 내용이 감지되었습니다.",
  "errorCode": "R002"
}
```

#### ❌ Case 4: 책 줄거리 복사
```json
{
  "isbn": "9788936434595",
  "content": "인터내셔널 부커상을 수상하며 한국문학의 입지를 한단계 확장시킨 한강의 장편소설..."  // description 그대로 복사
}
```
**Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "message": "잘못된 입력값입니다.",
  "errorCode": "C002"
}
```

#### ❌ Case 5: 자가 복제 (이전 독후감 재사용)
**Response (400 Bad Request):**
```json
{
  "success": false,
  "data": null,
  "message": "잘못된 입력값입니다.",
  "errorCode": "C002"
}
```

---

### 4.3 내 독후감 조회 (Get My Reports)

```http
GET http://localhost:8080/api/v1/reports/me
Authorization: Bearer {{token}}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "reportId": 1,
      "bookTitle": "채식주의자",
      "content": "한강 작가의 '채식주의자'를 읽고...",
      "logicScore": 75,
      "emotionScore": 85,
      "actionScore": 60,
      "analysisType": "PHILOSOPHER",
      "status": "APPROVED",
      "createdAt": "2025-01-27T01:00:00"
    }
  ],
  "message": null,
  "errorCode": null
}
```

---

## 5. 퀴즈 시스템 (Quiz)

### 5.1 퀴즈 조회/생성 (Get Quiz)

```http
GET http://localhost:8080/api/v1/quizzes/9788936434595
Authorization: Bearer {{token}}
```

**💡 동작 방식:**
- 퀴즈가 이미 있으면: DB에서 조회
- 퀴즈가 없으면: OpenAI API로 자동 생성 (5~10초 소요)

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "quizId": 1,
    "bookTitle": "채식주의자",
    "questions": [
      {
        "questionId": 1,
        "questionNo": 1,
        "type": "MULTIPLE",
        "question": "영혜의 남편은 누구인가?",
        "options": ["철수", "영호", "민호", "정현"]
      },
      {
        "questionId": 2,
        "questionNo": 2,
        "type": "OX",
        "question": "영혜는 채식을 시작했다.",
        "options": ["O", "X"]
      },
      {
        "questionId": 3,
        "questionNo": 3,
        "type": "SHORT",
        "question": "작가의 이름은?",
        "options": []
      }
    ]
  },
  "message": null,
  "errorCode": null
}
```

---

### 5.2 퀴즈 제출 (Submit Quiz)

```http
POST http://localhost:8080/api/v1/quizzes/submit
Authorization: Bearer {{token}}
Content-Type: application/json
```

**Request Body:**
```json
{
  "quizId": 1,
  "answers": [
    { "questionId": 1, "answer": "정현" },
    { "questionId": 2, "answer": "O" },
    { "questionId": 3, "answer": "한강" },
    { "questionId": 4, "answer": "보기2" },
    { "questionId": 5, "answer": "단답형정답" }
  ]
}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "score": 80,
    "isSolved": true,
    "gainedExp": 100,
    "message": "퀴즈를 통과했습니다! 100 XP를 획득했습니다."
  },
  "message": null,
  "errorCode": null
}
```

**📊 점수 산정:**
- 60점 이상: PASS (100 XP 지급)
- 60점 미만: FAIL (0 XP)
- 중복 제출 방지: 같은 퀴즈는 1회만 보상

---

## 6. 게임화 기능 (Gamification)

### 6.1 아이템 뽑기 (Gacha)

```http
POST http://localhost:8080/api/v1/gamification/gacha
Authorization: Bearer {{token}}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": {
    "itemName": "황금 책갈피",
    "rarity": "RARE",
    "category": "BOOKMARK",
    "effect": "독서 속도 +20%",
    "usedXp": 100
  },
  "message": null,
  "errorCode": null
}
```

**🎰 확률:**
- Common (60%): 기본 아이템
- Rare (30%): 희귀 아이템
- Epic (10%): 전설 아이템

**❌ 실패 케이스 (경험치 부족):**
```json
{
  "success": false,
  "data": null,
  "message": "경험치가 부족합니다.",
  "errorCode": "G001"
}
```

---

### 6.2 인벤토리 조회 (Get Inventory)

```http
GET http://localhost:8080/api/v1/gamification/inventory
Authorization: Bearer {{token}}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "invenId": 1,
      "itemName": "황금 책갈피",
      "rarity": "RARE",
      "category": "BOOKMARK",
      "effect": "독서 속도 +20%",
      "equipped": false
    },
    {
      "invenId": 2,
      "itemName": "마법의 독서등",
      "rarity": "EPIC",
      "category": "LIGHT",
      "effect": "집중력 +30%",
      "equipped": true
    }
  ],
  "message": null,
  "errorCode": null
}
```

---

### 6.3 아이템 장착 (Equip Item)

```http
POST http://localhost:8080/api/v1/gamification/inventory/1/equip
Authorization: Bearer {{token}}
```

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": null,
  "message": null,
  "errorCode": null
}
```

**💡 자동 처리:**
- 같은 카테고리의 다른 아이템은 자동 해제
- 예: BOOKMARK 장착 시, 기존 BOOKMARK는 해제

---

## 7. AI 추천 (Recommendation)

### 7.1 TF-IDF 기반 도서 추천

```http
GET http://localhost:8080/api/v1/recommendations
Authorization: Bearer {{token}}
```

**⚠️ 전제 조건:**
- 독후감을 최소 1개 이상 작성해야 합니다
- 독후감의 `keywords`가 추천 알고리즘에 사용됩니다

**✅ 성공 응답 (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "isbn": "9788954699075",
      "title": "도시와 그 불확실한 벽",
      "author": "무라카미 하루키",
      "thumbnail": "https://...",
      "similarity": 0.87,
      "reason": "키워드 매칭: 상상력, 내면, 고독"
    },
    {
      "isbn": "9788954681179",
      "title": "밝은 밤",
      "author": "최은영",
      "thumbnail": "https://...",
      "similarity": 0.82,
      "reason": "키워드 매칭: 가족, 시간, 기억"
    },
    {
      "isbn": "9788954646116",
      "title": "기사단장 죽이기",
      "author": "무라카미 하루키",
      "thumbnail": "https://...",
      "similarity": 0.79,
      "reason": "키워드 매칭: 현실, 상상력, 예술"
    }
  ],
  "message": null,
  "errorCode": null
}
```

**🧠 추천 알고리즘:**
1. 사용자의 모든 독후감에서 `keywords` 추출
2. 전체 책의 `keywords`와 TF-IDF 벡터 생성
3. 코사인 유사도 계산
4. 상위 3권 반환
5. 이미 읽은 책은 제외

---

## 8. 에러 처리

### 8.1 에러 코드 목록

| 코드 | HTTP Status | 메시지 | 설명 |
|------|-------------|--------|------|
| `C001` | 500 | 서버 내부 오류가 발생했습니다 | Internal Server Error |
| `C002` | 400 | 잘못된 입력값입니다 | Validation Error |
| `C003` | 401 | 인증되지 않은 사용자입니다 | Unauthorized |
| `C004` | 403 | 권한이 없습니다 | Forbidden |
| `B001` | 404 | 책을 찾을 수 없습니다 | Book Not Found |
| `U001` | 400 | 이미 존재하는 이메일입니다 | Email Duplication |
| `U002` | 404 | 사용자를 찾을 수 없습니다 | User Not Found |
| `U003` | 400 | 비밀번호가 일치하지 않습니다 | Invalid Password |
| `R001` | 404 | 독후감을 찾을 수 없습니다 | Report Not Found |
| `R002` | 400 | AI가 작성한 것으로 의심되는 내용이 감지되었습니다 | AI Content Detected |
| `Q001` | 404 | 퀴즈를 찾을 수 없습니다 | Quiz Not Found |
| `G001` | 400 | 경험치가 부족합니다 | Not Enough XP |
| `G002` | 404 | 아이템을 찾을 수 없습니다 | Item Not Found |

### 8.2 에러 응답 형식

```json
{
  "success": false,
  "data": null,
  "message": "AI가 작성한 것으로 의심되는 내용이 감지되었습니다.",
  "errorCode": "R002"
}
```

---

## 9. 고급 테스트 시나리오

### 9.1 완전한 사용자 플로우

```
1. 회원가입 → 로그인 → 토큰 획득
2. 도서 검색 ("채식주의자")
3. 독후감 작성 (정상적인 글)
4. 독후감 조회 (NLP 분석 결과 확인)
5. 퀴즈 조회/생성
6. 퀴즈 제출 (100 XP 획득)
7. 가챠 뽑기 (100 XP 소비)
8. 인벤토리 조회
9. 아이템 장착
10. AI 추천 받기
```

### 9.2 독후감 검증 테스트

#### Test Case 1: 신규 사용자 (독후감 0개)
```json
{
  "isbn": "9788936434595",
  "content": "한강의 채식주의자는 현대 한국 문학의 걸작입니다. 주인공 영혜의 내면 세계를 통해 사회의 억압과 개인의 자유에 대한 깊은 성찰을 보여줍니다. 특히 가부장적 폭력과 여성의 주체성에 대한 날카로운 통찰이 인상 깊었습니다."
}
```
**✅ 통과**: AI 샘플만 검증 → PASS

#### Test Case 2: 독후감 1개 있는 사용자
```json
{
  "isbn": "9788954699075",
  "content": "무라카미 하루키의 도시와 그 불확실한 벽을 읽으며 현실과 환상의 경계에 대해 생각해보게 되었습니다. 작가 특유의 몽환적 분위기와 섬세한 심리 묘사가 돋보이는 작품입니다."
}
```
**✅ 통과**: 최근 독후감 1개와 비교 → 유사도 낮음 → PASS

#### Test Case 3: 이전 독후감과 동일
```json
{
  "isbn": "9788954699075",
  "content": "한강의 채식주의자는 현대 한국 문학의 걸작입니다..."  // 위와 동일
}
```
**❌ 차단**: 유사도 0.95 → "C002" 에러

### 9.3 TF-IDF 추천 검증

**시나리오:**
1. "채식주의자" 독후감 작성 (키워드: 가부장, 폭력, 여성, 자유)
2. "소년이 온다" 독후감 작성 (키워드: 광주, 역사, 인권, 희생)
3. 추천 요청

**예상 결과:**
- 한강의 다른 작품들이 높은 유사도로 추천됨
- 인권, 사회 비판 테마의 작품들이 함께 추천됨

---

## 10. Swagger UI

### 10.1 웹 브라우저에서 API 문서 확인

```
http://localhost:8080/swagger-ui/index.html
```

**장점:**
- 모든 API 엔드포인트 확인
- Interactive Testing 가능
- Request/Response 스키마 확인

---

## 11. 트러블슈팅

### 11.1 401 Unauthorized
**원인**: 토큰이 만료되었거나 잘못됨  
**해결**: 다시 로그인하여 새 토큰 발급

### 11.2 OpenAI API 호출 실패
**원인**: API 키가 없거나 잘못됨  
**해결**: `application.yml`에서 `openai.api-key` 확인

### 11.3 Naver API 호출 실패
**원인**: API 키가 없거나 잘못됨  
**해결**: `application.yml`에서 `naver.client-id`, `naver.client-secret` 확인

### 11.4 MySQL 연결 실패
**원인**: MySQL 서버가 실행 중이 아니거나 DB가 없음  
**해결**:
```bash
# MySQL 시작
mysql.server start

# DB 생성
mysql -u root -p
CREATE DATABASE jeondoksi;
```

---

## 12. 성능 최적화 팁

### 12.1 응답 시간
- 일반 조회: ~100ms
- 도서 검색 (Naver API): ~500ms
- 퀴즈 생성 (OpenAI): ~5-10초
- 독후감 분석 (NLP): ~300ms
- AI 탐지 (OpenAI): ~2-3초

### 12.2 캐싱 전략
- 퀴즈: DB에 저장하여 재사용
- 도서 정보: 검색 시 자동 저장
- 추천 결과: 사용자별 독후감 변경 시 재계산

---

## 📞 문의 및 피드백

프로젝트 관련 문의사항이나 버그 리포트는 GitHub Issues를 이용해주세요.

**Happy Testing! 📚✨**
