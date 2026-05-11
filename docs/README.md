# Tumo Backend Docs

백엔드 구현과 운영 판단에 필요한 문서 정리

## 구조

```text
docs/
├── api/
│   └── phase1.md
├── db/
│   └── phase1-schema.md
├── setup/
│   └── application-yaml.md
└── decisions/
    └── README.md
```

## 문서 역할

| 경로 | 설명 |
|------|------|
| `api/` | iOS 클라이언트와 백엔드가 공유하는 API 계약 |
| `db/` | 테이블, 컬럼, 제약조건, 계산 정책 |
| `setup/` | 로컬 개발 환경과 설정 파일 설명 |
| `decisions/` | 주요 기술/정책 결정 기록 |

## Phase 1 문서

- [API 명세](api/phase1.md)
- [에러 응답 형식](api/error-response.md)
- [DB 스키마](db/phase1-schema.md)
- [application.yaml 설정](setup/application-yaml.md)
