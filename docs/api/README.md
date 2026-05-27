# API 문서

Tumo Backend API 문서는 기능별 디렉터리로 나눠 관리한다.

## 구조

```text
docs/api/
├── README.md
├── auth/
│   ├── jwt-authentication.md
│   └── refresh-token-policy.md
└── common/
    └── error-response.md
```

## 문서 목록

| 구분 | 문서 | 설명 |
|------|------|------|
| 공통 | [에러 응답 형식](common/error-response.md) | API 공통 에러 응답 구조와 에러 코드 |
| 인증 | [JWT 인증](auth/jwt-authentication.md) | Access Token 발급, 전달, 검증 흐름 |
| 인증 | [Refresh Token 정책](auth/refresh-token-policy.md) | Refresh Token 저장, 재발급, 만료 정책 |

## 정리 기준

- 현재 API 계약 또는 정책으로 계속 참고할 문서만 유지한다.
- 특정 시점의 수동 테스트 절차는 오래되기 쉬우므로 API 문서에서 관리하지 않는다.
- 기능별 최신 API 계약이 필요하면 별도 문서로 추가한다.
- 주요 설계 판단은 `docs/decisions/`에 기록한다.
