# Backend Coding Rules

이 문서는 Tumo Backend 작업 시 반복적으로 지켜야 할 코드 작성 규칙을 정리한다.

## 기본 원칙

- Clean Architecture, SOLID, OOP를 준수한다.
- Domain은 Spring, HTTP, WebSocket, DB, 외부 API 구현체에 의존하지 않는다.
- 외부 시스템 연동은 port/adapter 구조로 격리한다.
- Application Service는 use case 흐름을 조율하고, 도메인 객체의 행위를 호출한다.
- Domain 객체는 자신의 상태 변경을 메서드로 직접 책임진다.

## 패키지 의존 규칙

- `domain`은 다른 계층의 구현체를 import하지 않는다.
- `service` 또는 `application`은 repository와 port interface에 의존할 수 있다.
- 외부 API, WebSocket, SSE, HTTP client 구현은 adapter 또는 infrastructure 성격의 패키지에 둔다.
- Controller는 요청/응답 변환과 인증된 사용자 식별자 전달에 집중한다.

## 주석 규칙

- class, record, enum, interface에는 역할을 설명하는 Javadoc을 작성한다.
- record component와 주요 property에는 Javadoc `@param` 또는 field comment로 의미를 설명한다.
- 주석은 한국어로 작성한다.
- 단순 번역보다 도메인 의미, 사용 맥락, 주의할 점을 우선 설명한다.
- 외부 API DTO와 도메인 모델이 헷갈릴 수 있는 경우, 해당 객체가 어느 계층의 모델인지 주석에 명시한다.
- 값의 단위나 기준이 애매한 property는 반드시 주석에 기준을 적는다.

예시:

```java
/**
 * 특정 시점의 종목 가격 상태를 표현하는 값 객체.
 *
 * @param stockCode 가격이 적용되는 종목 코드
 * @param currentPrice 현재가
 * @param changePrice 전일 대비 가격 변화량
 * @param changeRate 전일 대비 가격 변화율
 * @param tradeVolume 누적 거래량
 * @param priceChangedAt 가격 변경 시각
 */
public record StockPrice(
        String stockCode,
        Long currentPrice,
        Long changePrice,
        BigDecimal changeRate,
        Long tradeVolume,
        LocalDateTime priceChangedAt
) {
}
```

## 에러 처리 규칙

- 사용자/API 응답 메시지는 한국어로 작성한다.
- DTO validation message는 한국어로 작성한다.
- 비즈니스 유스케이스 실패는 `BusinessException`과 `ErrorCode`를 사용한다.
- 도메인 값 객체의 불변식 위반은 기본적으로 `IllegalArgumentException`을 사용한다.
- 도메인 객체는 API 응답 정책에 직접 의존하지 않는다.
- 외부 provider의 잘못된 응답은 adapter에서 파싱 실패 또는 변환 실패로 처리하고, 도메인 내부로 원본 오류를 흘리지 않는다.

예시:

```java
if (stockCode == null || stockCode.isBlank()) {
    throw new IllegalArgumentException("종목 코드는 필수입니다.");
}
```

## 테스트 규칙

- 도메인 값 객체는 생성 성공 케이스와 주요 불변식 실패 케이스를 테스트한다.
- Application Service는 repository와 port를 mock 또는 fake로 대체해 use case 흐름을 테스트한다.
- 외부 API 연동 전에는 fake 구현체로 내부 흐름을 먼저 검증한다.
- 외부 provider parser는 fixture 기반 테스트를 작성한다.

## 커밋 규칙

- 기존 커밋 스타일처럼 `feat:`, `fix:`, `docs:`, `test:`, `refactor:` prefix를 사용한다.
- 하나의 커밋은 하나의 의도를 갖도록 작게 유지한다.
- 커밋 메시지에 co-author를 포함하지 않는다.
