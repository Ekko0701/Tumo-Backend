# Phase 1 API 테스트 가이드

Tumo Backend Phase 1 API 수동 테스트 절차 정리.

## 테스트 범위

```text
회원가입
로그인
종목 목록 조회
종목 상세 조회
매수 주문
포트폴리오 조회
추가 매수 후 평균 매입가 확인
잔고 부족 주문 실패 확인
로그아웃
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

### Swagger

```text
http://localhost:8080/swagger-ui/index.html
```

### Base URL

```text
http://localhost:8080
```

## 1. 회원가입

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "email": "phase1-user@example.com",
    "password": "password1234",
    "nickname": "페이즈원"
  }'
```

### 기대 응답

```http
HTTP/1.1 201
```

```json
{
  "id": 1,
  "email": "phase1-user@example.com",
  "nickname": "페이즈원",
  "cashBalance": 10000000
}
```

### 확인 포인트

```text
회원 생성 성공
초기 현금 잔고 10000000
password 응답 미포함
```

## 2. 로그인

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "phase1-user@example.com",
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

### 토큰 변수 저장

```bash
ACCESS_TOKEN="로그인_응답의_accessToken"
REFRESH_TOKEN="로그인_응답의_refreshToken"
```

Swagger 사용 시 `Authorize`에 access token 값 입력.

```text
Bearer 접두사 없이 accessToken 값만 입력
```

## 3. 종목 목록 조회

### Request

```bash
curl -i http://localhost:8080/api/v1/stocks \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "market": "KOSPI",
      "currentPrice": 75000,
      "priceChangedAt": "2026-05-15T09:00:00"
    }
  ]
}
```

### 확인 포인트

```text
stocks 배열 존재
개발용 seed 종목 조회
삼성전자 stockCode 005930 존재
```

## 4. 종목 상세 조회

### Request

```bash
curl -i http://localhost:8080/api/v1/stocks/005930 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "stockCode": "005930",
  "stockName": "삼성전자",
  "market": "KOSPI",
  "currentPrice": 75000,
  "priceChangedAt": "2026-05-15T09:00:00"
}
```

### 없는 종목 조회

```bash
curl -i http://localhost:8080/api/v1/stocks/999999 \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

기대 응답:

```http
HTTP/1.1 404
```

```json
{
  "code": "STOCK_NOT_FOUND",
  "message": "종목을 찾을 수 없습니다.",
  "fieldErrors": []
}
```

## 5. 매수 주문

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "stockCode": "005930",
    "quantity": 10,
    "orderType": "BUY"
  }'
```

### 기대 응답

```http
HTTP/1.1 201
```

```json
{
  "orderId": 1,
  "stockCode": "005930",
  "stockName": "삼성전자",
  "orderType": "BUY",
  "quantity": 10,
  "executedPrice": 75000,
  "totalAmount": 750000,
  "cashBalance": 9250000,
  "executedAt": "2026-05-15T09:10:00"
}
```

### 확인 포인트

```text
totalAmount = executedPrice * quantity
cashBalance = 이전 현금 잔고 - totalAmount
orders 테이블에 주문 내역 저장
holdings 테이블에 보유 종목 생성 또는 갱신
```

## 6. 포트폴리오 조회

### Request

```bash
curl -i http://localhost:8080/api/v1/portfolio \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 200
```

```json
{
  "cashBalance": 9250000,
  "totalStockValue": 750000,
  "totalAsset": 10000000,
  "profitAmount": 0,
  "profitRate": 0.0,
  "holdings": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "quantity": 10,
      "averagePrice": 75000,
      "currentPrice": 75000,
      "evaluationAmount": 750000,
      "profitAmount": 0,
      "profitRate": 0.0
    }
  ]
}
```

### 확인 포인트

```text
cashBalance는 매수 주문 후 잔고와 일치
totalStockValue = holdings[].evaluationAmount 합계
totalAsset = cashBalance + totalStockValue
profitAmount = totalAsset - 10000000
holdings[].evaluationAmount = currentPrice * quantity
holdings[].profitAmount = (currentPrice - averagePrice) * quantity
```

## 7. 추가 매수 후 평균 매입가 확인

같은 종목을 추가 매수.

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "stockCode": "005930",
    "quantity": 5,
    "orderType": "BUY"
  }'
```

### 포트폴리오 재조회

```bash
curl -i http://localhost:8080/api/v1/portfolio \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 확인 포인트

```text
holdings 배열에서 삼성전자 row 1개 유지
quantity는 기존 수량 + 추가 매수 수량
averagePrice는 가중 평균 매입가로 재계산
cashBalance는 추가 주문 금액만큼 차감
```

## 8. 잔고 부족 주문 실패

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "stockCode": "005930",
    "quantity": 1000000,
    "orderType": "BUY"
  }'
```

### 기대 응답

```http
HTTP/1.1 400
```

```json
{
  "code": "INSUFFICIENT_CASH",
  "message": "현금 잔고가 부족합니다.",
  "fieldErrors": []
}
```

### 확인 포인트

```text
잔고 부족 주문 실패
orders 테이블에 실패 주문 미저장
holdings 테이블 변경 없음
```

## 9. 주문 수량 검증 실패

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "stockCode": "005930",
    "quantity": 0,
    "orderType": "BUY"
  }'
```

### 기대 응답

```http
HTTP/1.1 400
```

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다.",
  "fieldErrors": [
    {
      "field": "quantity",
      "message": "주문 수량은 1 이상이어야 합니다."
    }
  ]
}
```

## 10. 로그아웃

### Request

```bash
curl -i -X POST http://localhost:8080/api/v1/auth/logout \
  -H "Authorization: Bearer $ACCESS_TOKEN"
```

### 기대 응답

```http
HTTP/1.1 204
```

응답 body 없음.

## 테스트 완료 기준

```text
회원가입 성공
로그인 성공 및 토큰 발급
종목 목록 조회 성공
종목 상세 조회 성공
매수 주문 성공
포트폴리오 조회 성공
추가 매수 후 보유 수량/평균 매입가 갱신 확인
잔고 부족 주문 실패 확인
주문 수량 검증 실패 확인
로그아웃 성공
```
