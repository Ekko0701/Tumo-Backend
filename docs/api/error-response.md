# Error Response

Tumo Backend API의 공통 에러 응답 형식 정의

## 기본 형식

모든 API 에러 응답의 JSON 구조

```json
{
  "code": "ERROR_CODE",
  "message": "에러 메시지",
  "fieldErrors": []
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| code | String | 클라이언트가 분기 처리에 사용할 에러 코드 |
| message | String | 사용자 또는 클라이언트에 전달할 기본 에러 메시지 |
| fieldErrors | Array | 요청 필드별 검증 실패 목록. 필드 오류가 없으면 빈 배열 |

## 일반 에러

비즈니스 규칙 위반, 서버 내부 오류처럼 특정 요청 필드와 직접 연결되지 않는 에러

예시:

```json
{
  "code": "DUPLICATED_EMAIL",
  "message": "이미 사용 중인 이메일입니다.",
  "fieldErrors": []
}
```

사용 상황:

```text
이미 가입된 이메일로 회원가입을 시도한 경우
잔고가 부족한 경우
존재하지 않는 종목을 요청한 경우
예상하지 못한 서버 오류가 발생한 경우
```

서버 코드 생성 방식:

```java
ErrorResponse.from(ErrorCode.DUPLICATED_EMAIL)
```

## 필드 검증 에러

`@Valid` 검증 실패 시 `INVALID_REQUEST` 사용 및 실패한 필드별 메시지를 `fieldErrors`에 포함

예시 요청:

```json
{
  "email": "wrong-email",
  "password": "123",
  "nickname": ""
}
```

예시 응답:

```json
{
  "code": "INVALID_REQUEST",
  "message": "요청값이 올바르지 않습니다.",
  "fieldErrors": [
    {
      "field": "email",
      "message": "이메일 형식이 올바르지 않습니다."
    },
    {
      "field": "password",
      "message": "비밀번호는 8자 이상 64자 이하여야 합니다."
    },
    {
      "field": "nickname",
      "message": "닉네임은 필수입니다."
    }
  ]
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| field | String | 검증에 실패한 요청 필드명 |
| message | String | 해당 필드에 표시할 검증 실패 메시지 |

서버 코드 생성 방식:

```java
ErrorResponse.of(ErrorCode.INVALID_REQUEST, fieldErrors)
```

## 현재 에러 코드

| HTTP Status | code | message | 설명 |
|-------------|------|---------|------|
| 400 | INVALID_REQUEST | 요청값이 올바르지 않습니다. | 요청 DTO 검증 실패 |
| 409 | DUPLICATED_EMAIL | 이미 사용 중인 이메일입니다. | 회원가입 이메일 중복 |
| 500 | INTERNAL_SERVER_ERROR | 서버 내부 오류가 발생했습니다. | 예상하지 못한 서버 오류 |

## 클라이언트 처리 기준

클라이언트의 1차 분기 기준: `code`

```text
INVALID_REQUEST
→ fieldErrors를 확인해 입력 필드별 안내 문구 표시

DUPLICATED_EMAIL
→ 이메일 중복 안내 표시

INTERNAL_SERVER_ERROR
→ 일반 서버 오류 안내 표시
```

회원가입 화면의 `fieldErrors` 매칭 기준: 요청 필드명

```text
email
password
nickname
```

예시 응답:

```json
{
  "field": "password",
  "message": "비밀번호는 8자 이상 64자 이하여야 합니다."
}
```

iOS 클라이언트 처리: 비밀번호 입력칸 아래에 해당 `message` 표시

## 서버 처리 흐름

### 비즈니스 예외

```text
Service에서 BusinessException 발생
→ GlobalExceptionHandler가 BusinessException 처리
→ ErrorCode를 ErrorResponse로 변환
→ code/message/fieldErrors 응답
```

예:

```java
throw new BusinessException(ErrorCode.DUPLICATED_EMAIL);
```

### 요청값 검증 예외

```text
Controller 파라미터의 @Valid 검증 실패
→ Spring MVC가 MethodArgumentNotValidException 발생
→ GlobalExceptionHandler가 필드별 오류 추출
→ INVALID_REQUEST + fieldErrors 응답
```

### 예상하지 못한 서버 예외

```text
처리되지 않은 Exception 발생
→ GlobalExceptionHandler가 Exception 처리
→ 서버 로그에 stack trace 기록
→ INTERNAL_SERVER_ERROR 응답
```
