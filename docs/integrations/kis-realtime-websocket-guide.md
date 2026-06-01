# KIS 실시간 시세 WebSocket 연동

이 문서는 Tumo Backend가 한국투자증권 KIS Open API WebSocket을 통해 국내주식 실시간 체결가와 실시간 호가를 수신하고, iOS 클라이언트에 SSE로 전달하는 현재 구현 구조를 정리한다.

## 전체 흐름

KIS 연동은 두 방향으로 나뉜다.

```text
KIS → Backend
Backend → iOS
```

Backend는 KIS WebSocket을 구독해 실시간 데이터를 받고, iOS는 Backend의 SSE stream에 연결해 가공된 이벤트를 받는다.

```text
KIS WebSocket
→ KisRealtimeWebSocketClient
→ KisWebSocketMessageDispatcher
→ KisTradePriceMessageParser / KisOrderBookMessageParser
→ StockPriceEvent / StockOrderBookEvent
→ StockRealtimePriceService / StockOrderBookService
→ StockPricePublisher / StockOrderBookPublisher
→ SseStockPricePublisher / SseStockOrderBookPublisher
→ iOS SSE stream
```

## 사용하는 KIS TR

| TR ID | 용도 | Tumo 도메인 모델 | Backend 처리 |
|-------|------|------------------|--------------|
| `H0STCNT0` | 국내주식 실시간체결가 | `StockPrice`, `StockPriceEvent` | 종목 현재가 갱신, 가격 SSE 발행 |
| `H0STASP0` | 국내주식 실시간호가 | `StockOrderBook`, `StockOrderBookEvent` | 호가 SSE 발행 |

API Portal URL은 문서/테스트 페이지다. 실제 WebSocket 요청에서는 URL 자체가 아니라 TR ID를 구독 메시지의 `tr_id`로 사용한다.

```text
체결가 구독: tr_id = H0STCNT0, tr_key = 005930
호가 구독: tr_id = H0STASP0, tr_key = 005930
```

## Backend 내부 KIS 구독 API

이 API들은 iOS가 실시간 데이터를 받기 위한 endpoint가 아니다. Backend가 KIS에서 실시간 데이터를 받기 시작하도록 트리거하는 내부 API다.

### 구독 상태 조회

```http
GET /api/v1/internal/stocks/realtime/subscriptions
```

응답 예시:

```json
{
  "priceStockCodes": ["005930", "000660"],
  "orderBookStockCodes": ["005930"]
}
```

### 실시간 체결가 구독 시작

```http
POST /api/v1/internal/stocks/realtime/subscribe
```

동작:

```text
StockRealtimeController
→ StockPriceSubscriptionService.subscribeAllStocks()
→ DB에 저장된 모든 Stock 조회
→ 이미 구독 중인 종목 제외
→ StockRealtimePriceClient.subscribe(...)
→ KisRealtimeWebSocketClient
→ H0STCNT0 구독 메시지 전송
```

### 실시간 호가 구독 시작

```http
POST /api/v1/internal/stocks/realtime/order-books/subscribe
```

동작:

```text
StockRealtimeController
→ StockOrderBookSubscriptionService.subscribeAllStocks()
→ DB에 저장된 모든 Stock 조회
→ 이미 구독 중인 종목 제외
→ StockRealtimeOrderBookClient.subscribe(...)
→ KisRealtimeWebSocketClient
→ H0STASP0 구독 메시지 전송
```

## iOS 실시간 stream API

iOS는 KIS에 직접 연결하지 않는다. iOS는 Backend SSE endpoint에 연결하고, Backend가 발행하는 이벤트를 수신한다.

### 가격 stream

전체 종목 가격을 받는 경우:

```http
GET /api/v1/stocks/realtime/prices/stream
Accept: text/event-stream
```

관심 종목만 받는 경우:

```http
GET /api/v1/stocks/realtime/prices/stream?stockCodes=005930&stockCodes=000660
Accept: text/event-stream
```

사용 화면:

```text
전체 주식 리스트
즐겨찾기 목록
내 보유 주식 목록
```

가격 이벤트 이름:

```text
event: stock-price
```

### 호가 stream

호가는 특정 종목 상세/주문 화면에서 사용하므로 단일 종목 기준으로 연결한다.

