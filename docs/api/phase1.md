# Phase 1 API 명세

이 문서는 Phase 1에서 iOS 클라이언트와 Spring 백엔드가 공유할 API 계약을 정의한다.

Phase 1의 목표는 다음 흐름을 end-to-end로 완성하는 것이다.

```text
회원가입/로그인
→ 종목 조회
→ 매수 주문
→ 포트폴리오 확인
```

## 공통 규칙

### Base URL

로컬 개발 환경 기준:

```text
http://localhost:8080
```

API prefix:

```text
/api/v1
```

### Content-Type

요청과 응답은 기본적으로 JSON을 사용한다.

```http
Content-Type: application/json
```

### 인증

회원가입과 로그인 API는 인증 없이 호출할 수 있다.

```text
POST /api/v1/auth/signup
POST /api/v1/auth/login
```

그 외 API는 로그인 후 발급받은 JWT access token을 사용한다.

```http
Authorization: Bearer {accessToken}
```

### 에러 응답 형식

Phase 1에서는 공통 에러 응답을 다음 형태로 맞춘다.

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지"
}
```

예시:

```json
{
  "code": "DUPLICATED_EMAIL",
  "message": "이미 사용 중인 이메일입니다."
}
```

## 1. 인증 API

### 1.1 회원가입

```http
POST /api/v1/auth/signup
```

신규 사용자를 생성한다.

#### Request

```json
{
  "email": "user@example.com",
  "password": "password1234",
  "nickname": "투자왕"
}
```

#### Request Fields

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | Y | 로그인에 사용할 이메일 |
| password | String | Y | 로그인 비밀번호 |
| nickname | String | Y | 앱에서 표시할 닉네임 |

#### Response

```json
{
  "id": 1,
  "email": "user@example.com",
  "nickname": "투자왕",
  "cashBalance": 10000000
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| id | Number | 사용자 ID |
| email | String | 이메일 |
| nickname | String | 닉네임 |
| cashBalance | Number | 초기 가상 현금 잔고 |

#### 정책

- 이메일은 중복될 수 없다.
- 비밀번호는 서버에 평문으로 저장하지 않고 BCrypt로 해싱한다.
- 신규 사용자의 초기 가상 현금은 10,000,000원이다.

---

### 1.2 로그인

```http
POST /api/v1/auth/login
```

이메일과 비밀번호로 로그인하고 JWT access token을 발급한다.

#### Request

```json
{
  "email": "user@example.com",
  "password": "password1234"
}
```

#### Request Fields

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| email | String | Y | 로그인 이메일 |
| password | String | Y | 로그인 비밀번호 |

#### Response

```json
{
  "accessToken": "jwt-access-token",
  "tokenType": "Bearer"
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| accessToken | String | API 인증에 사용할 JWT |
| tokenType | String | 토큰 타입. Phase 1에서는 `Bearer` 고정 |

#### 정책

- 이메일 또는 비밀번호가 일치하지 않으면 로그인에 실패한다.
- 로그인 실패 시 어떤 값이 틀렸는지 구체적으로 노출하지 않는다.

## 2. 시세 API

### 2.1 종목 목록 조회

```http
GET /api/v1/stocks
```

거래 가능한 국내 주식 목록을 조회한다.

Phase 1에서는 코스피200 또는 코스피50 중 제한된 목록만 제공한다.

#### Request Header

```http
Authorization: Bearer {accessToken}
```

#### Response

```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "market": "KOSPI"
    },
    {
      "stockCode": "000660",
      "stockName": "SK하이닉스",
      "market": "KOSPI"
    }
  ]
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| stocks | Array | 종목 목록 |
| stocks[].stockCode | String | 종목 코드 |
| stocks[].stockName | String | 종목명 |
| stocks[].market | String | 시장 구분 |

---

### 2.2 종목 상세 조회

```http
GET /api/v1/stocks/{stockCode}
```

특정 종목의 현재가를 포함한 상세 정보를 조회한다.

#### Request Header

```http
Authorization: Bearer {accessToken}
```

#### Path Variables

| 이름 | 타입 | 설명 |
|------|------|------|
| stockCode | String | 종목 코드 |

#### Response

