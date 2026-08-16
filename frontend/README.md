# CareerLog Frontend

순수 HTML, CSS, JavaScript로 만든 취준노트 프런트엔드입니다. Spring Boot + PostgreSQL 백엔드와 연결하도록 API 모듈을 분리했습니다.

## 실행

정적 파일이므로 VS Code Live Server 또는 아래 명령으로 실행할 수 있습니다.

```bash
cd careerlog-frontend
python3 -m http.server 5500
```

브라우저에서 `http://localhost:5500`을 엽니다.

## 주요 기능

- 날짜별 취준노트 작성 / 수정 / 삭제
- 취준노트 기반 레퍼런스 갤러리
- 탐색 기간 설정 후 공고 추천·지원 준비 시작
- 공고별 색상 Action Plan 캘린더
- 체크리스트 완료 처리

## Spring Boot API 연동

`js/api.js`에 API 요청을 모았습니다. 백엔드가 준비되면 `window.CAREERLOG_API_BASE_URL`에 API 주소를 설정하고, mock 데이터를 아래 API 호출로 바꾸면 됩니다.

| Method | Endpoint | 용도 |
|---|---|---|
| GET | `/api/career-notes?from&to` | 취준노트 목록 |
| POST | `/api/career-notes` | 취준노트 저장 |
| PATCH | `/api/career-notes/{id}` | 취준노트 수정 |
| DELETE | `/api/career-notes/{id}` | 취준노트 삭제 |
| GET | `/api/references?from&to` | LLM 추천 레퍼런스 |
| GET | `/api/job-recommendations?from&to` | 추천 공고 |
| POST | `/api/job-postings/{id}/action-plan` | 마감일 Action Plan 생성 |
| PATCH | `/api/checklist-items/{id}` | 체크리스트 완료 상태 변경 |

## GitHub 업로드

```bash
git add careerlog-frontend
git commit -m "feat: add CareerLog frontend workspace"
git push origin main
```
