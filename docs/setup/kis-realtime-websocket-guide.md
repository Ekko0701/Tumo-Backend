# KIS 실시간 시세 WebSocket 연동 가이드

이 문서는 Tumo Backend에 한국투자증권 KIS Open API WebSocket 기반 국내주식 실시간 체결가와 실시간 호가를 연동하기 위한 구현 가이드다.

코드는 이 문서를 기준으로 사용자 승인 후 단계별로 작성한다.

## 참고 자료

- KIS Open API 샘플 저장소: https://github.com/koreainvestment/open-trading-api
- 국내주식 WebSocket 예제: `examples_user/domestic_stock/domestic_stock_examples_ws.py`
- 국내주식 WebSocket 함수: `examples_user/domestic_stock/domestic_stock_functions_ws.py`
- KIS 설정 예시: `kis_devlp.yaml`

공식 샘플에서 확인한 내용:

- 저장소는 KIS Open API 연동 예시를 제공하는 샘플 코드 저장소다.
- Open API 사용을 위해 App Key, App Secret 발급이 필요하다.
- WebSocket 예제는 `ka.auth()`, `ka.auth_ws()` 인증 후 `KISWebSocket(api_url="/tryitout")`을 생성한다.
- 국내주식 실시간체결가 KRX 예제는 `ccnl_krx` 요청을 `["005930", "000660"]` 같은 종목 코드 목록으로 구독한다.
- 국내주식 실시간호가 KRX 예제는 호가 요청을 `["005930", "000660"]` 같은 종목 코드 목록으로 구독한다.
- 설정 예시는 실전 REST URL, 실전 WebSocket URL, 모의 REST URL, 모의 WebSocket URL을 구분한다.
- 사용할 KIS TR ID는 `H0STCNT0` 국내주식 실시간체결가, `H0STASP0` 국내주식 실시간호가다.

## 목표

Backend가 DB에 저장된 종목 전체를 KIS WebSocket에 구독하고, KIS에서 들어오는 실시간 체결가와 실시간 호가를 각각 도메인 이벤트로 변환해 기존 실시간 시세 처리 흐름에 연결한다.

```text
StockPriceSubscriptionService
→ StockRealtimePriceClient.subscribe(stockCodes, stockRealtimePriceService::handle)
→ KisRealtimeStockPriceClient
→ H0STCNT0 체결가 구독 요청
→ H0STASP0 호가 구독 요청
→ KIS 실시간 체결가 메시지 수신
→ KisTradePriceMessageParser
→ StockPriceEvent
→ handler.handle(event)
→ StockRealtimePriceService.handle(event)
→ Stock.updatePrice(...)
→ StockPricePublisher.publish(event)
```

호가 흐름은 별도 도메인 모델과 publisher를 둔다.

```text
KIS 실시간 호가 메시지 수신
→ KisOrderBookMessageParser
→ StockOrderBookEvent
→ StockOrderBookService.handle(event)
→ StockOrderBookPublisher.publish(event)
```

체결가는 실제 거래가 발생한 가격이고, 호가는 아직 체결되지 않은 매수/매도 대기 물량이다. 두 데이터는 의미, 필드 구조, 화면 사용 방식이 다르므로 하나의 객체에 섞지 않는다.

## 사용할 KIS TR

| TR ID | 용도 | Tumo 도메인 모델 | 주요 사용 화면 |
|-------|------|------------------|----------------|
| `H0STCNT0` | 국내주식 실시간체결가 | `StockPrice`, `StockPriceEvent` | 주식 리스트 현재가, 주문 직전 가격 캐시, 포트폴리오 평가 기준 |
| `H0STASP0` | 국내주식 실시간호가 | `StockOrderBook`, `StockOrderBookEvent` | 주식 리스트 최우선 매수/매도 호가, 종목 상세 호가창 |

API Portal URL은 문서/테스트 페이지이며, 실제 WebSocket 요청에서는 URL 자체가 아니라 TR ID를 구독 메시지의 `tr_id`로 사용한다.

