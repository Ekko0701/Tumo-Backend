# Future Improvements

Tumo Backend에서 나중에 수정, 리팩토링, 개선해야 할 항목을 정리한다.

각 항목은 지금 바로 구현하지 않더라도, 이후 작업 시 맥락을 잃지 않기 위해 남겨둔다.

## 실시간 시세

<details>
<summary>종목 단위 실시간 구독 상태 관리로 개선</summary>

현재 `StockPriceSubscriptionService`와 `StockOrderBookSubscriptionService`는 DB에 저장된 종목 목록을 한 번에 조회한 뒤, 여러 종목을 묶어서 KIS 구독 요청을 보낸다.

이 방식은 초기 구현으로는 단순하지만, 일부 종목만 구독 성공하고 중간에 실패하는 경우 상태 관리가 애매해질 수 있다.

예:

```text
005930 체결가 구독 성공
000660 체결가 구독 성공
035420 체결가 구독 실패
```

이 경우 전체 요청을 성공 또는 실패로만 처리하면 Backend의 구독 상태와 실제 KIS 구독 상태가 어긋날 수 있다.

추후에는 구독 요청과 상태 관리를 종목 단위로 분리한다.

```java
subscribePrice(String stockCode)
subscribeOrderBook(String stockCode)
unsubscribePrice(String stockCode)
unsubscribeOrderBook(String stockCode)
```

전체 종목 구독이 필요하면 service에서 단일 종목 구독을 반복 호출한다.

```java
for (String stockCode : stockCodes) {
    subscribePrice(stockCode);
}
```

주의할 점은 종목 단위로 구독 상태를 관리하더라도, WebSocket 연결을 종목마다 하나씩 만들 필요는 없다는 것이다.

권장 방향:

```text
KIS WebSocket 연결은 하나 또는 제한된 개수로 유지
구독 상태는 종목 + 데이터 타입 단위로 관리
```

예:

```text
KIS WebSocket 연결 1개
├── 005930 체결가 구독 상태
├── 000660 체결가 구독 상태
├── 005930 호가 구독 상태
└── 000660 호가 구독 상태
```

기대 효과:

- 종목별 성공/실패를 따로 기록할 수 있다.
- 실패한 종목만 재시도할 수 있다.
- 즐겨찾기, 보유 종목, 상세 화면 진입/이탈 기반 구독으로 확장하기 쉽다.
- 실제 KIS 구독 상태와 Backend 내부 상태가 어긋나는 문제를 줄일 수 있다.

</details>

<details>
<summary>KIS 구독 상태를 요청 상태와 확정 상태로 분리</summary>

현재는 Backend가 KIS 구독 메시지를 전송하면 내부 registry에 구독 중인 종목으로 기록한다.

추후에는 KIS WebSocket 제어 메시지를 해석해 실제 구독 성공 응답을 받은 뒤 `SUBSCRIBED` 상태로 확정하는 구조가 더 정확하다.

예상 상태:

```text
NONE
SUBSCRIBING
SUBSCRIBED
FAILED
UNSUBSCRIBING
```

이 구조를 도입하면 다음 상황을 더 정확히 처리할 수 있다.

- 구독 요청 전송은 성공했지만 KIS가 구독을 거부한 경우
- 일부 종목만 성공하고 일부 종목은 실패한 경우
- 네트워크 오류로 WebSocket이 닫힌 뒤 재구독이 필요한 경우
- 구독 해제 요청이 실패한 경우

</details>

<details>
<summary>실시간 구독 해제 API 추가</summary>

현재 내부 API에는 구독 시작과 구독 상태 조회만 있고, 명시적인 구독 해제 API는 없다.

현재 API:

```text
GET  /api/v1/internal/stocks/realtime/subscriptions
POST /api/v1/internal/stocks/realtime/subscribe
POST /api/v1/internal/stocks/realtime/order-books/subscribe
```

추후에는 다음과 같은 해제 API를 검토한다.

```text
DELETE /api/v1/internal/stocks/realtime/subscriptions/prices
DELETE /api/v1/internal/stocks/realtime/subscriptions/order-books
```

또는 단일 종목 단위로 관리할 경우:

```text
DELETE /api/v1/internal/stocks/{stockCode}/realtime/price/subscription
DELETE /api/v1/internal/stocks/{stockCode}/realtime/order-book/subscription
```

구독 해제 흐름:

```text
Controller
→ SubscriptionService
→ StockRealtimePriceClient 또는 StockRealtimeOrderBookClient
→ KIS WebSocket tr_type=2 메시지 전송
→ registry 상태 제거 또는 UNSUBSCRIBED 처리
```

</details>

<details>
<summary>SSE 인증 및 재연결 정책 문서화</summary>

SSE stream은 최초 연결 요청에서 JWT 인증을 수행하고, 연결이 열린 뒤에는 서버가 heartbeat와 실시간 이벤트를 비동기로 전송한다.

추후 iOS 클라이언트와 정책을 맞추기 위해 다음 내용을 명확히 문서화한다.

- SSE 최초 연결 시 access token 필요 여부
- 연결 유지 중 access token이 만료되었을 때 즉시 연결을 끊을지 여부
- 연결이 끊긴 뒤 재연결할 때 새 access token을 요구할지 여부
- heartbeat 주기와 timeout 기준
- iOS 앱이 백그라운드로 이동할 때 SSE 연결을 유지할지 해제할지 여부

현재 권장 방향:

```text
SSE 최초 연결 시 JWT 인증
연결 유지 중 token 만료는 즉시 끊지 않음
연결이 끊겨 재연결할 때는 새 token으로 다시 인증
```

</details>
