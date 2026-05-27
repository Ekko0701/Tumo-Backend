# application.yaml 설정

이 문서는 `src/main/resources/application.yaml`의 현재 설정 값을 설명한다.

## 전체 구조

```yaml
spring:
  application:
  datasource:
  jpa:

server:

jwt:

kis:
```

## `spring.application`

Spring Boot 애플리케이션 이름을 설정한다.

```yaml
spring:
  application:
    name: Tumo
```

## `spring.datasource`

로컬 PostgreSQL 연결 정보를 설정한다.

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tumo
    username: tumo
    password: tumo
```

주의:

- 현재 값은 로컬 개발용이다.
- 운영 환경에서는 DB 접속 정보와 비밀번호를 환경변수 또는 secret 관리 도구로 분리해야 한다.

## `spring.jpa`

JPA와 Hibernate 동작을 설정한다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true
    open-in-view: false
```

설정 의미:

| 설정 | 의미 |
|------|------|
| `ddl-auto: update` | Entity 기준으로 DB 스키마를 자동 갱신한다. 개발 초기에만 적합하다. |
| `format_sql: true` | Hibernate SQL 로그를 읽기 좋게 포맷한다. |
| `show-sql: true` | 실행 SQL을 콘솔에 출력한다. |
| `open-in-view: false` | Web 요청이 끝날 때까지 영속성 컨텍스트를 열어두지 않는다. |

운영 환경에서는 `ddl-auto: update` 대신 `validate`와 마이그레이션 도구 사용을 검토한다.

## `server`

Spring Boot 내장 서버 포트를 설정한다.

```yaml
server:
  port: 8080
```

로컬 Base URL:

```text
http://localhost:8080
```

## `jwt`

JWT Access Token과 Refresh Token 발급/검증에 사용하는 값을 설정한다.

```yaml
jwt:
  secret: tumo-local-development-secret-key-must-be-at-least-32-bytes
  access-token-expiration-millis: 3600000
  refresh-token-expiration-millis: 1209600000
```

| 설정 | 의미 |
|------|------|
| `secret` | JWT signature 생성과 검증에 사용하는 secret |
| `access-token-expiration-millis` | Access Token 만료 시간 |
| `refresh-token-expiration-millis` | Refresh Token 만료 시간 |

주의:

- `secret`은 개발용 값이다.
- 운영 환경에서는 반드시 환경변수 또는 secret 관리 도구로 분리한다.

## `kis`

한국투자증권 KIS Open API 연동 설정이다.

```yaml
kis:
  enabled: false
  app-key: ${KIS_APP_KEY:}
  app-secret: ${KIS_APP_SECRET:}
  rest-url: ${KIS_REST_URL:https://openapi.koreainvestment.com:9443}
  websocket-url: ${KIS_WEBSOCKET_URL:ws://ops.koreainvestment.com:21000}
  websocket-path: ${KIS_WEBSOCKET_PATH:/tryitout}
  customer-type: ${KIS_CUSTOMER_TYPE:P}
  trade-price-tr-id: ${KIS_TRADE_PRICE_TR_ID:H0STCNT0}
  order-book-tr-id: ${KIS_ORDER_BOOK_TR_ID:H0STASP0}
```

| 설정 | 의미 |
|------|------|
| `enabled` | KIS adapter 활성화 여부 |
| `app-key` | KIS Developers에서 발급받은 app key |
| `app-secret` | KIS Developers에서 발급받은 app secret |
| `rest-url` | KIS REST API 기본 URL |
| `websocket-url` | KIS WebSocket 기본 URL |
| `websocket-path` | KIS WebSocket 접속 경로 |
| `customer-type` | KIS 고객 타입 |
| `trade-price-tr-id` | 국내주식 실시간체결가 TR ID |
| `order-book-tr-id` | 국내주식 실시간호가 TR ID |

기본값 `kis.enabled=false`에서는 KIS adapter가 활성화되지 않는다.

실제 KIS 연동을 실행하려면 환경변수로 값을 주입한다.

```text
KIS_APP_KEY
KIS_APP_SECRET
KIS_REST_URL
KIS_WEBSOCKET_URL
KIS_WEBSOCKET_PATH
KIS_CUSTOMER_TYPE
KIS_TRADE_PRICE_TR_ID
KIS_ORDER_BOOK_TR_ID
```

주의:

- App Key, App Secret, approval key는 로그에 남기지 않는다.
- KIS 실전/모의 URL은 실제 계정 환경과 KIS 공식 문서를 기준으로 확인한다.
