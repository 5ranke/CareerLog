# CareerLog 개발 대화 및 구현 기록

> 작성일: 2026-08-16  
> 프로젝트 경로: `/Users/admin/Desktop/CareerLog`  
> 실제 API 키, 데이터베이스 비밀번호, 세션 토큰 등 보안 정보는 포함하지 않았습니다.

## 1. 프로젝트 시작 및 배포 준비

### 사용자 요청

> 이따가 배포할 예정이니 `.gitignore`를 먼저 설정하고, `.env` 파일과 Docker 이미지 및 EC2 배포를 고려해 달라. 이미 저장소에는 첫 커밋이 올라가 있다.

### 처리 내용

- `.env`, 인증서, 키, 빌드 결과물, IDE 설정, 로그 파일을 Git 추적에서 제외했습니다.
- `.env.example`은 저장소에 포함할 수 있도록 예외 처리했습니다.
- 이미 Git에 올라간 비밀 파일은 `.gitignore`만 추가해서는 제거되지 않으므로 `git rm --cached`가 필요하다는 방향을 정리했습니다.
- 이후 Docker Compose와 EC2 배포 구성을 추가했습니다.

---

## 2. 전체 서비스 구조 설계

### 사용자 요청

> SQL 파일을 확인하고 백엔드를 먼저 설계하자.
>
> 회원가입 / 로그인 → USER → 매일 취준노트 작성 → CAREER_NOTE → LLM → CAREER_PROFILE 갱신 → 레퍼런스 및 외부 채용 API → 추천 공고 → 지원 및 저장 → ACTION_PLAN → CHECKLIST_ITEM → Calendar
>
> 첫 페이지는 사용자를 구분하기 위한 로그인 화면이 될 것 같다.

### 설계 방향

1. 사용자는 회원가입과 로그인으로 구분합니다.
2. 사용자는 날짜별 취준노트를 작성합니다.
3. LLM은 노트를 구조화합니다.
4. 레퍼런스와 채용 공고는 노트 기록을 바탕으로 연결합니다.
5. 사용자가 공고 등록을 선택하면 액션 플랜을 만듭니다.
6. 액션 플랜은 마감일까지 날짜별 체크리스트를 생성합니다.
7. 체크리스트와 공고 마감일을 캘린더에 표시합니다.

해커톤 일정에 맞춰 실제 외부 API나 크롤링보다 데모 데이터를 우선 연결하고, 교체 가능한 구조로 구현했습니다.

---

## 3. 회원가입 및 로그인 API

### 사용자 요청

> 화면은 나중에 만들 예정이니 API부터 만들어 달라.

### 구현 결과

- 세션 기반 인증
- BCrypt 비밀번호 암호화
- CSRF 토큰 처리
- 회원가입
- 로그인
- 로그아웃
- 현재 로그인 사용자 조회

### 주요 API

```http
GET /api/auth/csrf
POST /api/auth/signup
POST /api/auth/login
POST /api/auth/logout
GET /api/auth/me
```

회원가입 아이디는 한글, 영문, 숫자, 점, 밑줄, 하이픈을 지원하며 2자 이상으로 완화했습니다. 비밀번호는 8자 이상입니다.

---

## 4. PostgreSQL 연결

### 사용자 요청

> DB 접속 정보를 설정하고, SQL밖에 없는 상태에서 데이터베이스를 만들고 연결해 달라.

### 구현 결과

- PostgreSQL 데이터베이스 `careerlog` 연결
- 애플리케이션 계정 `careerlog_app` 사용
- 접속 정보는 `.env`에서 읽도록 구성
- Hibernate는 `ddl-auto: validate`를 사용하여 스키마가 코드와 일치하는지 검증
- 실제 비밀번호는 Git에서 제외

### 주요 환경변수

```dotenv
DB_URL=jdbc:postgresql://localhost:5432/careerlog
DB_USERNAME=careerlog_app
DB_PASSWORD=<비밀번호>
```

---

## 5. 취준노트 API 및 AI 연동

### 초기 요청

> 다음 단계를 구현하되 해커톤이므로 테스트를 복잡하게 하지 말고 기능 구현을 우선하자.

### 초기 구현