```http
GET /api/v1/stocks/{stockCode}/realtime/order-book/stream
Accept: text/event-stream
```

예시:

```http
GET /api/v1/stocks/005930/realtime/order-book/stream
Accept: text/event-stream
```

사용 화면:

```text
종목 상세 화면
주문 화면
```

호가 이벤트 이름:

```text
event: stock-order-book
```

### Heartbeat

가격/호가 stream 모두 30초마다 heartbeat 이벤트를 보낸다.

```text
event: heartbeat
data: ok
```

목적:

- 이벤트가 없는 시간에도 SSE 연결이 idle 상태로 방치되지 않게 한다.
- 끊어진 연결을 heartbeat 전송 실패로 빠르게 감지한다.
- 실패한 emitter는 registry에서 제거한다.

현재 SSE timeout은 30분이다. iOS는 화면에 진입할 때 SSE 연결을 열고, 화면에서 나갈 때 연결을 명시적으로 닫아야 한다.

## Backend 구현 구조

### KIS adapter

```text
com.tumo.stock.adapter.out.kis
├── config/
│   ├── KisConfiguration
│   └── KisProperties
├── rest/
│   ├── auth/
│   │   ├── KisAccessTokenClient
│   │   ├── KisAccessTokenRequest
│   │   └── KisAccessTokenResponse
│   ├── client/
│   │   ├── KisRestClient
│   │   └── KisRestRequest
│   └── quotation/
│       ├── KisInquirePriceRequest
│       ├── KisInquirePriceResponse
│       └── KisStockPriceQueryClient
└── websocket/
    ├── auth/
    │   ├── KisApprovalKeyClient
    │   ├── KisApprovalKeyRequest
    │   └── KisApprovalKeyResponse
    ├── client/KisRealtimeWebSocketClient
    ├── dispatcher/KisWebSocketMessageDispatcher
    ├── message/KisWebSocketMessageSender
    ├── message/KisWebSocketSubscribeMessage
    ├── parser/KisTradePriceMessageParser
    ├── parser/KisOrderBookMessageParser
    └── session/KisWebSocketSessionManager
```

| 클래스 | 책임 |
|--------|------|
| `KisProperties` | KIS App Key, App Secret, REST URL, WebSocket URL, TR ID 설정 바인딩 |
| `KisAccessTokenClient` | KIS REST API 호출에 필요한 access token 발급 |
| `KisRestClient` | KIS REST API 공통 header 적용과 HTTP 요청 실행 |
| `KisRestRequest` | KIS REST API path, TR ID, query parameter, 응답 타입을 묶는 요청 값 |
| `KisStockPriceQueryClient` | KIS REST 현재가 조회 API를 `StockPriceQueryPort`로 구현 |
| `KisApprovalKeyClient` | WebSocket 접속에 필요한 approval key 발급 |
| `KisRealtimeWebSocketClient` | KIS WebSocket 연결, 체결가/호가 구독/해제, raw message callback 연결 |
| `KisWebSocketMessageSender` | 구독 메시지를 JSON으로 변환해 WebSocket으로 전송 |
| `KisWebSocketSessionManager` | WebSocket 연결 생성과 수신 message callback 관리 |
| `KisWebSocketMessageDispatcher` | raw message를 체결가/호가 parser로 분배 |
| `KisTradePriceMessageParser` | `H0STCNT0` raw message를 `StockPriceEvent`로 변환 |
| `KisOrderBookMessageParser` | `H0STASP0` raw message를 `StockOrderBookEvent`로 변환 |

### SSE adapter

```text
com.tumo.stock.adapter.out.sse
├── price/
│   ├── SseStockPricePublisher
│   └── StockPriceSseEmitterRegistry
├── orderbook/
│   ├── SseStockOrderBookPublisher
│   └── StockOrderBookSseEmitterRegistry
└── heartbeat/
    └── StockRealtimeSseHeartbeatScheduler
```

| 클래스 | 책임 |
|--------|------|
| `SseStockPricePublisher` | 처리된 가격 이벤트를 가격 SSE registry에 전달 |
| `SseStockOrderBookPublisher` | 처리된 호가 이벤트를 호가 SSE registry에 전달 |
| `StockPriceSseEmitterRegistry` | 전체 또는 관심 종목 가격 SSE 연결 관리 |
| `StockOrderBookSseEmitterRegistry` | 특정 종목 호가 SSE 연결 관리 |
| `StockRealtimeSseHeartbeatScheduler` | 가격/호가 SSE 연결에 30초마다 heartbeat 전송 |

