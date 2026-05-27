# Refresh Token 정책

Tumo Backend의 Refresh Token 발급, 저장, 재발급, 만료 정책 정리.

## 목적

Access Token 만료 이후 사용자의 재로그인 부담을 줄이기 위한 토큰 재발급 수단.

```text
Access Token
→ 짧은 만료 시간
→ API 인증에 사용

Refresh Token
→ 긴 만료 시간
→ Access Token 재발급에 사용
```

## 기본 흐름

로그인 성공 시 토큰 발급 흐름:

```text
1. 이메일/비밀번호 검증
2. Access Token 발급
3. Refresh Token 발급
4. Refresh Token 저장
5. 클라이언트에 Access Token + Refresh Token 응답
```

Access Token 만료 후 재발급 흐름:

```text
1. 클라이언트가 Refresh Token으로 재발급 요청
2. 서버가 Refresh Token 검증
3. 저장된 Refresh Token과 요청 토큰 비교
4. 유효하면 새 Access Token 발급
5. Refresh Token rotation 적용 시 새 Refresh Token도 함께 발급
```

로그아웃 흐름:

```text
1. 클라이언트가 Access Token으로 로그아웃 요청
2. 서버가 Access Token 검증
3. Access Token의 userId로 사용자 식별
4. 사용자에게 저장된 Refresh Token 삭제
5. 이후 기존 Refresh Token으로 재발급 요청 시 실패
```

## 만료 시간

개발용 초기 설정:

```yaml
jwt:
  access-token-expiration-millis: 3600000
  refresh-token-expiration-millis: 1209600000
```

| 설정 | 값 | 의미 |
|------|----|------|
| `access-token-expiration-millis` | `3600000` | 1시간 |
| `refresh-token-expiration-millis` | `1209600000` | 14일 |

운영 환경 기준:

```text
Access Token
→ 15분~1시간 범위 검토

Refresh Token
→ 7일~30일 범위 검토
```

## 저장 정책

Refresh Token은 서버 저장소에 저장.

초기 구현 기준:

```text
저장소
→ PostgreSQL

저장 대상
→ userId
→ refreshToken
→ expiresAt
→ createdAt
```

저장 이유:

```text
로그아웃 시 Refresh Token 폐기 가능
탈취 의심 시 서버에서 무효화 가능
재발급 요청 시 서버 저장값과 비교 가능
```

## Rotation 정책

Refresh Token 재발급 요청 성공 시 기존 Refresh Token 폐기 후 새 Refresh Token 발급.

```text
재발급 성공
→ 새 Access Token 발급
→ 새 Refresh Token 발급
→ 기존 Refresh Token 삭제 또는 교체
```

장점:

```text
탈취된 Refresh Token의 재사용 가능 시간 감소
토큰 재사용 탐지 정책으로 확장 가능
```

초기 구현에서는 사용자당 유효한 Refresh Token 1개 유지.

```text
같은 사용자가 다시 로그인
→ 기존 Refresh Token 교체
```

## 클라이언트 저장 정책

iOS 저장 위치:

```text
Access Token
→ 메모리 또는 Keychain

Refresh Token
→ Keychain
```

주의 사항:

```text
Refresh Token은 Access Token보다 오래 유효
UserDefaults 저장 금지
로그아웃 시 Keychain에서 삭제
```

## API 계획

로그인 응답:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

Access Token 재발급 요청:

```http
POST /api/v1/auth/token/refresh
Content-Type: application/json
```

```json
{
  "refreshToken": "..."
}
```

Access Token 재발급 응답:

```json
{
  "accessToken": "...",
  "refreshToken": "...",
  "tokenType": "Bearer"
}
```

## 실패 응답

Refresh Token이 없거나 유효하지 않은 경우:

```json
{
  "code": "INVALID_TOKEN",
  "message": "인증 토큰이 유효하지 않습니다.",
  "fieldErrors": []
}
```

세부 에러 코드는 구현 단계에서 필요 시 분리.

```text
INVALID_REFRESH_TOKEN
EXPIRED_REFRESH_TOKEN
REFRESH_TOKEN_NOT_FOUND
```

초기 구현은 `INVALID_TOKEN` 재사용.

## 구현 예정

```text
Access Token 즉시 무효화 정책
```

## 구현 완료

```text
Refresh Token 정책 문서화
application.yaml에 refresh-token-expiration-millis 설정 추가
JwtProperties에 refresh-token-expiration-millis 바인딩 추가
JwtTokenProvider에 createRefreshToken 추가
RefreshToken Entity/Repository 추가
로그인 시 Refresh Token 저장 및 응답
토큰 재발급 API 추가
로그아웃 시 Refresh Token 폐기
```