```text
체결가 구독: tr_id = H0STCNT0, tr_key = 005930
호가 구독: tr_id = H0STASP0, tr_key = 005930
```

## 구현 원칙

- KIS 구현체는 체결가 구독을 위해 `StockRealtimePriceClient` port를 구현한다.
- 호가 구독은 별도 port를 추가해 분리한다. 예: `StockRealtimeOrderBookClient`, `StockOrderBookPublisher`.
- KIS 원본 메시지 포맷은 adapter 내부에서만 다룬다.
- `StockRealtimePriceService`, `OrderService`, `PortfolioService`는 KIS class를 import하지 않는다.
- 체결가와 호가는 parser, domain event, service, publisher를 분리한다.
- KIS App Key, App Secret, approval key는 로그에 남기지 않는다.
- 기본 실행에서는 no-op adapter를 유지하고, 명시적으로 KIS 설정을 켰을 때만 KIS adapter를 활성화한다.
- KIS 연동 실패가 애플리케이션 전체 부팅 실패로 이어질지, 실시간 시세만 비활성화할지는 별도 정책으로 결정한다.

## 추천 패키지 구조

```text
com.tumo.stock.adapter.out.kis
├── KisProperties
├── KisApprovalKeyClient
├── KisRealtimeStockPriceClient
├── KisTradePriceMessageParser
└── KisOrderBookMessageParser
```

역할:

| 클래스 | 책임 |
|--------|------|
| `KisProperties` | KIS App Key, App Secret, REST URL, WebSocket URL, TR ID 등 설정 바인딩 |
| `KisApprovalKeyClient` | WebSocket 접속에 필요한 approval key 발급 |
| `KisRealtimeStockPriceClient` | KIS WebSocket 연결, 체결가/호가 구독/해제, 메시지 수신, handler 호출 |
| `KisTradePriceMessageParser` | `H0STCNT0` raw message를 `StockPriceEvent`로 변환 |
| `KisOrderBookMessageParser` | `H0STASP0` raw message를 `StockOrderBookEvent`로 변환 |

## 도메인 모델 확장 계획

체결가 모델은 현재 구조를 유지한다.

```text
StockPrice
StockPriceEvent
StockRealtimePriceService
StockPricePublisher
```

호가는 별도 모델로 추가한다.

```text
StockOrderBook
StockOrderBookLevel
StockOrderBookEvent
StockOrderBookService
StockOrderBookPublisher
```

역할:

| 모델 | 책임 |
|------|------|
| `StockOrderBook` | 특정 시점의 종목 호가 상태 |
| `StockOrderBookLevel` | 한 가격대의 매도/매수 가격과 잔량 |
| `StockOrderBookEvent` | 호가 변경이 발생했다는 이벤트 |
| `StockOrderBookService` | 호가 이벤트 처리와 publish 조율 |
| `StockOrderBookPublisher` | iOS stream 구독자에게 호가 이벤트 전달 |

초기 리스트 화면은 전체 호가창을 모두 보여주지 않는다. 리스트에서는 최우선 매도/매수 호가와 스프레드만 사용하고, 종목 상세 화면에서 10호가 등 깊은 호가창을 사용한다.

```text
주식 리스트:
- 현재가
- 전일 대비
- 등락률
- 거래량
- 최우선 매도호가
- 최우선 매수호가
- 스프레드

종목 상세:
- 현재가
- 체결량
- 호가 10단계
- 가격대별 잔량
- 누적 잔량
```

## 설정 가이드

`application.yaml`에는 secret 원문을 직접 쓰지 않고 환경변수 override를 둔다.

```yaml
kis:
  enabled: false
  app-key: ${KIS_APP_KEY:}
  app-secret: ${KIS_APP_SECRET:}
  rest-url: ${KIS_REST_URL:https://openapi.koreainvestment.com:9443}
  websocket-url: ${KIS_WEBSOCKET_URL:ws://ops.koreainvestment.com:21000}
  websocket-path: ${KIS_WEBSOCKET_PATH:/tryitout}
  customer-type: ${KIS_CUSTOMER_TYPE:P}
  domestic-stock-trade-price-tr-id: ${KIS_DOMESTIC_STOCK_TRADE_PRICE_TR_ID:H0STCNT0}
  domestic-stock-order-book-tr-id: ${KIS_DOMESTIC_STOCK_ORDER_BOOK_TR_ID:H0STASP0}
```