### Stock port

```text
com.tumo.stock.port
├── client/
│   ├── StockRealtimePriceClient
│   └── StockRealtimeOrderBookClient
├── handler/
│   ├── StockPriceEventHandler
│   └── StockOrderBookEventHandler
├── publisher/
│   ├── StockPricePublisher
│   └── StockOrderBookPublisher
└── query/
    └── StockPriceQueryPort
```

| 패키지 | 책임 |
|--------|------|
| `client` | 외부 실시간 시세 provider 구독/해제 계약 |
| `handler` | 외부 provider가 수신 이벤트를 Backend use case로 넘길 callback 계약 |
| `publisher` | 처리된 실시간 이벤트를 클라이언트 전송 adapter로 발행하는 계약 |
| `query` | REST API나 캐시를 통한 단건 현재가 조회 계약 |

### Stock service

```text
com.tumo.stock.service
├── query/
│   ├── StockService
│   └── StockRealtimeSubscriptionQueryService
├── realtime/
│   ├── StockRealtimePriceService
│   └── StockOrderBookService
├── registry/
│   └── StockRealtimeSubscriptionRegistry
└── subscription/
    ├── StockPriceSubscriptionService
    └── StockOrderBookSubscriptionService
```

| 패키지 | 책임 |
|--------|------|
| `query` | 조회성 use case 처리 |
| `realtime` | KIS에서 들어온 실시간 가격/호가 이벤트 처리 |
| `registry` | Backend가 현재 구독 중인 종목 상태 관리 |
| `subscription` | DB 종목 목록을 기준으로 외부 provider 구독 시작 |

## 설정

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
  trade-price-tr-id: ${KIS_TRADE_PRICE_TR_ID:H0STCNT0}
  order-book-tr-id: ${KIS_ORDER_BOOK_TR_ID:H0STASP0}
```

`kis.enabled=false`이면 KIS adapter 대신 no-op client가 사용된다.

실제 KIS 연동을 실행하려면 최소한 다음 환경변수가 필요하다.

```text
KIS_APP_KEY
KIS_APP_SECRET
```

실전/모의 URL은 KIS 계정 환경과 공식 문서를 기준으로 확인한다.

## 구현 원칙

- KIS 원본 메시지 포맷은 adapter 내부에서만 다룬다.
- Service와 Domain은 KIS class를 import하지 않는다.
- 체결가와 호가는 parser, domain event, service, publisher를 분리한다.
- KIS App Key, App Secret, approval key는 로그에 남기지 않는다.
- iOS는 KIS에 직접 연결하지 않는다.
- 가격 stream은 전체/관심 종목 목록을 지원한다.
- 호가 stream은 특정 종목 단건을 지원한다.

## 수동 검증 순서

1. KIS 환경변수를 설정한다.
2. `kis.enabled=true`로 Backend를 실행한다.
3. iOS 또는 curl로 SSE stream을 먼저 연결한다.
4. 내부 API로 KIS 구독을 시작한다.
5. SSE stream에서 `stock-price`, `stock-order-book`, `heartbeat` 이벤트를 확인한다.

예시:

```bash
curl -N http://localhost:8080/api/v1/stocks/realtime/prices/stream
```

```bash
curl -N http://localhost:8080/api/v1/stocks/005930/realtime/order-book/stream
```

```bash
curl -i -X POST http://localhost:8080/api/v1/internal/stocks/realtime/subscribe
```

```bash
curl -i -X POST http://localhost:8080/api/v1/internal/stocks/realtime/order-books/subscribe
```

## 참고 자료

- KIS Open API 샘플 저장소: https://github.com/koreainvestment/open-trading-api
- 국내주식 WebSocket 예제: `examples_user/domestic_stock/domestic_stock_examples_ws.py`
- 국내주식 WebSocket 함수: `examples_user/domestic_stock/domestic_stock_functions_ws.py`
- KIS 설정 예시: `kis_devlp.yaml`
