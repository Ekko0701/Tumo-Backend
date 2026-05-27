# 실시간 주식 시세 연동 작업 계획

이 문서는 Tumo Backend에 한국투자증권 KIS WebSocket 기반 실시간 주식 시세를 연동하기 위한 작업 계획을 정리한다.

목표는 주식 리스트 화면에서 각 종목의 가격이 실시간으로 변하고, 주문 체결 시점에도 최신 기준 가격을 사용하도록 만드는 것이다.

## 배경

현재 Backend는 `stocks.current_price`를 기준으로 다음 기능을 처리한다.

- 종목 목록 조회
- 종목 상세 조회
- 매수 주문 체결가 결정
- 포트폴리오 평가금액 계산

하지만 `current_price`는 seed 데이터 또는 마지막 저장 값에 가깝다. 모의 주식 투자 서비스에서는 사용자가 주식 리스트를 보는 동안에도 가격이 변해야 하므로, 외부 실시간 시세를 Backend로 받아 iOS 클라이언트에 지속적으로 전달해야 한다.

## 핵심 원칙

### Clean Architecture

- Domain은 KIS, HTTP, WebSocket, SSE, DB 세부 구현에 의존하지 않는다.
- Application Service는 use case 흐름을 조합하고, 외부 시스템은 port interface 뒤로 숨긴다.
- Infrastructure adapter가 KIS WebSocket, 인증, 메시지 파싱, reconnect를 담당한다.
- Controller는 요청과 응답 또는 stream 연결만 담당한다.

### SOLID

- 단일 책임: 가격 수신, 가격 저장, 가격 broadcast, 주문 체결을 분리한다.
- 개방 폐쇄: KIS 외 다른 시세 provider로 교체 가능해야 한다.
- 의존성 역전: 핵심 서비스는 `StockRealtimePriceClient`, `StockPricePublisher` 같은 추상화에 의존한다.
- 인터페이스 분리: 현재가 단건 조회와 실시간 stream 구독을 같은 interface에 억지로 묶지 않는다.

### OOP

- `Stock`은 자신의 가격 상태를 변경하는 `updatePrice(...)` 메서드를 계속 책임진다.
- 시세 이벤트는 값 객체로 모델링한다.
- 주문, 보유, 사용자 잔고 변경은 각 도메인 객체의 행위 메서드를 통해 처리한다.

## 목표 아키텍처

```text
KIS WebSocket
→ KisRealtimeWebSocketClient
→ StockRealtimePriceService
→ StockPricePublisher
→ StockRealtimeStreamController
→ iOS Stock List
```

주문 흐름은 별도로 최신 가격 확정 단계를 갖는다.

```text
OrderController
→ OrderService
→ StockPriceQueryPort
→ KIS 현재가 조회 또는 최신 캐시 조회
→ Stock.updatePrice(...)
→ Order 저장
```

## 시세 이벤트 처리 책임 흐름

KIS에서 가격 변동 메시지가 들어오면 adapter가 원본 메시지를 도메인 이벤트로 변환하고, application service가 해당 이벤트를 처리한다.

```text
KIS raw message
→ KisWebSocketMessageParser
→ StockPriceEvent
→ StockRealtimePriceService
→ StockRepository.findByStockCode(...)
→ Stock.updatePrice(...)
→ StockRepository.save(...)
→ StockPricePublisher.publish(...)
```

각 객체의 책임은 다음과 같이 분리한다.

| 객체 | 책임 |
|------|------|
| `KisWebSocketMessageParser` | KIS 원본 메시지를 Backend 도메인 이벤트로 변환한다. KIS 필드명, 구분자, 메시지 포맷은 adapter 내부에서 끝낸다. |
| `StockPriceEvent` | 가격 변경이 발생했다는 사건을 표현한다. 내부에 `StockPrice`를 포함할 수 있다. |
| `StockPrice` | 특정 시점의 종목 가격 상태를 표현하는 값 객체다. DB 조회, 저장, 외부 API 호출을 알지 않는다. |
| `StockRealtimePriceService` | 가격 이벤트 처리 use case를 조율한다. 종목 조회, 가격 변경, 저장, publish를 순서대로 수행한다. |
| `Stock` | 종목 엔티티다. 자신의 가격 상태 변경은 `updatePrice(...)` 메서드로 직접 책임진다. |
| `StockPricePublisher` | 처리된 가격 이벤트를 iOS stream 구독자에게 전달한다. SSE, WebSocket 같은 전송 방식은 구현체 세부사항이다. |

