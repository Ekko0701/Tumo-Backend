# KIS WebSocket 테스트 가이드

이 문서는 Tumo Backend가 실제 KIS WebSocket에 연결해 실시간 체결가와 실시간 호가를 수신하고, SSE stream으로 전달하는지 수동 검증하는 절차를 정리한다.

## 검증 목표

다음 흐름이 실제 환경에서 동작하는지 확인한다.

```text
KIS WebSocket
→ Backend KIS adapter
→ parser
→ service
→ SSE publisher
→ curl 또는 iOS SSE client
```

확인할 이벤트:

```text
event: stock-price
event: stock-order-book
event: heartbeat
```

## 사전 준비

### KIS 계정 정보

KIS Developers에서 발급받은 App Key와 App Secret이 필요하다.

주의:

- App Key를 문서, 커밋, 로그에 남기지 않는다.
- App Secret을 문서, 커밋, 로그에 남기지 않는다.
- WebSocket approval key도 로그에 남기지 않는다.

### 환경변수 설정

로컬에서는 `.env` 파일에 KIS 인증 정보를 저장한다.

```env
KIS_APP_KEY=
KIS_APP_SECRET=
```

`.env`에는 실제 값을 넣되, 절대 커밋하지 않는다. 필요한 키 목록은 `.env.example`에만 빈 값으로 기록한다.

터미널에서 Backend를 실행하기 전에 `.env`를 환경변수로 로드한다.

```bash
set -a
source .env
set +a
```

필요하면 KIS URL도 `.env`에서 override한다.

```bash
KIS_REST_URL=https://openapi.koreainvestment.com:9443
KIS_WEBSOCKET_URL=ws://ops.koreainvestment.com:21000
KIS_WEBSOCKET_PATH=/tryitout
```

기본 TR ID:

```env
KIS_TRADE_PRICE_TR_ID=H0STCNT0
KIS_ORDER_BOOK_TR_ID=H0STASP0
```

### Backend 실행

KIS adapter를 활성화해서 Backend를 실행한다.

```bash
./gradlew bootRun --args='--kis.enabled=true'
```

정상 기준:

```text
Tomcat started on port 8080
Started TumoApplication
```

## 1. SSE 가격 stream 연결

다른 터미널에서 가격 stream을 먼저 연결한다.

```bash
curl -N \
  -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/stocks/realtime/prices/stream"
```

관심 종목만 확인하려면 `stockCodes`를 지정한다.

```bash
curl -N \
  -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/stocks/realtime/prices/stream?stockCodes=005930&stockCodes=000660"
```

초기 정상 기준:

```text
event: heartbeat
data: ok
```

`heartbeat`는 30초마다 전송된다.

## 2. SSE 호가 stream 연결

다른 터미널에서 특정 종목의 호가 stream을 연결한다. 이 요청은 단순히 SSE emitter만 등록하는 것이 아니라, Backend가 KIS에 해당 종목의 실시간 호가 구독을 시작하는 트리거이기도 하다.

```bash
curl -N \
  -H "Accept: text/event-stream" \
  "http://localhost:8080/api/v1/stocks/005930/realtime/order-book/stream"
```

초기 정상 기준:

```text
event: heartbeat
data: ok
```

장중에 KIS 구독과 메시지 파싱이 정상 동작하면 같은 터미널에 다음 이벤트가 이어서 들어온다.

```text
event: stock-order-book
data: {...}
```

## 3. KIS 실시간 체결가 구독 시작

Backend가 KIS에서 실시간 체결가를 받기 시작하도록 내부 API를 호출한다.

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/internal/stocks/realtime/subscribe"
```

정상 기준:

```http
HTTP/1.1 204
```

가격 stream 터미널에서 기대하는 이벤트:

```text
event: stock-price
data: {...}
```

## 4. KIS 실시간 호가 수동 구독 확인

호가는 `GET /api/v1/stocks/{stockCode}/realtime/order-book/stream` 연결 시 자동으로 KIS 구독을 시작한다. 아래 내부 API는 stream 없이 Backend의 KIS 호가 구독만 별도로 확인해야 할 때 사용하는 수동 테스트용이다.

```bash
curl -i -X POST \
  "http://localhost:8080/api/v1/internal/stocks/005930/realtime/order-book/subscribe"