- 취준노트 생성
- 날짜 범위 조회
- 단건 조회
- 수정
- 삭제
- 저장 시 OpenAI 분석

### 주요 API

```http
POST /api/career-notes
GET /api/career-notes?from=YYYY-MM-DD&to=YYYY-MM-DD
GET /api/career-notes/{noteId}
PUT /api/career-notes/{noteId}
DELETE /api/career-notes/{noteId}
POST /api/career-notes/{noteId}/ai-summary
```

---

## 6. OpenAI API 키와 크레딧

### 사용자 질문

> API 키는 어떻게 만드는가?

> Codex 크레딧과 OpenAI API 크레딧은 다른가?

> 크레딧을 충전했으니 다시 시도해 달라.

> 토큰이 50달러밖에 없으니 아껴 달라.

### 정리

- Codex 사용 크레딧과 OpenAI API 플랫폼 결제는 별도일 수 있습니다.
- 애플리케이션의 LLM 기능은 `OPENAI_API_KEY`를 통해 OpenAI API 플랫폼을 호출합니다.
- API 키는 프론트엔드가 아닌 백엔드 환경변수에만 저장합니다.
- 기본 모델은 비용 절약을 위해 `gpt-4o-mini`로 설정했습니다.
- 테스트에서는 OpenAI 키를 강제로 비활성화하여 크레딧이 소비되지 않게 했습니다.
- 체크리스트 생성은 AI가 아닌 규칙 기반으로 구현하여 크레딧을 사용하지 않습니다.

```dotenv
OPENAI_API_KEY=<API 키>
OPENAI_MODEL=gpt-4o-mini
OPENAI_BASE_URL=https://api.openai.com
```

---

## 7. 레퍼런스 기능

### 사용자 질문 및 요청

> 레퍼런스는 크롤링인가? 어떤 방식으로 선정되는가?

> 데모로 우선 구현하자.

> 추후에는 인터넷에서 가져온 자료 중 사용자가 참고하고 싶은 내용만 저장하는 방식일 것이다.

### 구현 방향

- 현재는 데모 레퍼런스 데이터를 사용합니다.
- 취준노트의 주요 키워드와 레퍼런스 제목·설명·출처를 비교해 순위를 정합니다.
- 추후 크롤러나 검색 API를 `REFERENCE` 저장 과정 앞에 연결할 수 있습니다.
- 사용자가 저장을 선택한 자료만 노트와 연결하는 구조입니다.

### 데모 초기 데이터

- Spring Boot 공식 레퍼런스
- Spring Security 인증 가이드
- PostgreSQL 공식 튜토리얼
- GitHub Actions 빠른 시작

---

## 8. 프론트엔드 통합

### 사용자 요청

> `frontend` 폴더의 HTML, CSS, JS 원본은 최대한 건드리지 말고 필요한 파일을 백엔드 쪽으로 가져와 적절히 배치하자.

> API부터 연결하고 서버를 띄우면 어떤 주소로 접속하는지 알려 달라.

### 구현 결과

- Gradle `processResources`에서 `frontend` 파일을 Spring Boot 정적 리소스로 복사합니다.
- 백엔드 연동이 필요한 `api.js`, `app.js`는 `src/main/resources/static/js`에서 관리합니다.
- Spring Boot가 프론트와 API를 같은 서버에서 제공합니다.

### 접속 주소

```text
http://localhost:8080
```

---

## 9. 로그인 및 회원가입 프론트 연동 점검

### 사용자 요청

> 회원가입과 로그인이 되지 않으니 로직을 점검해 달라.

### 수정 결과

- CSRF 쿠키와 헤더 연결
- 세션 쿠키 유지
- 회원가입 성공 후 자동 로그인
- 로그인 후 사용자 정보 표시
- 입력 검증 문구 개선
- 인증 실패 상태별 안내 처리

HTTP 기준으로 CSRF, 회원가입, 로그인, 현재 사용자 조회까지 정상 동작을 확인했습니다.

---

## 10. 채용 공고 검색 및 추천

### 사용자 요청

> 오른쪽 상단의 `지원 준비 시작`에서 기간을 선택하고, 해당 기간의 취준노트를 확인해 지원 공고를 검색하게 연결해 달라. 디자인은 거의 수정하지 말자.

