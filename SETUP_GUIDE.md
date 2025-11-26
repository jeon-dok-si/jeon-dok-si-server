# 🔐 환경 설정 가이드

## 📋 개요

이 프로젝트는 **보안을 위해 API 키와 민감한 정보를 Git에 포함하지 않습니다**.  
로컬에서 실행하려면 다음 단계를 따라 설정하세요.

---

## 🚀 빠른 시작

### 1. application.yml 생성

```bash
# src/main/resources 디렉토리로 이동
cd src/main/resources

# 템플릿 파일을 복사하여 실제 설정 파일 생성
cp application-example.yml application.yml
```

### 2. API 키 입력

`application.yml` 파일을 열고 다음 항목들을 실제 값으로 변경하세요:

```yaml
# 데이터베이스
username: root              # MySQL 사용자명
password: root              # MySQL 비밀번호

# JWT
jwt:
  secret: YOUR_ACTUAL_SECRET_KEY_256_BITS_OR_MORE

# 알라딘 API
api:
  aladin:
    key: YOUR_ALADIN_API_KEY

# 네이버 Book API
  naver:
    client-id: YOUR_NAVER_CLIENT_ID
    client-secret: YOUR_NAVER_CLIENT_SECRET

# OpenAI API
openai:
  api-key: YOUR_OPENAI_API_KEY
  model: gpt-3.5-turbo
```

---

## 🔑 API 키 발급 방법

### 1. OpenAI API 키 (필수)

**용도**: AI 탐지, 퀴즈 자동 생성

1. [OpenAI Platform](https://platform.openai.com/) 접속
2. 로그인 후 `API Keys` 메뉴로 이동
3. `Create new secret key` 클릭
4. 생성된 키 복사 (sk-proj-로 시작)
5. `application.yml`의 `openai.api-key`에 입력

**⚠️ 주의사항**:
- API 키는 한 번만 표시되므로 반드시 복사해두세요
- 무료 크레딧이 소진되면 유료 결제 필요
- 비용 관리를 위해 Usage Limits 설정 권장

---

### 2. Naver Book API 키 (권장)

**용도**: 도서 검색

1. [Naver Developers](https://developers.naver.com/) 접속
2. 로그인 후 `애플리케이션 등록` 클릭
3. 다음 정보 입력:
   - 애플리케이션 이름: `jeondoksi` (또는 원하는 이름)
   - 사용 API: `검색` 선택
4. 등록 후 `Client ID`와 `Client Secret` 복사
5. `application.yml`에 입력:
   ```yaml
   naver:
     client-id: YOUR_CLIENT_ID
     client-secret: YOUR_CLIENT_SECRET
   ```

**📌 참고**:
- 하루 25,000건 무료 호출 가능
- 개인 개발자 계정으로 충분

---

### 3. Aladin API 키 (선택사항)

**용도**: 보조 도서 정보 검색

1. [알라딘 OpenAPI](https://blog.aladin.co.kr/openapi/category/6395187) 접속
2. TTBKey 신청
3. 발급받은 키를 `application.yml`에 입력:
   ```yaml
   aladin:
     key: YOUR_ALADIN_KEY
   ```

**📌 참고**:
- 현재 코드에서는 주로 Naver API를 사용
- 선택적으로 추가 가능

---

### 4. JWT Secret 키 생성

**용도**: 사용자 인증 토큰 암호화

**방법 1: 온라인 생성기 사용**
```bash
# 브라우저에서:
https://jwtsecrets.com/

# 256-bit 또는 512-bit 선택 후 생성
```

**방법 2: 터미널에서 생성**
```bash
# macOS/Linux
openssl rand -base64 64

# 또는
node -e "console.log(require('crypto').randomBytes(64).toString('hex'))"
```

생성된 키를 `application.yml`에 입력:
```yaml
jwt:
  secret: <생성된_키>
  expiration: 86400000
```

---

## 📁 파일 구조

```
src/main/resources/
├── application.yml
├── application-example.yml
└── data.sql
```

---

## ⚠️ 보안 주의사항

### ✅ 해야 할 것
- `application.yml`은 **절대 Git에 커밋하지 마세요**
- API 키는 **팀원과 안전한 방법으로 공유** (Slack DM, 비밀번호 관리자 등)
- `.gitignore`가 올바르게 설정되었는지 확인

### ❌ 하지 말아야 할 것
- API 키를 코드에 하드코딩
- 공개 저장소에 API 키 업로드
- 무료 크레딧을 소진 후 방치 (OpenAI 요금 주의)

---

## 🧪 설정 확인

모든 설정이 완료되었는지 확인:

```bash
# 서버 실행
./gradlew bootRun

# 정상 실행 확인 (8080 포트)
curl http://localhost:8080/swagger-ui/index.html
```

**예상 로그**:
```
✅ MySQL 연결 성공
✅ JPA 테이블 생성 완료
✅ data.sql 실행 완료
✅ 서버 시작: http://localhost:8080
```

---

## 🔧 트러블슈팅

### 문제 1: OpenAI API 호출 실패
```
Error: 401 Unauthorized
```
**해결**: `openai.api-key`가 올바른지 확인, sk-proj-로 시작해야 함

### 문제 2: Naver API 호출 실패
```
Error: 401 Unauthorized
```
**해결**: `client-id`와 `client-secret` 확인, 공백이나 따옴표 없이 입력

### 문제 3: MySQL 연결 실패
```
Error: Access denied for user
```
**해결**: 
1. MySQL 서버 실행 확인: `mysql.server status`
2. 사용자명/비밀번호 확인
3. 데이터베이스 생성: `CREATE DATABASE book_service;`