주의:

- 실전/모의 URL은 KIS 공식 문서와 실제 계정 환경을 기준으로 최종 확인한다.
- WebSocket URL scheme은 공식 샘플과 운영 환경에서 요구하는 값이 다를 수 있으므로 구현 직전 재확인한다.
- `enabled` 기본값은 `false`로 둔다. KIS adapter가 준비되기 전까지 local/test context는 no-op client로 동작해야 한다.

## 단계별 구현 가이드

### 1단계: 설정 객체 추가

목표:

- `KisProperties`를 추가한다.
- `@ConfigurationProperties(prefix = "kis")`로 설정을 바인딩한다.
- `TumoApplication` 또는 별도 configuration에서 properties를 활성화한다.

완료 기준:

- secret 없이도 기본 profile에서 애플리케이션이 뜬다.
- `kis.enabled=false`일 때는 KIS bean이 생성되지 않는다.
- 설정 값에는 class, record, property 주석을 상세히 작성한다.

### 2단계: WebSocket approval key 발급 client 추가

목표:

- `KisApprovalKeyClient`를 추가한다.
- KIS REST endpoint로 App Key, App Secret을 전달해 WebSocket 접속키를 발급받는다.
- 응답에서 approval key만 추출한다.

주의:

- App Key, App Secret, approval key는 로그에 남기지 않는다.
- HTTP 실패, 응답 파싱 실패, approval key 누락은 명확한 예외로 처리한다.
- 이 client는 KIS adapter 내부에서만 사용한다.

완료 기준:

- `kis.enabled=true`이고 credentials가 없으면 명확한 오류가 난다.
- credentials가 없는 기본 test 환경에서는 이 bean이 활성화되지 않는다.

### 3단계: 체결가 도메인 흐름 유지

목표:

- 기존 `StockPrice`, `StockPriceEvent`, `StockRealtimePriceService` 흐름을 체결가 전용으로 유지한다.
- 이름은 당장 바꾸지 않는다. 다만 문서에서는 `StockPrice`가 체결가/현재가 중심 모델임을 명확히 한다.

완료 기준:

- 호가 필드를 `StockPrice`에 추가하지 않는다.
- 주문과 포트폴리오는 체결가 기반 최신 가격을 사용한다.

### 4단계: 호가 도메인 모델과 port 추가

목표:

- `StockOrderBook`을 추가한다.
- `StockOrderBookLevel`을 추가한다.
- `StockOrderBookEvent`를 추가한다.
- `StockOrderBookPublisher`를 추가한다.
- 필요하면 `StockRealtimeOrderBookClient`를 추가한다.

완료 기준:

- 호가 모델은 `StockPrice`와 독립적이다.
- 각 class, record, property에 Javadoc을 작성한다.
- KIS 필드명은 도메인 모델에 노출하지 않는다.

### 5단계: KIS 메시지 parser 추가

목표:

- `KisTradePriceMessageParser`를 추가한다.
- 국내주식 실시간체결가 raw message를 `StockPriceEvent`로 변환한다.
- `KisOrderBookMessageParser`를 추가한다.
- 국내주식 실시간호가 raw message를 `StockOrderBookEvent`로 변환한다.

변환 기준:

```text
KIS raw message
→ StockPrice
→ StockPriceEvent.fromKis(...)
```

호가 변환 기준:

```text
KIS raw message
→ StockOrderBook
→ StockOrderBookEvent.fromKis(...)
```

`StockPrice`에 매핑할 값:

| Tumo field | 의미 |
|------------|------|
| `stockCode` | 종목 코드 |
| `currentPrice` | 현재가 |
| `changePrice` | 전일 대비 가격 변화량 |
| `changeRate` | 전일 대비 가격 변화율 |
| `tradeVolume` | 누적 거래량 |
| `priceChangedAt` | KIS 체결 시각 또는 가격 변경 시각 |

주의:

- KIS 필드명을 도메인 밖으로 노출하지 않는다.
- 상승/하락 부호 코드를 해석해 `changePrice`, `changeRate`의 부호를 도메인 값으로 변환한다.
- parser는 fixture 기반 단위 테스트를 먼저 작성한다.
- 파싱할 수 없는 메시지는 stream 전체를 중단하지 않도록 폐기하거나 warning 로그 정책을 둔다.

완료 기준:

- 정상 체결 메시지 fixture가 `StockPriceEvent`로 변환된다.
- 정상 호가 메시지 fixture가 `StockOrderBookEvent`로 변환된다.
- 체결가가 아닌 메시지는 무시된다.
- 호가가 아닌 메시지는 무시된다.
- 잘못된 메시지 하나가 전체 WebSocket 처리를 중단하지 않는다.

### 6단계: KIS WebSocket client 추가

목표:

- `KisRealtimeStockPriceClient`가 `StockRealtimePriceClient`를 구현한다.
- `subscribe(stockCodes, handler)`에서 approval key를 준비하고 WebSocket에 연결한다.
- 각 `stockCode`에 대해 국내주식 실시간체결가 구독 요청을 보낸다.
- 각 `stockCode`에 대해 국내주식 실시간호가 구독 요청도 보낸다.
- 메시지가 도착할 때마다 parser로 `StockPriceEvent`를 만들고 `handler.handle(event)`를 호출한다.
- 호가 메시지가 도착하면 parser로 `StockOrderBookEvent`를 만들고 호가 handler 또는 publisher 흐름으로 전달한다.

핵심 흐름:

```text
subscribe(["005930", "000660"], stockRealtimePriceService::handle)
→ approval key 발급
→ KIS WebSocket 연결
→ 005930 H0STCNT0 체결가 구독 요청
→ 005930 H0STASP0 호가 구독 요청
→ 000660 H0STCNT0 체결가 구독 요청
→ 000660 H0STASP0 호가 구독 요청
→ KIS 체결가 메시지 수신
→ StockPriceEvent 생성
→ handler.handle(event)
→ KIS 호가 메시지 수신
→ StockOrderBookEvent 생성
→ orderBookHandler.handle(event)
```

주의:

- `subscribe(...)` 호출 시점에는 이벤트를 만들지 않는다.
- 이벤트는 KIS 메시지가 도착할 때마다 하나씩 생성한다.
- handler가 없거나 parser가 empty를 반환하면 메시지를 버린다.
- 연결 종료, 오류, 재연결 정책은 최소 구현 후 별도 보강한다.

완료 기준:

- KIS adapter는 체결가/호가 port 외부로 KIS DTO를 노출하지 않는다.
- `StockPriceSubscriptionService` 코드는 변경하지 않고 KIS adapter로 교체 가능하다.
- `kis.enabled=false`일 때는 no-op client가 사용된다.
- `kis.enabled=true`일 때만 KIS client가 사용된다.

### 7단계: iOS stream 이벤트 타입 설계

목표:

- iOS가 체결가 이벤트와 호가 이벤트를 구분할 수 있게 stream 응답 타입을 설계한다.

예시:

```json
{
  "type": "TRADE_PRICE",
  "stockCode": "005930",
  "currentPrice": 75100,
  "changePrice": 100,
  "changeRate": 0.13,
  "tradeVolume": 1234567,
  "priceChangedAt": "2026-05-25T09:00:01"
}
```

```json
{
  "type": "ORDER_BOOK",
  "stockCode": "005930",
  "bestAskPrice": 75200,
  "bestAskVolume": 3000,
  "bestBidPrice": 75100,
  "bestBidVolume": 2500,
  "spread": 100,
  "orderBookChangedAt": "2026-05-25T09:00:01"
}
```

