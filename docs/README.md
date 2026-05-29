# Tumo Backend Docs

백엔드 구현과 운영 판단에 필요한 문서 정리

## 구조

```text
docs/
├── api/
│   ├── README.md
│   ├── auth/
│   │   ├── jwt-authentication.md
│   │   └── refresh-token-policy.md
│   └── common/
│       └── error-response.md
├── db/
│   └── README.md
├── integrations/
│   ├── README.md
│   └── kis-realtime-websocket-guide.md
├── test-guide/
│   ├── README.md
│   └── kis-websocket-test-guide.md
├── rules/
│   └── backend-coding-rules.md
├── setup/
│   └── application-yaml.md
└── decisions/
    ├── README.md
    └── realtime-stock-price-plan.md
```

## 문서 역할

| 경로 | 설명 |
|------|------|
| `api/` | iOS 클라이언트와 백엔드가 공유하는 API 계약 |
| `db/` | 테이블, 컬럼, 제약조건, 계산 정책 |
| `integrations/` | KIS 같은 외부 시스템 연동 문서 |
| `test-guide/` | 실제 환경에서 기능을 검증하는 수동 테스트 절차 |
| `rules/` | 백엔드 코드 작성, 에러 처리, 주석 작성 규칙 |
| `setup/` | 로컬 개발 환경과 설정 파일 설명 |
| `decisions/` | 주요 기술/정책 결정 기록 |

## 문서 목록

- [API 문서 인덱스](api/README.md)
- [에러 응답 형식](api/common/error-response.md)
- [JWT 인증](api/auth/jwt-authentication.md)
- [Refresh Token 정책](api/auth/refresh-token-policy.md)
- [DB 문서](db/README.md)
- [Backend Coding Rules](rules/backend-coding-rules.md)
- [application.yaml 설정](setup/application-yaml.md)
- [외부 연동 문서](integrations/README.md)
- [KIS 실시간 시세 WebSocket 연동 가이드](integrations/kis-realtime-websocket-guide.md)
- [테스트 가이드 문서](test-guide/README.md)
- [KIS WebSocket 테스트 가이드](test-guide/kis-websocket-test-guide.md)
- [실시간 주식 시세 연동 작업 계획](decisions/realtime-stock-price-plan.md)