`StockPriceEvent`나 `StockPrice`가 직접 `Stock`을 변경하지 않는다. 이벤트와 값 객체는 상태와 사실을 표현하고, 실제 use case 흐름은 `StockRealtimePriceService`가 담당한다. 이렇게 해야 KIS가 아닌 다른 provider가 추가되어도 주문, 포트폴리오, 종목 도메인으로 외부 API 세부사항이 새지 않는다.

### Handler와 Adapter 흐름

`StockPriceEventHandler`는 실시간 가격 이벤트가 들어왔을 때 실행할 callback interface다.

```java
@FunctionalInterface
public interface StockPriceEventHandler {

    void handle(StockPriceEvent event);
}
```

`StockRealtimePriceClient`는 handler를 파라미터로 받는다.

```java
void subscribe(Collection<String> stockCodes, StockPriceEventHandler handler);
```

이 구조의 의미는 다음과 같다.

```text
이 종목들을 구독한다.
그리고 가격 이벤트가 들어올 때마다 전달받은 handler를 호출한다.
```

Java 문법상 handler는 다음 세 가지 방식으로 전달할 수 있다.

```java
stockRealtimePriceClient.subscribe(stockCodes, event -> stockRealtimePriceService.handle(event));
```

```java
stockRealtimePriceClient.subscribe(stockCodes, stockRealtimePriceService::handle);
```

```java
stockRealtimePriceClient.subscribe(stockCodes, new StockPriceEventHandler() {
    @Override
    public void handle(StockPriceEvent event) {
        stockRealtimePriceService.handle(event);
    }
});
```

위 세 코드는 모두 같은 의미다. 가격 이벤트가 들어오면 `stockRealtimePriceService.handle(event)`를 실행한다.

특히 다음 코드는 Java의 메서드 참조 문법이다.

```java
stockRealtimePriceService::handle
```

이 코드는 `StockPriceEvent`가 들어오면 `stockRealtimePriceService` 객체의 `handle(...)` 메서드에 이벤트를 전달해 실행하겠다는 뜻이다.

```java
event -> stockRealtimePriceService.handle(event)
```

실제로 adapter 내부에서는 전달받은 handler를 다음처럼 호출한다.

```java
StockPriceEvent event = parse(rawMessage);
handler.handle(event);
```

이때 handler가 `stockRealtimePriceService::handle`로 전달되어 있었다면, 위 코드는 결과적으로 다음 코드와 같다.

```java
stockRealtimePriceService.handle(event);
```

즉 `stockRealtimePriceService::handle`은 메서드를 즉시 실행하는 코드가 아니라, 나중에 이벤트가 도착했을 때 실행할 메서드를 callback으로 넘기는 코드다.

`KisRealtimePriceClient`는 `StockRealtimePriceClient`의 KIS adapter 구현체가 된다. adapter는 KIS WebSocket 연결, 인증, 구독 메시지 전송, 원본 메시지 수신, 파싱, `StockPriceEvent` 생성을 담당한다. 하지만 `Stock` 엔티티 조회, DB 저장, 주문 체결, 포트폴리오 평가는 담당하지 않는다.

adapter가 application service를 직접 의존하지 않고 handler만 호출하는 이유는 KIS 구현체와 use case 처리를 느슨하게 연결하기 위해서다.

```text
StockRealtimePriceService
→ stockRealtimePriceClient.subscribe(stockCodes, this::handle)
→ KisRealtimePriceClient가 KIS 메시지 수신
→ KisRealtimePriceClient가 StockPriceEvent 생성
→ KisRealtimePriceClient가 handler.handle(event) 호출
→ 결과적으로 StockRealtimePriceService.handle(event) 실행
```

### 구독 요청과 이벤트 생성 시점

`StockPriceSubscriptionService`는 DB에 저장된 모든 종목 코드를 조회해 `StockRealtimePriceClient`에 구독을 요청한다.

```java
List<String> stockCodes = stockRepository.findAll().stream()
        .map(Stock::getStockCode)
        .toList();

stockRealtimePriceClient.subscribe(stockCodes, stockRealtimePriceService::handle);
```

이때 첫 번째 인자인 `stockCodes`는 KIS WebSocket에 어떤 종목들을 구독할지 알려주는 값이다. 두 번째 인자인 `stockRealtimePriceService::handle`은 KIS에서 가격 이벤트가 도착했을 때 실행할 callback이다.