### 구현 결과

- 취준노트 탐색 시작일과 종료일 선택
- 선택 기간의 취준노트 조회
- 노트 키워드와 공고 설명 비교
- 추천 공고 목록과 상세 표시
- 데모 공고 데이터 자동 생성

### 주요 API

```http
POST /api/job-recommendations/search
```

요청 예시:

```json
{
  "from": "2026-08-01",
  "to": "2026-08-31"
}
```

---

## 11. 지원 공고 등록과 일일 체크리스트

### 사용자 요청

> 채용 공고 검색 후 그 공고를 준비하기 위한 체크리스트를 마감일까지 매일 생성하고 캘린더에 표시하자.
>
> 채용 마감 날짜도 캘린더에 표시하자.
>
> 체크리스트와 채용 공고를 등록할지 사용자가 선택할 수 있게 하자.

### 구현 결과

- 검색 결과 확인만으로는 캘린더에 등록되지 않습니다.
- `지원 준비 등록` 버튼을 누르면 확인 창이 표시됩니다.
- 사용자는 다음 중 하나를 선택합니다.
  - 등록하지 않기
  - 공고와 체크리스트 등록
- 등록하면 오늘부터 마감일까지 하루 한 개의 준비 항목을 생성합니다.
- 마감일을 캘린더에 표시합니다.
- 등록 후 공고의 마감 월로 자동 이동합니다.
- 이전 달과 다음 달 이동 버튼을 추가했습니다.

### 체크리스트 순서

1. 공고 핵심 업무와 자격 요건 표시
2. 회사와 서비스 조사
3. 연결할 경험 선정
4. STAR 경험 정리
5. 이력서 보완
6. 자기소개서 보완
7. 포트폴리오 보완
8. 예상 면접 질문 정리
9. 오탈자 및 제출 파일 점검
10. 지원서 제출

### 주요 API

```http
POST /api/job-postings/{jobPostingId}/action-plan
GET /api/action-plans
GET /api/action-plans/calendar?from=YYYY-MM-DD&to=YYYY-MM-DD
PATCH /api/checklist-items/{itemId}
DELETE /api/checklist-items/{itemId}
DELETE /api/action-plans/{actionPlanId}
```

---

## 12. 취준노트 입력 구조 변경

### 사용자 요청

> 취준노트 입력을 다음 세 질문으로 바꾸자.
>
> 1. 오늘 취준과 관련해서 무엇을 했거나 접했나요?
> 2. 그중 어떤 점이 가장 기억에 남았나요?
> 3. 왜 그렇게 느꼈던 것 같나요? 선택
>
> 저장 시 LLM은 `experience`, `activities`, `reaction`, `reason`만 추출한다.
>
> 이 단계에서는 성향, 능력, 적합 직무를 추론하지 않고 기록에 없는 내용을 생성하지 않는다.

### 요청 형식

```json
{
  "whatDidYouDo": "게임회사 현직자와 커피챗",
  "memorablePoint": "사용자 데이터로 이탈 원인을 분석하는 업무",
  "reason": "원인을 찾는 것을 좋아해서",
  "noteDate": "2026-08-16"
}
```

### 구조화 결과

```json
{
  "experience": "게임회사 현직자와 커피챗",
  "activities": [
    "사용자 데이터 분석",
    "이탈 원인 분석"
  ],
  "reaction": "이탈 원인을 분석하는 일이 재미있어 보였음",
  "reason": "원인을 찾는 것을 좋아해서"
}
```

### 반영된 원칙

- 노트 저장 단계에서 관심 직무와 커리어 프로필을 자동 추론하지 않습니다.
- 입력에 없는 사실, 감정, 이유를 만들지 않도록 프롬프트를 제한했습니다.
- 세 번째 답변이 없으면 `reason`은 `null`입니다.
- `reason`은 AI 결과보다 사용자가 직접 작성한 값을 그대로 보존합니다.
- AI 분석 실패 시에도 원문 노트는 저장됩니다.
- 노트 수정 시 변경된 내용으로 다시 구조화합니다.
- 비용 절약을 위해 입력은 항목별 최대 2,000자, 출력은 최대 250토큰으로 제한했습니다.