```

정상 기준:

```http
HTTP/1.1 204
```

이미 호가 stream이 연결되어 있다면 기대하는 이벤트:

```text
event: stock-order-book
data: {...}
```

## 5. 구독 상태 확인

Backend가 어떤 종목을 KIS에 구독 중인지 확인한다.

```bash
curl -s \
  "http://localhost:8080/api/v1/internal/stocks/realtime/subscriptions"
```

응답 예시:

```json
{
  "priceStockCodes": ["005930", "000660"],
  "orderBookStockCodes": ["005930"]
}
```

## 정상 동작 체크리스트

- Backend가 `kis.enabled=true`로 실행된다.
- KIS approval key 발급이 실패하지 않는다.
- KIS WebSocket 연결이 실패하지 않는다.
- 체결가 구독 API가 `204 No Content`를 반환한다.
- 호가 SSE stream 연결 시 Backend가 해당 종목의 KIS 호가 구독을 시작한다.
- 수동 호가 구독 API를 별도로 호출했다면 `204 No Content`를 반환한다.
- 가격 SSE stream에서 `heartbeat` 이벤트가 보인다.
- 호가 SSE stream에서 `heartbeat` 이벤트가 보인다.
- 장중에는 가격 SSE stream에서 `stock-price` 이벤트가 보인다.
- 장중에는 호가 SSE stream에서 `stock-order-book` 이벤트가 보인다.
- 구독 상태 조회 API에 종목 코드가 기록된다.

## 실패 시 점검 항목

### Backend 실행 실패

확인할 것:

```text
PostgreSQL 실행 여부
application.yaml DB 접속 정보
KIS_APP_KEY 설정 여부
KIS_APP_SECRET 설정 여부
```

### KIS approval key 발급 실패

확인할 것:

```text
KIS_APP_KEY 값이 비어 있지 않은지
KIS_APP_SECRET 값이 비어 있지 않은지
KIS_REST_URL이 현재 계정 환경과 맞는지
KIS 계정이 Open API 사용 가능한 상태인지
```

### WebSocket 연결 실패

확인할 것:

```text
KIS_WEBSOCKET_URL
KIS_WEBSOCKET_PATH
실전/모의 환경 URL 구분
네트워크 연결
장 운영 시간
```

### SSE에는 heartbeat만 보이고 실시간 이벤트가 없는 경우

확인할 것:

```text
KIS 구독 시작 API를 호출했는지
DB에 구독할 Stock 데이터가 존재하는지
장 운영 시간이 맞는지
stockCodes 필터에 이벤트 종목이 포함되어 있는지
parser가 raw message를 정상 변환하는지
```

### parser 실패가 의심되는 경우

확인할 것:

```text
KIS raw message 필드 순서
H0STCNT0 parser fixture와 실제 메시지 차이
H0STASP0 parser fixture와 실제 메시지 차이
숫자 필드의 빈 문자열 또는 부호 처리
시간 필드 포맷
```

## 테스트 종료

curl stream은 `Ctrl + C`로 종료한다.

iOS에서는 화면 이탈 시 SSE 연결을 명시적으로 닫는다.

```text
화면 진입
→ SSE 연결 시작

화면 이탈
→ SSE 연결 cancel
→ Backend registry에서 emitter 제거
```

## 주의사항

- smoke test 중에도 secret 값을 로그에 남기지 않는다.
- 실제 장이 열려 있지 않으면 실시간 체결/호가 이벤트가 제한될 수 있다.
- KIS API의 실전/모의 URL과 WebSocket URL은 계정 환경에 맞게 확인한다.
- raw message 전체를 장기간 로그로 남기지 않는다.
