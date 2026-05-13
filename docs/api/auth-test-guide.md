# Auth API 테스트 가이드

Tumo Backend 인증 API 수동 테스트 절차 정리.

## 테스트 범위

```text
회원가입
로그인
내 정보 조회
Access Token 재발급
로그아웃
로그아웃 이후 재발급 실패
```

## 사전 확인

### PostgreSQL 실행

```bash
brew services list | grep postgresql
```

기대 상태:

```text
postgresql@15 started
```

### DB 접속 확인

```bash
psql -h localhost -U tumo -d tumo
```

접속 성공 후 종료:

```sql
\q
```

### Spring Boot 실행

IntelliJ에서 `TumoApplication` 실행.

또는 터미널 실행:

```bash
./gradlew bootRun
```

기대 로그:

```text
Tomcat started on port 8080
Started TumoApplication
```

### Base URL

```text
http://localhost:8080
```

## 1. 회원가입

### Request

```http
POST /api/v1/auth/signup
Content-Type: application/json
```

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password1234",
    "nickname": "투자왕"
  }'
```

### 기대 응답

```http
HTTP/1.1 201
```

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "투자왕",
  "cashBalance": 10000000
}
```

### 확인 포인트

```text
HTTP Status 201 Created
password 미노출
cashBalance 10000000
```

## 2. 로그인

### Request

```http
POST /api/v1/auth/login
Content-Type: application/json
```

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "password1234"
  }'
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "accessToken": "jwt-access-token",
  "refreshToken": "jwt-refresh-token",
  "tokenType": "Bearer"
}
```

### 확인 포인트

```text
accessToken 존재
refreshToken 존재
tokenType Bearer
```

### 토큰 변수 저장

터미널에서 이후 요청을 편하게 테스트하기 위한 변수 설정.

```bash
ACCESS_TOKEN="로그인_응답의_accessToken"
REFRESH_TOKEN="로그인_응답의_refreshToken"
```

## 3. 내 정보 조회

### Request

```http
GET /api/v1/users/me
Authorization: Bearer {accessToken}
```

```bash
curl -i http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "투자왕",
  "cashBalance": 10000000
}
```

### 확인 포인트

```text
Authorization 헤더 없으면 INVALID_TOKEN
유효한 Access Token이면 사용자 정보 응답
```

## 4. Access Token 재발급

### Request

```http
POST /api/v1/auth/token/refresh
Content-Type: application/json
```

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "accessToken": "new-jwt-access-token",
  "refreshToken": "new-jwt-refresh-token",
  "tokenType": "Bearer"
}
```

### 확인 포인트

```text
새 accessToken 발급
새 refreshToken 발급
기존 refreshToken은 새 refreshToken으로 교체
```

### 새 토큰 변수 저장

재발급 응답의 새 토큰으로 변수 교체.

```bash
ACCESS_TOKEN="재발급_응답의_accessToken"
REFRESH_TOKEN="재발급_응답의_refreshToken"
```

## 5. 로그아웃

### Request

```http
POST /api/v1/auth/logout
Authorization: Bearer {accessToken}
```

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 204
```

응답 body 없음.

### 확인 포인트

```text
서버에 저장된 Refresh Token 삭제
Access Token은 만료 시간까지 유효 가능
```

## 6. 로그아웃 이후 재발급 실패

### Request

로그아웃 전에 사용하던 Refresh Token으로 재발급 요청.

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/token/refresh \
  -H "Content-Type: application/json" \
  -d "{
    \"refreshToken\": \"$REFRESH_TOKEN\"
  }"
```

### 기대 응답

```http
HTTP/1.1 401
```

```json
{
  "code": "INVALID_TOKEN",
  "message": "인증 토큰이 유효하지 않습니다.",
  "fieldErrors": []
}
```

### 확인 포인트

```text
로그아웃 후 Refresh Token 재사용 불가
재로그인 전까지 Access Token 재발급 불가
```

## 7. 검증 실패 케이스

### 회원가입 요청값 오류

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "wrong-email",
    "password": "123",
    "nickname": ""
  }'
```

기대 응답:

```http
HTTP/1.1 400
```

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다.",
  "fieldErrors": [
    {
      "field": "email",
      "message": "이메일 형식이 올바르지 않습니다."
    }
  ]
}
```

### 로그인 실패

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "wrong-password"
  }'
```

기대 응답:

```http
HTTP/1.1 401
```

```json
{
  "code": "INVALID_LOGIN",
  "message": "이메일 또는 비밀번호가 올바르지 않습니다.",
  "fieldErrors": []
}
```

### 인증 헤더 누락

```bash
curl -i http://localhost:8080/api/v1/users/me
```

기대 응답:

```http
HTTP/1.1 401
```

```json
{
  "code": "INVALID_TOKEN",
  "message": "인증 토큰이 유효하지 않습니다.",
  "fieldErrors": []
}
```

## 테스트 완료 기준

```text
회원가입 성공
로그인 성공 및 token 2개 발급
Access Token으로 내 정보 조회 성공
Refresh Token으로 token 재발급 성공
로그아웃 성공
로그아웃 이후 기존 Refresh Token 재발급 실패
요청값 오류 시 fieldErrors 응답
로그인 실패 시 INVALID_LOGIN 응답
인증 헤더 누락 시 INVALID_TOKEN 응답
```
