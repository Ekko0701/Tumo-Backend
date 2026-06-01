# 0601 Stock List 구현 계획

이 문서는 iOS 주식 리스트 화면을 위한 Backend API와 구현 계획을 정리한다.

초기 구현은 page 기반 pagination으로 통일한다. 기본 종목 목록은 종목명 오름차순으로 정렬하고, 같은 종목명이 있을 경우 종목 코드 오름차순으로 보조 정렬한다.

## 목표

- KOSPI/KOSDAQ 전체 종목 리스트를 page 방식 무한 스크롤로 제공한다.
- 리스트 진입 직후 현재가는 KIS REST API로 조회한다.
- 이후 실시간 가격 변화는 기존 SSE 실시간 체결가 stream으로 반영한다.
- 기본 목록과 랭킹 목록 API를 분리한다.

## API 계획

### 기본 종목 목록

```http
GET /api/v1/stocks?market=KOSPI&page=0&size=30
```

Query parameter:

| 이름 | 설명 |
|------|------|
| `market` | 시장 구분. `KOSPI`, `KOSDAQ` |
| `page` | 0부터 시작하는 page 번호 |
| `size` | 한 번에 조회할 종목 수. 기본값은 30 |

정렬:

```text
stockName ASC
stockCode ASC
```

`stockName ASC`가 기본 정렬이고, 같은 종목명이 있을 경우 `stockCode ASC`를 보조 정렬로 사용한다.

### 랭킹 종목 목록

```http
GET /api/v1/stocks/rankings?market=KOSPI&type=TRADE_AMOUNT&page=0&size=30
```

Query parameter:

| 이름 | 설명 |
|------|------|
| `market` | 시장 구분. `KOSPI`, `KOSDAQ` |
| `type` | 랭킹 기준 |
| `page` | 0부터 시작하는 page 번호 |
| `size` | 한 번에 조회할 종목 수. 기본값은 30 |

지원할 랭킹 타입:

| 타입 | 설명 |
|------|------|
| `TRADE_AMOUNT` | 거래대금 상위 |
| `TRADE_VOLUME` | 거래량 상위 |
| `RISING` | 급상승 |
| `FALLING` | 급하락 |
| `POPULAR` | 인기 |

`POPULAR`은 KIS 데이터가 아니라 Tumo 내부 사용자 행동 데이터 기반이므로 초기에는 미지원 또는 별도 후속 작업으로 둔다.

### 응답 형태

```json
{
  "stocks": [
    {
      "stockCode": "005930",
      "stockName": "삼성전자",
      "market": "KOSPI",
      "currentPrice": 80000,
      "changePrice": 500,
      "changeRate": 0.63,
      "tradeVolume": 12000000,
      "tradeAmount": 960000000000,
      "priceChangedAt": "2026-06-01T10:00:00"
    }
  ],
  "page": 0,
  "size": 30,
  "hasNext": true
}
```

응답 필드:

| 이름 | 설명 |
|------|------|
| `stocks` | 종목 목록 |
| `page` | 현재 page 번호 |
| `size` | 요청 size |
| `hasNext` | 다음 page 존재 여부 |

종목 필드:

| 이름 | 설명 |
|------|------|
| `stockCode` | 종목 코드 |
| `stockName` | 종목명 |
| `market` | 시장 구분 |
| `currentPrice` | 현재가 |
| `changePrice` | 전일 대비 가격 변화량 |
| `changeRate` | 전일 대비 가격 변화율 |
| `tradeVolume` | 거래량 |
| `tradeAmount` | 거래대금 |
| `priceChangedAt` | 가격 기준 시각 |

## 구현 계획

### 기본 목록

`StockController`에서 `market`, `page`, `size` query parameter를 받는다.

`StockService`는 다음 정렬 기준으로 `PageRequest`를 생성한다.

```java
PageRequest.of(
        page,
        size,
        Sort.by("stockName").ascending()
                .and(Sort.by("stockCode").ascending())
)
```

`StockRepository.findByMarket(market, pageable)`로 page 단위 조회를 수행한다.

조회된 page의 종목에 대해서만 `StockPriceQueryPort.findCurrentPrice(...)`를 호출해 현재가를 보정한다. KIS 현재가 조회에 성공하면 `Stock.updatePrice(...)`로 DB의 마지막 현재가를 갱신하고, 실패하면 기존 DB 가격을 유지한다.

### 랭킹 목록

`StockRankingType` enum을 추가한다.

```java
public enum StockRankingType {
    TRADE_AMOUNT,
    TRADE_VOLUME,
    RISING,
    FALLING,
    POPULAR
}
```

`/api/v1/stocks/rankings` endpoint를 추가한다.

거래대금, 거래량, 급상승, 급하락은 KIS 랭킹/순위 REST API 조사 후 구현한다.

`POPULAR`은 Tumo 내부 사용자 행동 데이터가 필요하므로 초기에는 미지원 또는 별도 후속 작업으로 둔다. 후보 지표는 조회수, 검색 수, 관심 종목 등록 수, 주문 수, 보유 사용자 수다.

### 종목 마스터

KOSPI/KOSDAQ 전체 종목을 보여주려면 DB에 전체 종목 마스터 데이터가 필요하다.

현재 seed 일부 종목만으로는 전체 KOSPI 리스트를 만들 수 없으므로, 종목 마스터 수집 또는 import 작업을 별도 선행 또는 후속 작업으로 진행한다.

종목 마스터에는 최소한 다음 값이 필요하다.

```text
stockCode
stockName
market
```

## iOS 사용 흐름

기본 KOSPI 리스트:

```text
1. GET /api/v1/stocks?market=KOSPI&page=0&size=30
2. 응답으로 첫 page 표시
3. 현재 화면에 보이는 stockCode 목록으로 SSE stream 연결
4. 스크롤 하단 도달 시 page=1 요청
5. 다음 page append
6. 화면에 보이는 종목 기준으로 SSE 구독 목록 갱신 또는 재연결
```

실시간 체결가 stream:

```http
GET /api/v1/stocks/realtime/prices/stream?stockCodes=005930&stockCodes=000660
Accept: text/event-stream
```

SSE 이벤트가 오면 iOS는 같은 `stockCode`를 가진 row만 갱신한다.

## 테스트 계획

### 기본 목록 API

- `market=KOSPI&page=0&size=30` 요청 시 page 응답을 반환한다.
- 기본 목록은 `stockName ASC`, 동일 이름은 `stockCode ASC`로 정렬된다.
- 현재가 조회 성공 시 응답 가격이 갱신된다.
- 현재가 조회 실패 시 기존 DB 가격으로 응답한다.
- `hasNext`가 page 결과에 맞게 계산된다.

### 랭킹 API

- 지원하는 `type` 요청을 정상 처리한다.
- 미지원 `POPULAR` 정책을 명확히 검증한다.
- 잘못된 `market`, `type`, `page`, `size` 요청을 검증한다.

## 결정 사항

- 초기 pagination은 cursor가 아니라 page 방식으로 통일한다.
- iOS 무한 스크롤은 `page`, `size`, `hasNext` 기준으로 구현한다.
- 기본 종목 목록 정렬은 종목명 오름차순이며, 같은 종목명은 종목 코드 오름차순으로 보조 정렬한다.
- 실시간 가격 반영은 기존 `GET /api/v1/stocks/realtime/prices/stream` SSE API를 계속 사용한다.
- `docs/Plan`은 요청한 대문자 경로를 그대로 따른다.