### DB 추가 필드

```text
input_reason
experience
activities
reaction
structured_reason
```

---

## 13. 체크리스트 중복 클릭 문제 수정

### 사용자 요청

> 캘린더에서 체크리스트 하나를 누르면 다른 항목도 같이 선택되는 문제를 확인해 달라.

### 원인과 수정

- 체크리스트 버튼 내부에 네이티브 체크박스가 중첩되어 있었습니다.
- 브라우저 기본 체크 동작과 상위 버튼 이벤트가 겹칠 수 있는 구조였습니다.
- 중첩 체크박스를 제거하고 하나의 클릭 요소로 통합했습니다.
- 클릭한 체크리스트 ID 하나만 API 응답값으로 갱신합니다.
- 요청 처리 중 같은 항목의 중복 클릭을 차단합니다.
- 완료 상태는 `☐`, `☑`로 표시합니다.

---

## 14. 삭제 및 수정 기능

### 사용자 요청

> 지원 공고와 체크리스트를 삭제할 수 있게 하고, 기존 취준노트도 수정할 수 있게 하자.
>
> 체크리스트 오른쪽에 삭제 버튼을 만들고 확인 팝업을 표시하자.
>
> 공고 삭제 시 관련 체크리스트도 삭제하자.

### 구현 결과

- 체크리스트 오른쪽에 `×` 삭제 버튼 추가
- 삭제 전 확인 팝업 표시
- 개별 체크리스트 삭제 지원
- 공고 상세에 `지원 공고 삭제` 버튼 추가
- 공고 삭제 시 액션 플랜과 전체 체크리스트 연쇄 삭제
- 캘린더 마감 공고 오른쪽에도 `×` 삭제 버튼 추가
- 캘린더에서 공고 삭제 시 관련 체크리스트와 마감 일정 삭제
- 기존 취준노트에 `취준노트 수정` 버튼 표시

DB 외래키의 `ON DELETE CASCADE`를 사용해 액션 플랜 삭제 시 체크리스트가 함께 삭제됩니다.

---

## 15. 메인 컬러 변경

### 사용자 요청

> 메인 페이지 컬러를 `#1E3A5F`로 바꾸고, 좌측 탭과 프로필의 연두색도 어울리는 남색 계열로 변경하자.

### 구현 결과

- 메인 버튼 컬러: `#1E3A5F`
- 오늘 날짜 강조 컬러: `#1E3A5F`
- 좌측 사이드바 배경: `#1E3A5F`
- 프로필 아이콘: 밝은 블루 배경과 남색 텍스트
- 좌측 탭 기본 텍스트: 밝은 블루
- 좌측 탭 선택 및 호버: 중간 블루
- 사이드 카드와 안내 영역: 어두운 블루 계열
- 본문 레이아웃과 기능은 유지

---

## 16. Docker Compose 및 EC2 배포

### 사용자 요청

> EC2에서 저장소를 clone한 뒤 Compose로 실행할 예정이다.
>
> 서버 최초 시작 시 필요한 데이터를 넣고 Compose 파일과 AI 작동 여부, `.env` 구성까지 점검하자.

### 추가된 파일

- `Dockerfile`
- `compose.yaml`
- `.dockerignore`
- 배포용 항목이 보완된 `.env.example`

### Compose 구성

```text
app
 ├─ Spring Boot 4.1
 ├─ Java 17
 ├─ 정적 프론트엔드 제공
 └─ OpenAI Responses API 호출

db
 ├─ PostgreSQL 16 Alpine
 ├─ 영구 볼륨 postgres_data
 ├─ 최초 실행 시 schema.sql 적용
 └─ healthcheck 통과 후 app 시작
```

### 최초 데이터

빈 PostgreSQL 볼륨에서 처음 시작하면 다음 순서로 처리됩니다.

1. `schema.sql` 실행
2. 테이블, 외래키 및 인덱스 생성
3. 앱 시작
4. 데모 채용 공고 3개 삽입
5. 데모 레퍼런스 4개 삽입

데모 공고 마감일은 서버 최초 실행일을 기준으로 14일, 21일, 30일 후입니다.

### EC2 `.env` 예시