중요한 점은 `subscribe(...)` 호출 시점에 `StockPriceEvent`를 바로 만드는 것이 아니라는 점이다. `subscribe(...)`는 구독 요청만 수행한다. 이후 KIS WebSocket에서 실제 실시간 메시지가 도착할 때마다 `KisRealtimePriceClient`가 메시지를 파싱해 `StockPriceEvent`를 하나씩 생성한다.

```text
StockPriceSubscriptionService
→ stockCodes = ["005930", "000660"]
→ stockRealtimePriceClient.subscribe(stockCodes, stockRealtimePriceService::handle)
→ KisRealtimePriceClient가 "005930" 구독 요청
→ KisRealtimePriceClient가 "000660" 구독 요청
```

이후 메시지 수신 흐름:

```text
KIS 메시지: 09:00:01 / 005930 / 현재가 75100
→ KisRealtimePriceClient가 StockPriceEvent(005930, 75100) 생성
→ handler.handle(event)
→ StockRealtimePriceService.handle(event)

KIS 메시지: 09:00:02 / 000660 / 현재가 180500
→ KisRealtimePriceClient가 StockPriceEvent(000660, 180500) 생성
→ handler.handle(event)
→ StockRealtimePriceService.handle(event)

KIS 메시지: 09:00:03 / 005930 / 현재가 75200
→ KisRealtimePriceClient가 StockPriceEvent(005930, 75200) 생성
→ handler.handle(event)
→ StockRealtimePriceService.handle(event)
```

따라서 `stockCodes`는 구독 대상을 결정하는 입력이고, `handler`는 나중에 생성되는 각 `StockPriceEvent`를 처리할 실행 경로다.

### 책임 분리 상세

실시간 시세 처리에서 각 계층의 책임은 다음과 같다.

| 대상 | 해야 하는 일 | 하지 말아야 할 일 |
|------|--------------|-------------------|
| `KisRealtimePriceClient` | KIS 연결, 인증, 구독, 메시지 수신, 파싱, `StockPriceEvent` 생성, handler 호출 | `Stock` 조회, DB 저장, 주문 체결, 포트폴리오 평가, iOS 응답 정책 결정 |
| `StockRealtimePriceService` | `Stock` 조회, `Stock.updatePrice(...)` 호출, 마지막 가격 캐시 저장, `StockPricePublisher.publish(event)` 호출 | 주문 체결, 포트폴리오 평가, SSE/WebSocket 응답 DTO 결정 |
| `OrderService` | 사용자 주문 요청 처리, 주문 직전 최신 가격 확정, 잔고 검증, 주문/보유/잔고 갱신 | 실시간 WebSocket 구독, iOS stream 발행 |
| `PortfolioService` | 사용자 보유 종목 기준 평가금액 계산 | 실시간 이벤트 수신, 주문 체결 |
| `StockPricePublisher` 구현체 | 처리된 가격 이벤트를 SSE 또는 WebSocket 구독자에게 전달 | KIS 메시지 파싱, `Stock` 가격 변경, 주문/포트폴리오 계산 |

따라서 `StockRealtimePriceService.handle(event)`는 실시간 가격 이벤트로 인해 즉시 필요한 최소 작업만 수행한다.

```text
Stock 조회
→ Stock.updatePrice(...)
→ DB 마지막 가격 캐시 갱신
→ StockPricePublisher.publish(event)
```

주문과 포트폴리오는 이 handler 내부에서 실행하지 않는다. 주문은 사용자의 주문 요청 시 `OrderService`가 처리하고, 포트폴리오는 사용자의 포트폴리오 조회 시 `PortfolioService`가 처리한다.

### 세부 플로우

실시간 시세 반영 플로우:

```text
KIS WebSocket
→ KisRealtimePriceClient
→ KisWebSocketMessageParser
→ StockPrice
→ StockPriceEvent
→ StockPriceEventHandler.handle(event)
→ StockRealtimePriceService.handle(event)
→ StockRepository.findByStockCode(...)
→ Stock.updatePrice(...)
→ StockRepository.save(...)
→ StockPricePublisher.publish(event)
→ SSE 또는 WebSocket adapter
→ iOS 주식 리스트 가격 갱신
```

주문 가격 확정 플로우:

