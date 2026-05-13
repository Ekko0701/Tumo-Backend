# Backend application.yaml 설정 설명

이 문서는 `src/main/resources/application.yaml`의 초기 설정값을 설명한다.

## 전체 설정 예시

```yaml
spring:
  application:
    name: Tumo

  datasource:
    url: jdbc:postgresql://localhost:5432/tumo
    username: tumo
    password: tumo

  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        format_sql: true
    show-sql: true

server:
  port: 8080

jwt:
  secret: tumo-local-development-secret-key-must-be-at-least-32-bytes
  access-token-expiration-millis: 3600000
  refresh-token-expiration-millis: 1209600000
```

## spring

```yaml
spring:
```

Spring Boot 관련 설정의 최상위 그룹이다.

`application`, `datasource`, `jpa`처럼 Spring Boot가 사용하는 설정들이 이 아래에 위치한다.

## spring.application

```yaml
  application:
    name: Tumo
```

현재 Spring Boot 애플리케이션의 이름을 지정한다.

이 이름은 로그, 모니터링, Actuator, 클라우드 환경 등에서 애플리케이션을 식별하는 데 사용될 수 있다.

## spring.datasource

```yaml
  datasource:
    url: jdbc:postgresql://localhost:5432/tumo
    username: tumo
    password: tumo
```

데이터베이스 연결 정보를 지정한다.

### url

```yaml
    url: jdbc:postgresql://localhost:5432/tumo
```

PostgreSQL 접속 주소다.

구성은 다음과 같다.

```text
jdbc:postgresql://  PostgreSQL JDBC 드라이버 사용
localhost           내 컴퓨터에서 실행 중인 DB 서버
5432                PostgreSQL 기본 포트
tumo                접속할 데이터베이스 이름
```

즉, 로컬 PostgreSQL 서버의 `tumo` 데이터베이스에 접속한다는 의미다.

### username

```yaml
    username: tumo
```

데이터베이스 접속 계정 이름이다.

### password

```yaml
    password: tumo
```

데이터베이스 접속 비밀번호다.

개발 환경에서는 직접 적어도 되지만, 배포 환경에서는 환경변수나 별도 secret 관리 방식으로 분리하는 것이 좋다.

## spring.jpa

```yaml
  jpa:
```

JPA 관련 설정이다.

JPA는 Java 객체와 데이터베이스 테이블을 매핑해주는 기술이다. 예를 들어 `User` 엔티티를 만들면 데이터베이스의 사용자 테이블과 연결할 수 있다.

## spring.jpa.hibernate

```yaml
    hibernate:
      ddl-auto: update
```

Hibernate는 Spring Data JPA에서 주로 사용하는 JPA 구현체다.

### ddl-auto

```yaml
      ddl-auto: update
```

애플리케이션 실행 시 Entity 클래스를 기준으로 데이터베이스 스키마를 자동으로 갱신한다.

`update`는 다음 의미를 가진다.

```text
Entity에는 있지만 DB에는 없는 테이블이나 컬럼이 있으면 추가한다.
기존 데이터는 가능한 유지한다.
```

개발 초반에는 편리하지만, 운영 환경에서는 의도하지 않은 스키마 변경 위험이 있으므로 보통 `validate` 또는 Flyway/Liquibase 같은 마이그레이션 도구를 사용한다.

## spring.jpa.properties.hibernate

```yaml
    properties:
      hibernate:
        format_sql: true
```

Hibernate에 전달할 세부 옵션을 지정한다.

### format_sql

```yaml
        format_sql: true
```

Hibernate가 출력하는 SQL을 보기 좋게 줄바꿈하고 들여쓰기한다.

개발 중 SQL을 읽고 디버깅할 때 유용하다.

## spring.jpa.show-sql

```yaml
    show-sql: true
```

JPA/Hibernate가 실행하는 SQL을 콘솔에 출력한다.

개발 중에는 실제로 어떤 SQL이 실행되는지 확인하기 좋지만, 운영 환경에서는 로그가 많아지고 민감 정보가 노출될 수 있어 보통 끈다.

## server

```yaml
server:
  port: 8080
```

Spring Boot 내장 웹 서버 설정이다.

### port

```yaml
  port: 8080
```

애플리케이션이 사용할 포트를 지정한다.

이 설정에서는 서버 실행 후 다음 주소로 접근할 수 있다.

```text
http://localhost:8080
```

예를 들어 `/health` API를 만들면 다음 주소로 확인한다.

```text
http://localhost:8080/health
```

## jwt

```yaml
jwt:
  secret: tumo-local-development-secret-key-must-be-at-least-32-bytes
  access-token-expiration-millis: 3600000
  refresh-token-expiration-millis: 1209600000
```

JWT 발급과 검증에 사용하는 설정이다.

### secret

```yaml
  secret: tumo-local-development-secret-key-must-be-at-least-32-bytes
```

JWT signature 생성과 검증에 사용하는 개발용 비밀값이다.

운영 환경에서는 환경변수 또는 Secret Manager로 분리하는 것이 좋다.

### access-token-expiration-millis

```yaml
  access-token-expiration-millis: 3600000
```

Access Token 만료 시간이다.

```text
3600000ms = 1시간
```

### refresh-token-expiration-millis

```yaml
  refresh-token-expiration-millis: 1209600000
```

Refresh Token 만료 시간이다.

```text
1209600000ms = 14일
```

## 요약

이 설정은 다음 의미를 가진다.

```text
애플리케이션 이름은 Tumo다.
localhost:5432에서 실행 중인 PostgreSQL의 tumo DB에 접속한다.
DB 계정은 tumo / tumo를 사용한다.
개발 중에는 JPA Entity 기준으로 DB 스키마를 자동 갱신한다.
실행되는 SQL을 콘솔에 출력하고 보기 좋게 포맷한다.
서버는 8080 포트에서 실행한다.
JWT Access Token은 1시간 동안 유효하다.
JWT Refresh Token은 14일 동안 유효하다.
```