```dotenv
POSTGRES_DB=careerlog
DB_USERNAME=careerlog_app
DB_PASSWORD=<긴 랜덤 비밀번호>

APP_PORT=8080

OPENAI_API_KEY=<OpenAI API 키>
OPENAI_MODEL=gpt-4o-mini
OPENAI_BASE_URL=https://api.openai.com

FRONTEND_ORIGINS=http://EC2_PUBLIC_IP:8080
```

`.env`는 Git에 올리지 않고 EC2 안에서 직접 생성합니다.

```bash
cp .env.example .env
nano .env
chmod 600 .env
```

안전한 DB 비밀번호 생성 예시:

```bash
openssl rand -hex 24
```

### Git 반영

EC2에서 clone하기 전에 로컬 변경사항을 원격 저장소에 올립니다.

```bash
git add Dockerfile compose.yaml .dockerignore .env.example \
  src/main/java/com/team03/careerlog/job/DemoJobDataInitializer.java

git commit -m "chore: add EC2 docker compose deployment"
git push origin main
```

### EC2 실행

```bash
git clone <GITHUB_REPOSITORY_URL>
cd CareerLog

cp .env.example .env
nano .env
chmod 600 .env

docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs -f app
```

접속 주소:

```text
http://EC2_PUBLIC_IP:8080
```

EC2 보안 그룹 인바운드에서 TCP 8080을 허용해야 합니다. 운영 환경에서 Nginx와 도메인을 사용할 경우 80과 443만 외부에 공개하고 8080은 내부로 제한하는 구성이 적합합니다.

### 재배포

```bash
git pull
docker compose up -d --build
docker compose logs -f app
```

### 데이터베이스 초기화 주의

일반적인 재배포에서는 `postgres_data` 볼륨이 유지되어 사용자 데이터가 보존됩니다.

아래 명령은 기존 DB를 완전히 삭제하므로 정말 초기화가 필요할 때만 사용합니다.

```bash
docker compose down -v
docker compose up -d --build
```

---

## 17. 배포 후 AI 작동 조건

다음 조건이 충족되면 EC2에서도 AI 분석이 작동합니다.

- 유효한 `OPENAI_API_KEY`
- OpenAI API 결제 크레딧
- EC2 아웃바운드 HTTPS 443 허용
- 설정한 모델의 API 사용 권한
- 정상적인 EC2 시스템 시간

API 키와 외부 연결 확인:

```bash
set -a
source .env
set +a

curl -sS -o /dev/null -w '%{http_code}\n' \
  https://api.openai.com/v1/models \
  -H "Authorization: Bearer $OPENAI_API_KEY"
```

`200`이면 키 인증과 외부 네트워크 연결이 정상입니다.

AI 키는 브라우저 코드에 포함되지 않고 백엔드 컨테이너 환경변수에만 전달됩니다.

---

## 18. 현재 주요 기능 요약

- 회원가입 / 로그인 / 로그아웃
- 사용자별 세션 분리
- 날짜별 취준노트 생성 / 조회 / 수정 / 삭제
- 취준노트의 사실 기반 AI 구조화
- 데모 레퍼런스 조회 및 추천
- 기간별 취준노트 기반 데모 공고 추천
- 사용자가 선택한 공고만 지원 준비로 등록
- 마감일까지 일일 체크리스트 자동 생성
- 체크리스트 완료 상태 변경
- 개별 체크리스트 삭제
- 공고 및 연결 체크리스트 전체 삭제
- 캘린더 공고 마감일 표시
- 달력 월 이동
- Docker Compose 기반 EC2 배포
- PostgreSQL 최초 스키마 및 데모 데이터 자동 생성

---

## 19. 주요 접속 및 운영 명령

### 로컬

```text
http://localhost:8080
```

### EC2

```text
http://EC2_PUBLIC_IP:8080
```

### 컨테이너 상태

```bash
docker compose ps
```

### 앱 로그

```bash
docker compose logs -f app
```

### DB 로그

```bash
docker compose logs -f db
```

### 재시작

```bash
docker compose restart app
```

### 종료

```bash
docker compose down
```

`docker compose down`은 기본적으로 DB 볼륨을 삭제하지 않습니다.