```text
iOS 주문 요청
→ OrderController
→ OrderService.buy(...)
→ User 조회
→ Stock 조회
→ StockPriceQueryPort.findCurrentPrice(stockCode)
→ 최신 가격 확정
→ Stock.updatePrice(...)
→ 잔고 검증
→ Order 저장
→ Holding 생성 또는 갱신
→ User cashBalance 변경
→ 주문 응답 반환
```

포트폴리오 평가 플로우:

```text
iOS 포트폴리오 조회 요청
→ PortfolioController
→ PortfolioService
→ 사용자 Holding 목록 조회
→ 각 보유 종목의 최신 가격 확인
→ 평가금액, 평가손익, 수익률 계산
→ 포트폴리오 응답 반환
```

핵심 원칙은 다음과 같다.

```text
실시간 이벤트는 가격 캐시를 최신화한다.
주문과 포트폴리오는 최신 가격을 필요할 때 사용한다.
```

## Backend 패키지 계획

초기 구현은 `stock` bounded context 내부에 둔다.

```text
com.tumo.stock
├── application
│   ├── StockRealtimePriceService
│   ├── StockPriceQueryService
│   └── StockPriceSubscriptionService
├── domain
│   ├── Stock
│   ├── StockPrice
│   └── StockPriceEvent
├── port
│   ├── StockRealtimePriceClient
│   ├── StockPriceQueryPort
│   └── StockPricePublisher
├── adapter
│   ├── in
│   │   └── StockRealtimeStreamController
│   └── out
│       └── kis
│           ├── KisRealtimeWebSocketClient
│           ├── KisStockPriceQueryClient
│           ├── KisWebSocketMessageParser
│           ├── KisAccessTokenClient
│           └── KisProperties
└── dto
    └── StockPriceStreamResponse
```

현재 프로젝트가 레이어드 구조로 시작했기 때문에, 패키지는 한 번에 대규모 개편하지 않는다. 실시간 시세 연동부터 port/adapter 구조를 적용하고, 이후 stock 영역을 점진적으로 정리한다.

## API 계획

### 종목 목록 조회

기존 API는 유지한다.

```http
GET /api/v1/stocks
```

역할:

- 최초 리스트 구성을 위한 snapshot 제공
- 각 종목의 마지막 가격 캐시 포함

### 실시간 시세 스트림

iOS 클라이언트로 실시간 가격을 전달하는 endpoint를 추가한다.

```http
GET /api/v1/stocks/realtime/prices/stream
```

초기 방식은 Server-Sent Events를 우선 검토한다.

이유:

- 주식 리스트는 서버에서 클라이언트로 가격을 push하는 단방향 흐름이다.
- 주문, 로그인, 포트폴리오는 기존 REST API와 분리할 수 있다.
- iOS 구현이 WebSocket보다 단순하다.
- Backend 내부는 KIS WebSocket을 사용하되, 클라이언트 계약은 독립적으로 유지할 수 있다.

단, 다음 요구가 생기면 iOS와 Backend 사이도 WebSocket으로 전환할 수 있다.

- 클라이언트별 구독 종목 동적 변경
- 호가, 체결, 체결량 등 다중 stream 통합
- 양방향 control message 필요

### Stream 응답 예시

```json
{
  "stockCode": "005930",
  "stockName": "삼성전자",
  "market": "KOSPI",
  "currentPrice": 75100,
  "changePrice": 100,
  "changeRate": 0.13,
  "tradeVolume": 1234567,
  "priceChangedAt": "2026-05-24T20:55:00"
}
```

## 데이터 모델 계획

기존 `stocks` 테이블은 마지막 시세 캐시로 사용한다.

```text
stocks.current_price
stocks.price_changed_at
```

추가 검토 컬럼:

```text
previous_close_price
change_price
change_rate
trade_volume
```

초기 구현에서는 DB 스키마 변경을 최소화하고, 화면에 꼭 필요한 값부터 추가한다.

## 구현 단계

### 0단계: 1차 작업 상세 가이드

KIS WebSocket 구현 전에 Backend 내부 시세 경계를 먼저 만든다. 이번 작업의 목표는 외부 provider 없이도 실시간 시세 이벤트를 도메인 언어로 표현하고 테스트할 수 있게 하는 것이다.

작업 범위:

