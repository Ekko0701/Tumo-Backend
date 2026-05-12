# JWT 인증

Tumo Backend의 JWT Access Token 발급, 전달, 검증 흐름 정리.

## JWT 기본 구조

JWT는 다음 3개 영역으로 구성.

```text
header.payload.signature
```

| 영역 | 역할 |
|------|------|
| `header` | 토큰 타입, 서명 알고리즘 정보 |
| `payload` | 토큰에 담긴 실제 데이터 |
| `signature` | `header`와 `payload`의 변조 여부 검증용 서명 |

## Payload

`payload`는 JWT 안에 담긴 실제 데이터 영역.

예시:

```json
{
  "sub": "1",
  "iat": 1710000000,
  "exp": 1710003600
}
```

| 값 | 의미 |
|----|------|
| `sub` | 토큰의 주체, 현재는 `userId` |
| `iat` | 토큰 발급 시간 |
| `exp` | 토큰 만료 시간 |

`payload`는 암호화된 값이 아니라 Base64Url 인코딩된 값.

```text
JWT payload는 누구나 디코딩해서 확인 가능
```

따라서 JWT payload에 넣지 말아야 할 값:

```text
비밀번호
주민등록번호
카드번호
민감한 개인정보
```

## Signature

`signature`는 토큰이 변조되지 않았음을 검증하기 위한 값.

서버는 JWT 발급 시 다음 값으로 signature 생성.

```text
signature = sign(header + payload, secret)
```

`secret`은 서버만 알고 있어야 하는 비밀값.

Tumo Backend 개발용 설정:

```yaml
jwt:
  secret: tumo-local-development-secret-key-must-be-at-least-32-bytes
```

`secret`의 역할:

```text
토큰 발급 시 signature 생성
토큰 검증 시 signature 재계산
```

## JWT 발급 흐름

로그인 성공 시 서버에서 JWT Access Token 발급.

```text
1. 사용자 로그인 요청
2. 서버의 이메일/비밀번호 검증
3. 검증 성공 시 userId 기준 JWT 생성
4. JWT에 발급 시간과 만료 시간 포함
5. 서버 secret으로 signature 생성
6. 클라이언트에 Access Token 응답
```

단순화된 발급 구조:

```text
header = H
payload = P1
signature = sign(H + P1, secret)

JWT = H.P1.signature
```

클라이언트는 이 JWT 저장.

```text
iOS 앱 기준 저장 위치 후보: Keychain
```

## JWT 요청 흐름

클라이언트는 인증이 필요한 API 호출 시 JWT를 헤더에 포함.

```http
GET /api/v1/users/me
Authorization: Bearer H.P1.signature
```

서버의 요청 처리 흐름:

```text
1. Authorization 헤더 확인
2. Bearer 뒤의 JWT 추출
3. JWT를 header, payload, signature로 분리
4. header + payload + 서버 secret으로 signature 재계산
5. 재계산한 signature와 토큰에 포함된 signature 비교
6. 만료 시간 exp 확인
7. 검증 성공 시 payload의 sub에서 userId 추출
```

## JWT 검증 흐름

서버가 받은 토큰:

```text
H.P1.signature
```

서버가 토큰을 분리한 결과:

```text
header = H
payload = P1
token.signature = 클라이언트가 보낸 signature
```

서버가 가진 secret으로 검증용 signature 재계산.

```text
expectedSignature = sign(H + P1, secret)
```

비교:

```text
expectedSignature == token.signature
```

검증 성공:

```text
서버가 발급한 뒤 header와 payload가 변조되지 않은 토큰
```

검증 실패:

```text
payload가 변조된 토큰
서버 secret으로 발급되지 않은 토큰
만료된 토큰
형식이 올바르지 않은 토큰
```

## Payload 변조 예시

원래 payload:

```json
{
  "sub": "1",
  "iat": 1710000000,
  "exp": 1710003600
}
```

공격자가 변경한 payload:

```json
{
  "sub": "999",
  "iat": 99999999,
  "exp": 99999999
}
```

정상 토큰:

```text
H.P1.S1
```

payload만 변경된 토큰:

```text
H.P2.S1
```

서버의 검증용 signature 재계산:

```text
S2 = sign(H + P2, secret)
```

비교:

```text
S2 == S1
```

payload가 바뀌었기 때문에 signature 불일치.

```text
검증 실패
→ INVALID_TOKEN
```

공격자가 성공하려면 다음 형태의 토큰 필요.

```text
H.P2.sign(H + P2, secret)
```

하지만 `secret`은 서버만 알고 있으므로 올바른 signature 생성 불가.

## Access Token 저장 여부

일반적인 JWT Access Token 저장 방식:

```text
Access Token
→ 클라이언트 저장
→ 서버 DB에 저장하지 않음
→ 요청마다 signature 검증
```

서버가 DB에 Access Token을 저장하지 않아도 되는 이유:

```text
서버는 secret 보관
클라이언트는 JWT 전체 전달
서버는 secret으로 signature 재계산 가능
```

Refresh Token은 DB 또는 Redis에 저장하는 경우가 많음.

```text
Access Token
→ 짧은 만료 시간
→ API 인증용

Refresh Token
→ 긴 만료 시간
→ Access Token 재발급용
→ DB 또는 Redis 저장 가능
```

## Tumo Backend 현재 구현 상태

패키지 배치:

```text
com.tumo.auth
├── controller
├── dto
└── service

com.tumo.global.security
├── SecurityConfig
└── jwt
    ├── JwtProperties
    └── JwtTokenProvider
```

구현 완료:

```text
JWT Access Token 발급
JWT secret 설정
JwtProperties 설정 바인딩
JwtTokenProvider 생성
JWT 유효성 검증
JWT에서 userId 추출
INVALID_TOKEN 에러 코드 추가
```

구현 예정:

```text
Authorization 헤더에서 JWT 추출
JWT 인증 필터
SecurityContext 인증 정보 저장
인증 실패 공통 응답 정리
/api/v1/users/me 같은 보호 API
```

## 핵심 정리

```text
JWT는 암호화된 토큰이 아니라 서명된 토큰
```

```text
payload는 확인 가능
payload 변경 시 signature 검증 실패
```

```text
서버가 신뢰하는 값은 payload 내용 자체가 아니라 signature 검증을 통과한 payload
```

```text
서버는 signature를 DB에 저장하지 않고 secret으로 signature를 다시 계산해 비교
```

```text
Access Token은 보통 DB에 저장하지 않고, Refresh Token은 저장하는 경우가 많음
```