완료 기준:

- 주식 리스트는 `TRADE_PRICE`로 현재가를 갱신한다.
- 주식 리스트는 `ORDER_BOOK`으로 최우선 매도/매수 호가를 갱신한다.
- 종목 상세는 `ORDER_BOOK`으로 호가창을 갱신한다.

### 8단계: 구독 시작 트리거 결정

아직 자동 시작은 바로 넣지 않는다. 먼저 수동 실행 또는 내부 테스트용 endpoint 없이 application runner 수준에서 제한적으로 검증한다.

후보:

| 방식 | 장점 | 단점 |
|------|------|------|
| `ApplicationReadyEvent` | 서버 시작 후 자동 구독 | KIS 장애가 부팅 흐름에 영향을 줄 수 있음 |
| 관리용 internal endpoint | 수동으로 시작 가능 | 운영 보안 정책 필요 |
| scheduler 또는 market open trigger | 장 운영 시간에 맞추기 좋음 | 초기 구현 복잡도 증가 |

초기 추천:

```text
KIS adapter 구현
→ 테스트 profile에서는 비활성화
→ local에서 환경변수 설정 후 수동으로 subscribeAllStocks() 호출 경로 검증
→ 안정화 후 ApplicationReadyEvent 또는 운영 트리거 결정
```

## 테스트 전략

단위 테스트:

- `KisProperties` 바인딩 테스트
- `KisTradePriceMessageParser` fixture 테스트
- `KisOrderBookMessageParser` fixture 테스트
- approval key 응답 파싱 테스트
- WebSocket 구독 메시지 생성 테스트

통합 또는 수동 테스트:

1. KIS App Key, App Secret 환경변수 설정
2. `kis.enabled=true`
3. DB에 `005930`, `000660` 종목 존재 확인
4. `StockPriceSubscriptionService.subscribeAllStocks()` 실행
5. KIS WebSocket 체결가/호가 구독 요청 확인
6. 체결가 이벤트가 `StockRealtimePriceService.handle(event)`까지 전달되는지 확인
7. `stocks.current_price`, `stocks.price_changed_at` 갱신 확인
8. `StockPricePublisher.publish(event)` 호출 확인
9. 호가 이벤트가 `StockOrderBookPublisher.publish(event)`까지 전달되는지 확인

## 에러 처리 정책

| 상황 | 처리 방향 |
|------|-----------|
| App Key/App Secret 누락 | KIS adapter 활성화 시 명확한 예외 |
| approval key 발급 실패 | 구독 시작 실패, secret 마스킹 로그 |
| WebSocket 연결 실패 | backoff 후 reconnect 후보 |
| 구독 메시지 전송 실패 | 해당 종목 구독 실패 로그, 재시도 후보 |
| 파싱 실패 | 해당 메시지만 폐기 |
| 알 수 없는 종목 이벤트 | `StockRealtimePriceService`에서 `STOCK_NOT_FOUND` 처리 |
| KIS 장애 | 마지막 DB 캐시 유지, 장애 상태 관측 |

## 보안 규칙

- App Key, App Secret, approval key는 git에 커밋하지 않는다.
- secret은 `application.yaml`에 직접 쓰지 않는다.
- 운영 로그에는 token, approval key, secret, account 정보를 남기지 않는다.
- iOS는 KIS API를 직접 호출하지 않는다.
- Backend만 KIS와 통신하고, iOS는 Backend stream API만 사용한다.

## 열어둔 결정

- 실전/모의 WebSocket URL과 path 최종값
- KIS WebSocket 재연결 backoff 정책
- 구독 가능한 종목 수 제한과 chunk 처리 방식
- 서버 시작 시 자동 구독 여부
- 장 종료 후 가격 표시와 stream 유지 정책
- 호가 10단계를 모두 iOS 리스트까지 보낼지, 종목 상세에서만 보낼지 결정
- 체결가와 호가를 하나의 WebSocket 연결에서 함께 구독할지, 연결을 분리할지 결정