- `StockPrice` 값 객체 추가
- `StockPriceEvent` 이벤트 모델 추가
- `StockPriceQueryPort` interface 추가
- `StockRealtimePriceClient` interface 추가
- `StockPricePublisher` interface 추가
- `StockRealtimePriceService` skeleton 추가
- fake client 또는 fake publisher 기반 단위 테스트 추가

이번 작업에서 제외할 것:

- KIS WebSocket 연결
- KIS access token 발급
- KIS 원본 message parser
- iOS stream controller
- 주문 체결 로직 변경
- 포트폴리오 평가 로직 변경
- DB schema 변경

추천 구현 순서:

1. `stock/domain`에 `StockPrice`를 추가한다.
2. `stock/domain`에 `StockPriceEvent`를 추가한다.
3. `stock/port`에 단건 현재가 조회용 `StockPriceQueryPort`를 추가한다.
4. `stock/port`에 실시간 시세 수신용 `StockRealtimePriceClient`를 추가한다.
5. `stock/port`에 구독자 broadcast용 `StockPricePublisher`를 추가한다.
6. `stock/application`에 `StockRealtimePriceService` skeleton을 추가한다.
7. fake publisher를 사용해 이벤트 처리 흐름 단위 테스트를 작성한다.

완료 기준:

- domain과 port는 Spring, WebSocket, HTTP, KIS 구현체에 의존하지 않는다.
- application service는 port interface만 의존한다.
- 단위 테스트에서 fake 구현체를 쉽게 주입할 수 있다.
- 이번 커밋만으로 KIS 없이 컴파일과 테스트가 가능하다.

추천 커밋 메시지:

```text
feat: add stock price realtime ports
```

### 1단계: KIS 설정과 보안

- KIS app key, app secret, account 관련 설정을 `application.yaml`에 추가한다.
- 민감 정보는 환경변수 override를 지원한다.
- `KisProperties`로 설정을 타입 안전하게 바인딩한다.
- 운영 URL과 모의투자 URL을 profile로 분리한다.
- KIS 공식 문서에서 WebSocket 인증 방식, 실시간 시세 message format, rate limit을 구현 직전에 재확인한다.

완료 기준:

- 설정 값 누락 시 애플리케이션이 명확한 오류로 실패한다.
- 테스트에서는 실제 secret 없이 fake properties를 사용할 수 있다.

### 2단계: Domain model과 port 정의

- `StockPrice` 값 객체를 추가한다.
- `StockPriceEvent`를 추가한다.
- `StockRealtimePriceClient` interface를 정의한다.
- `StockPriceQueryPort` interface를 정의한다.
- `StockPricePublisher` interface를 정의한다.

완료 기준:

- application service는 KIS 구현체를 import하지 않는다.
- 단위 테스트에서 fake client와 fake publisher를 주입할 수 있다.

### 3단계: KIS WebSocket adapter 구현

- `KisRealtimeWebSocketClient`를 구현한다.
- KIS WebSocket 연결, 인증, 종목 구독, message 수신을 담당한다.
- `KisWebSocketMessageParser`로 raw message 파싱을 분리한다.
- reconnect 정책을 구현한다.
- 연결 실패, 인증 실패, 파싱 실패는 로그와 metric으로 관측 가능하게 만든다.

완료 기준:

- 한두 개 종목 예: `005930`, `000660`을 구독해 가격 이벤트를 받을 수 있다.
- raw message parser는 fixture 기반 단위 테스트를 가진다.
- WebSocket 연결 장애가 application service를 직접 깨뜨리지 않는다.

### 4단계: 실시간 가격 application service 구현

- `StockRealtimePriceService`가 `StockRealtimePriceClient`에서 이벤트를 받는다.
- 수신한 가격을 `Stock.updatePrice(...)`로 DB 캐시에 반영한다.
- `StockPricePublisher`를 통해 구독자에게 이벤트를 broadcast한다.
- DB 저장 실패와 broadcast 실패의 처리 정책을 분리한다.

완료 기준:

- 가격 이벤트 수신 시 stock 가격이 갱신된다.
- 알 수 없는 종목 코드는 명확하게 무시하거나 로그 처리한다.
- 하나의 종목 처리 실패가 전체 stream을 중단하지 않는다.

### 5단계: iOS 구독용 stream API 구현

- `StockRealtimeStreamController`를 추가한다.
- `GET /api/v1/stocks/realtime/prices/stream` endpoint를 제공한다.
- `GET /api/v1/stocks/{stockCode}/realtime/order-book/stream` endpoint를 제공한다.
- JWT 인증을 유지한다.
- client disconnect 시 구독 resource를 정리한다.
- heartbeat event를 추가해 유휴 연결을 유지한다.