```json
{
  "stockCode": "005930",
  "stockName": "삼성전자",
  "market": "KOSPI",
  "currentPrice": 75000,
  "priceChangedAt": "2026-05-10T15:30:00"
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| stockCode | String | 종목 코드 |
| stockName | String | 종목명 |
| market | String | 시장 구분 |
| currentPrice | Number | 현재가 |
| priceChangedAt | String | 현재가 기준 시각 |

#### 정책

- Phase 1에서는 KIS Open API의 REST 현재가 조회를 사용한다.
- KIS Open API 연동에 문제가 있으면 mock 시세 데이터를 사용할 수 있다.

## 3. 거래 API

### 3.1 매수 주문

```http
POST /api/v1/orders
```

현재가 기준으로 매수 주문을 즉시 체결한다.

#### Request Header

```http
Authorization: Bearer {accessToken}
```

#### Request

```json
{
  "stockCode": "005930",
  "quantity": 10,
  "orderType": "BUY"
}
```

#### Request Fields

| 필드 | 타입 | 필수 | 설명 |
|------|------|------|------|
| stockCode | String | Y | 매수할 종목 코드 |
| quantity | Number | Y | 매수 수량 |
| orderType | String | Y | Phase 1에서는 `BUY`만 허용 |

#### Response

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
  "executedAt": "2026-05-10T15:31:00"
}
```

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| orderId | Number | 주문 ID |
| stockCode | String | 종목 코드 |
| stockName | String | 종목명 |
| orderType | String | 주문 유형 |
| quantity | Number | 체결 수량 |
| executedPrice | Number | 체결가 |
| totalAmount | Number | 총 체결 금액 |
| cashBalance | Number | 주문 후 현금 잔고 |
| executedAt | String | 체결 시각 |

#### 정책

- Phase 1에서는 지정가 주문 없이 현재가 기준 시장가 매수만 처리한다.
- 수수료와 세금은 Phase 1에서 제외한다.
- 현금 잔고가 부족하면 주문을 거절한다.
- 수량은 1 이상이어야 한다.
- 주문이 성공하면 주문 내역을 저장하고 포트폴리오 보유 수량과 평균 매입가를 갱신한다.

## 4. 포트폴리오 API

### 4.1 내 포트폴리오 조회

```http
GET /api/v1/portfolio
```

로그인한 사용자의 현금 잔고, 보유 종목, 평가손익을 조회한다.

#### Request Header

```http
Authorization: Bearer {accessToken}
```

#### Response

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

#### Response Fields

| 필드 | 타입 | 설명 |
|------|------|------|
| cashBalance | Number | 현금 잔고 |
| totalStockValue | Number | 보유 주식 평가 금액 합계 |
| totalAsset | Number | 총 자산. 현금 + 보유 주식 평가 금액 |
| profitAmount | Number | 전체 평가손익 금액 |
| profitRate | Number | 전체 수익률 |
| holdings | Array | 보유 종목 목록 |
| holdings[].stockCode | String | 종목 코드 |
| holdings[].stockName | String | 종목명 |
| holdings[].quantity | Number | 보유 수량 |
| holdings[].averagePrice | Number | 평균 매입가 |
| holdings[].currentPrice | Number | 현재가 |
| holdings[].evaluationAmount | Number | 평가 금액 |
| holdings[].profitAmount | Number | 종목별 평가손익 금액 |
| holdings[].profitRate | Number | 종목별 수익률 |

#### 정책

- 평가 금액은 `현재가 * 보유 수량`으로 계산한다.
- 종목별 평가손익은 `(현재가 - 평균 매입가) * 보유 수량`으로 계산한다.
- 종목별 수익률은 `(현재가 - 평균 매입가) / 평균 매입가 * 100`으로 계산한다.
- 전체 수익률은 Phase 1에서는 초기 시드머니 10,000,000원 대비 수익률로 계산한다.

## Phase 1 엔드포인트 요약

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/auth/signup` | N | 회원가입 |
| POST | `/api/v1/auth/login` | N | 로그인 |
| GET | `/api/v1/stocks` | Y | 종목 목록 조회 |
| GET | `/api/v1/stocks/{stockCode}` | Y | 종목 상세 조회 |
| POST | `/api/v1/orders` | Y | 매수 주문 |
| GET | `/api/v1/portfolio` | Y | 내 포트폴리오 조회 |

## Phase 1에서 제외하는 것

- 매도 주문
- 거래 내역 조회
- 리그, 그룹, 랭킹
- 주간 정산
- 수수료와 세금
- WebSocket 실시간 시세
- Redis 시세 캐싱
- 배포 환경 설정
