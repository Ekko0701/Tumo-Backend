# Phase 1 DB 스키마

이 문서는 Phase 1에서 사용할 데이터베이스 스키마를 정의한다.

Phase 1의 목표는 다음 흐름을 지원하는 최소 테이블을 구성하는 것이다.

```text
회원가입/로그인
→ 종목 조회
→ 매수 주문
→ 포트폴리오 확인
```

## 공통 규칙

### 데이터베이스

```text
PostgreSQL
```

### 테이블명

테이블명은 복수형 snake_case를 사용한다.

```text
users
orders
portfolios
```

### 시간 컬럼

시간 컬럼은 Phase 1에서는 `TIMESTAMP`를 사용한다.

서버 애플리케이션에서는 `LocalDateTime`으로 매핑한다.

### 금액 컬럼

금액은 정수 원화 단위로 관리한다.

Java에서는 `BigDecimal` 또는 `Long` 중 하나를 선택할 수 있다.

Phase 1에서는 소수점이 필요한 거래를 다루지 않으므로 `Long` 사용을 우선 검토한다.

단, 평균 매입가와 수익률 계산에서 소수점 처리가 필요해질 수 있으므로 정책이 확정되면 조정한다.

## 1. users

사용자 정보를 저장한다.

### 컬럼

| 컬럼 | 타입 | Java 타입 | Nullable | 제약/기본값 | 설명 |
|------|------|-----------|----------|-------------|------|
| id | BIGSERIAL | Long | N | PK | 사용자 ID |
| email | VARCHAR(255) | String | N | UNIQUE | 로그인 이메일 |
| password_hash | VARCHAR(255) | String | N | | BCrypt 해시 비밀번호 |
| nickname | VARCHAR(50) | String | N | | 앱 표시 닉네임 |
| cash_balance | BIGINT | Long | N | DEFAULT 10000000 | 가상 현금 잔고 |
| created_at | TIMESTAMP | LocalDateTime | N | | 생성 시각 |
| updated_at | TIMESTAMP | LocalDateTime | N | | 수정 시각 |

### 제약

```sql
UNIQUE (email)
```

### 정책

- 이메일은 중복될 수 없다.
- 비밀번호는 평문으로 저장하지 않고 BCrypt 해시값만 저장한다.
- 신규 사용자의 초기 현금 잔고는 10,000,000원이다.
- `cash_balance`는 서버의 주문 처리 로직에서만 변경한다.

## 2. orders

주문 및 체결 내역을 저장한다.

Phase 1에서는 매수 주문만 지원하며, 주문은 현재가 기준으로 즉시 체결된다고 가정한다.

### 컬럼

| 컬럼 | 타입 | Java 타입 | Nullable | 제약/기본값 | 설명 |
|------|------|-----------|----------|-------------|------|
| id | BIGSERIAL | Long | N | PK | 주문 ID |
| user_id | BIGINT | Long | N | FK | 주문 사용자 ID |
| stock_code | VARCHAR(10) | String | N | | 종목 코드 |
| stock_name | VARCHAR(100) | String | N | | 종목명 |
| order_type | VARCHAR(10) | String 또는 Enum | N | | Phase 1에서는 BUY |
| quantity | INTEGER | Integer | N | | 주문 수량 |
| executed_price | BIGINT | Long | N | | 체결가 |
| total_amount | BIGINT | Long | N | | 총 체결 금액 |
| executed_at | TIMESTAMP | LocalDateTime | N | | 체결 시각 |
| created_at | TIMESTAMP | LocalDateTime | N | | 생성 시각 |

### 외래 키

```sql
orders.user_id -> users.id
```

### 인덱스

```sql
INDEX (user_id)
INDEX (stock_code)
INDEX (executed_at)
```

### 정책

- Phase 1에서는 `order_type`으로 `BUY`만 허용한다.
- 주문은 별도 대기 상태 없이 즉시 체결된다.
- `executed_price`는 주문 시점의 현재가를 사용한다.
- `total_amount`는 `executed_price * quantity`로 계산한다.
- 수수료와 세금은 Phase 1에서 제외한다.
- 현금 잔고가 부족하면 주문을 저장하지 않고 거절한다.

## 3. portfolios

사용자별 보유 종목 정보를 저장한다.

한 사용자는 같은 종목에 대해 하나의 포트폴리오 row만 가진다.

### 컬럼

| 컬럼 | 타입 | Java 타입 | Nullable | 제약/기본값 | 설명 |
|------|------|-----------|----------|-------------|------|
| id | BIGSERIAL | Long | N | PK | 포트폴리오 ID |
| user_id | BIGINT | Long | N | FK | 사용자 ID |
| stock_code | VARCHAR(10) | String | N | | 종목 코드 |
| stock_name | VARCHAR(100) | String | N | | 종목명 |
| quantity | INTEGER | Integer | N | | 보유 수량 |
| average_price | BIGINT | Long | N | | 평균 매입가 |
| updated_at | TIMESTAMP | LocalDateTime | N | | 수정 시각 |
| created_at | TIMESTAMP | LocalDateTime | N | | 생성 시각 |

### 외래 키

```sql
portfolios.user_id -> users.id
```

### 제약

```sql
UNIQUE (user_id, stock_code)
```

### 인덱스

```sql
INDEX (user_id)
INDEX (stock_code)
```

### 정책

- 매수 주문이 성공하면 포트폴리오를 생성하거나 갱신한다.
- 기존에 보유하지 않은 종목이면 새 row를 생성한다.
- 이미 보유 중인 종목이면 수량과 평균 매입가를 갱신한다.
- Phase 1에서는 매도 주문이 없으므로 보유 수량 감소는 발생하지 않는다.

## 평균 매입가 계산

같은 종목을 추가 매수하면 평균 매입가를 다음 공식으로 갱신한다.

```text
newAveragePrice =
  ((oldAveragePrice * oldQuantity) + (executedPrice * buyQuantity))
  / (oldQuantity + buyQuantity)
```

예시:

```text
기존 보유: 10주, 평균 70,000원
추가 매수: 5주, 체결가 80,000원

newAveragePrice =
  ((70,000 * 10) + (80,000 * 5)) / 15
  = 73,333원
```

원 단위 반올림/버림 정책은 구현 시 명시한다.

Phase 1에서는 소수점 없이 원 단위로 저장하므로 나눗셈 결과는 버림 처리하는 것을 우선 검토한다.

## 포트폴리오 평가 계산

포트폴리오 조회 시점에 현재가를 조회해 평가 금액과 수익률을 계산한다.

### 종목별 평가 금액

```text
evaluationAmount = currentPrice * quantity
```

### 종목별 평가손익

```text
profitAmount = (currentPrice - averagePrice) * quantity
```

### 종목별 수익률

```text
profitRate = (currentPrice - averagePrice) / averagePrice * 100
```

### 전체 자산

```text
totalAsset = cashBalance + totalStockValue
```

### 전체 수익률

Phase 1에서는 초기 시드머니 10,000,000원 대비 수익률로 계산한다.

```text
profitRate = (totalAsset - 10000000) / 10000000 * 100
```

## ERD

```text
users 1 ─── N orders
users 1 ─── N portfolios
```

## Phase 1 이후 확장 예정

Phase 2 이후 다음 테이블을 추가할 수 있다.

```text
stocks
seasons
groups
rankings
refresh_tokens
```

Phase 1에서는 외부 시세 API 또는 mock 데이터를 사용하므로 별도의 `stocks` 테이블은 만들지 않는다.