완료 기준:

- iOS 또는 curl로 stream을 열면 가격 이벤트가 지속적으로 수신된다.
- 연결 종료 시 서버 resource가 누수되지 않는다.
- 인증되지 않은 요청은 거절된다.

### 6단계: 주문 체결 가격 확정

- `OrderService.buy(...)`에서 주문 직전 최신 가격을 확정한다.
- 실시간 캐시가 충분히 최신이면 캐시를 사용한다.
- 캐시가 없거나 오래됐으면 `StockPriceQueryPort`로 단건 현재가를 조회한다.
- 확정된 가격으로 `Order`와 `Holding`을 갱신한다.

완료 기준:

- 주문 체결가는 화면에 마지막으로 보인 값이 아니라 Backend가 확정한 최신 가격이다.
- 가격 확정 실패 시 주문은 실패한다.
- 주문 실패 시 잔고, 보유 종목, 주문 내역이 부분 저장되지 않는다.

### 7단계: 포트폴리오 평가금액 최신화

- `PortfolioService`가 보유 종목의 최신 가격을 사용하도록 정리한다.
- 실시간 캐시를 우선 사용하고, 필요 시 단건 조회 fallback을 둔다.
- 평가금액, 평가손익, 수익률 계산 기준을 문서화한다.

완료 기준:

- 포트폴리오 조회 시 오래된 seed 가격만으로 평가하지 않는다.
- 가격 조회 실패 시 응답 실패 또는 마지막 캐시 사용 정책이 명확하다.

### 8단계: 운영 안정성 보강

- KIS WebSocket reconnect backoff
- 구독 종목 목록 동적 관리
- market open/close 상태 처리
- rate limit 대응
- logging, metric, alert 기준 정리
- 장애 시 마지막 캐시 사용 정책 정리

## 테스트 계획

### 단위 테스트

- KIS message parser fixture 테스트
- `StockRealtimePriceService` 가격 갱신 테스트
- `OrderService` 주문 직전 가격 확정 테스트
- `PortfolioService` 최신 가격 기반 평가 테스트

### 통합 테스트

- fake realtime client로 stream endpoint 테스트
- 인증 없는 stream 요청 거절 테스트
- client disconnect resource 정리 테스트

### 수동 테스트

1. Backend 실행
2. iOS 로그인
3. 주식 리스트 진입
4. 실시간 가격 변화 확인
5. 매수 주문
6. 주문 체결가가 Backend 확정 가격인지 확인
7. 포트폴리오 평가금액 반영 확인

## 에러 처리 정책

| 상황 | 처리 |
|------|------|
| KIS 인증 실패 | stream 중단, 로그 기록, 재인증 시도 |
| WebSocket 연결 실패 | backoff 후 reconnect |
| 메시지 파싱 실패 | 해당 메시지만 폐기, 원문 일부 마스킹 로그 |
| 알 수 없는 종목 코드 | 무시 또는 warning 로그 |
| DB 가격 갱신 실패 | transaction rollback, stream 중단 여부는 별도 정책 |
| 주문 직전 가격 확정 실패 | 주문 실패 |

## 보안 정책

- KIS app key, app secret은 git에 커밋하지 않는다.
- 로그에 access token, approval key, secret을 남기지 않는다.
- iOS는 KIS API를 직접 호출하지 않는다.
- 모든 Backend stream endpoint는 JWT 인증을 유지한다.

## 우선순위

1. KIS 설정과 port 정의
2. KIS WebSocket adapter vertical slice
3. Backend 내부 price event broadcast
4. iOS stream endpoint
5. 주문 직전 가격 확정
6. 포트폴리오 최신 평가
7. 장애 대응과 운영 안정화

## 열어둔 결정

- iOS와 Backend 사이의 실시간 전송을 SSE로 시작할지 WebSocket으로 바로 갈지 결정 필요
- 실시간 캐시의 stale 기준 예: 1초, 3초, 5초 결정 필요
- 실시간 구독 종목 범위: 전체 상장 종목, seed 종목, 관심 종목, 보유 종목 중 선택 필요
- 장 종료 후 가격 표시 정책 결정 필요
- KIS 모의투자 환경에서 제공되는 실시간 데이터 범위 확인 필요
