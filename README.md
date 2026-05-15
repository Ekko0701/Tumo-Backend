# Tumo Backend

Tumo의 Spring Boot 백엔드 애플리케이션.

Phase 1 목표는 회원가입/로그인부터 종목 조회, 매수 주문, 포트폴리오 조회까지의 핵심 흐름 구현.

```text
회원가입/로그인
→ 종목 조회
→ 매수 주문
→ 포트폴리오 조회
```

## 기술 스택

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build | Gradle Groovy |
| Database | PostgreSQL |
| ORM | Spring Data JPA |
| Auth | Spring Security, JWT |
| API Docs | springdoc-openapi, Swagger UI |
| Test | JUnit 5, Mockito, AssertJ |

## 주요 기능

### Auth

```text
회원가입
로그인
Access Token / Refresh Token 발급
Access Token 재발급
로그아웃
내 정보 조회
```

### Stock

```text
종목 목록 조회
종목 상세 조회
개발용 seed 종목 데이터
```

### Order

```text
현재가 기준 시장가 매수 주문
주문 내역 저장
현금 잔고 차감
보유 종목 생성 또는 평균 매입가 갱신
```

### Portfolio

```text
현금 잔고 조회
보유 종목 조회
평가 금액 계산
평가손익 계산
수익률 계산
```

## 프로젝트 구조

```text
src/main/java/com/tumo/
├── auth
├── global
├── holding
├── order
├── portfolio
├── stock
└── user
```

현재 구조는 Spring Boot 레이어드 아키텍처 기반.

```text
Controller
→ Service
→ Repository
→ Entity
```

도메인 객체에는 자기 상태를 변경하는 규칙을 메서드로 둔다.

```text
User.decreaseCashBalance()
Holding.buy()
RefreshToken.updateToken()
```

## 로컬 실행

### 1. PostgreSQL 실행 확인

```bash
brew services list | grep postgresql
```

기대 상태:

```text
postgresql@15 started
```

### 2. DB 접속 확인

```bash
psql -h localhost -U tumo -d tumo
```

종료:

```sql
\q
```

### 3. 설정 확인

[application.yaml 설정 문서](docs/setup/application-yaml.md) 참고.

로컬 개발 기본 설정:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/tumo
    username: tumo
    password: tumo

server:
  port: 8080
```

### 4. 애플리케이션 실행

IntelliJ에서 `TumoApplication` 실행.

또는:

```bash
./gradlew bootRun
```

기대 로그:

```text
Tomcat started on port 8080
Started TumoApplication
```

## 테스트

```bash
./gradlew test
```

## Swagger

애플리케이션 실행 후 접속:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Swagger 인증:

```text
1. 로그인 API로 accessToken 발급
2. Swagger UI의 Authorize 선택
3. accessToken 값 입력
```

## API 요약

| Method | Endpoint | 인증 | 설명 |
|--------|----------|------|------|
| POST | `/api/v1/auth/signup` | N | 회원가입 |
| POST | `/api/v1/auth/login` | N | 로그인 |
| POST | `/api/v1/auth/token/refresh` | N | Access Token 재발급 |
| POST | `/api/v1/auth/logout` | Y | 로그아웃 |
| GET | `/api/v1/users/me` | Y | 내 정보 조회 |
| GET | `/api/v1/stocks` | Y | 종목 목록 조회 |
| GET | `/api/v1/stocks/{stockCode}` | Y | 종목 상세 조회 |
| POST | `/api/v1/orders` | Y | 매수 주문 |
| GET | `/api/v1/portfolio` | Y | 내 포트폴리오 조회 |

## 문서

| 문서 | 설명 |
|------|------|
| [Backend Docs](docs/README.md) | 백엔드 문서 인덱스 |
| [Phase 1 API 명세](docs/api/phase1.md) | iOS와 공유하는 API 계약 |
| [Phase 1 API 테스트 가이드](docs/api/phase1-test-guide.md) | Phase 1 전체 흐름 수동 테스트 절차 |
| [Auth API 테스트 가이드](docs/api/auth-test-guide.md) | 인증 API 수동 테스트 절차 |
| [에러 응답 형식](docs/api/error-response.md) | 공통 에러 응답 구조 |
| [JWT 인증](docs/api/jwt-authentication.md) | JWT 발급/검증 흐름 |
| [Refresh Token 정책](docs/api/refresh-token-policy.md) | Refresh Token 저장/재발급/로그아웃 정책 |
| [DB 스키마](docs/db/phase1-schema.md) | Phase 1 DB 구조 |
| [application.yaml 설정](docs/setup/application-yaml.md) | 로컬 설정 설명 |

## 개발 메모

현재 `application.yaml`의 DB 계정과 JWT secret은 로컬 개발용 값.

```text
배포 전 환경변수 분리 필요
운영용 secret 교체 필요
ddl-auto 운영 설정 재검토 필요
```
