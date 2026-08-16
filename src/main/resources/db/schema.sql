-- =========================================================
-- CareerLog PostgreSQL Initial Schema
-- =========================================================


-- 1. 사용자
CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,

                       login_id VARCHAR(50) NOT NULL UNIQUE,

    -- 비밀번호 원문이 아니라 암호화된 해시값 저장
                       password_hash VARCHAR(255) NOT NULL,

                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 2. 사용자의 누적 커리어 성향
CREATE TABLE career_profiles (
                                 id BIGSERIAL PRIMARY KEY,

    -- 사용자 1명당 커리어 프로필 1개만 존재
                                 user_id BIGINT NOT NULL UNIQUE,

    -- 관심 직무
    -- 예: "백엔드 개발자, 서버 개발자"
                                 preferred_jobs TEXT,

    -- 관심 요소
    -- 예: "서비스 운영, API 개발, 사용자 서비스"
                                 interests TEXT,

    -- 선호하는 업무/회사 환경
    -- 예: "자율적인 조직, 다양한 개발 경험"
                                 work_preferences TEXT,

    -- AI가 종합한 사용자 성향
                                 summary TEXT,

                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_career_profile_user
                                     FOREIGN KEY (user_id)
                                         REFERENCES users(id)
                                         ON DELETE CASCADE
);


-- 3. 매일 작성하는 취준 노트
CREATE TABLE career_notes (
                              id BIGSERIAL PRIMARY KEY,

                              user_id BIGINT NOT NULL,

                              title VARCHAR(200),

    -- 긴 줄글이므로 TEXT 사용
                              content TEXT NOT NULL,

    -- 캘린더에 표시할 실제 작성 날짜
                              note_date DATE NOT NULL,

    -- 해당 노트에 대한 AI 요약
                              ai_summary TEXT,

    -- 사용자가 직접 작성한 세 번째 질문의 답변
                              input_reason TEXT,

    -- LLM이 사실 기반으로 구조화한 결과
                              experience TEXT,
                              activities TEXT,
                              reaction TEXT,
                              structured_reason TEXT,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_career_note_user
                                  FOREIGN KEY (user_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);


-- 4. 취준 탐색 레퍼런스
-- 아티클 / 인터뷰 / 브이로그 / 블로그 등
CREATE TABLE reference_contents (
                                    id BIGSERIAL PRIMARY KEY,

                                    title VARCHAR(300) NOT NULL,

                                    url TEXT NOT NULL,

    -- ARTICLE, INTERVIEW, VLOG, VIDEO, BLOG 등
                                    reference_type VARCHAR(30) NOT NULL,

                                    thumbnail_url TEXT,

    -- YouTube, Medium, 회사 블로그 등
                                    source VARCHAR(100),

                                    description TEXT,

                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);


-- 5. 취준노트 ↔ 레퍼런스 연결
CREATE TABLE note_references (
                                 id BIGSERIAL PRIMARY KEY,

                                 career_note_id BIGINT NOT NULL,
                                 reference_id BIGINT NOT NULL,

    -- AI가 이 레퍼런스를 추천한 이유
                                 recommendation_reason TEXT,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_note_reference_note
                                     FOREIGN KEY (career_note_id)
                                         REFERENCES career_notes(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_note_reference_reference
                                     FOREIGN KEY (reference_id)
                                         REFERENCES reference_contents(id)
                                         ON DELETE CASCADE,

    -- 같은 노트에 동일한 레퍼런스가 중복 추천되는 것 방지
                                 CONSTRAINT uq_note_reference
                                     UNIQUE (career_note_id, reference_id)
);


-- 6. 외부 API에서 가져온 채용 공고
CREATE TABLE job_postings (
                              id BIGSERIAL PRIMARY KEY,

    -- 외부 API가 제공하는 공고 ID
                              external_id VARCHAR(100),

    -- 공고 출처
    -- 예: Saramin, Wanted 등
                              source VARCHAR(100),

                              company_name VARCHAR(200) NOT NULL,

                              title VARCHAR(300) NOT NULL,

                              description TEXT,

                              url TEXT NOT NULL,

                              deadline DATE,

                              location VARCHAR(200),

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- 같은 출처의 같은 공고가 중복 저장되는 것을 방지
                              CONSTRAINT uq_job_external
                                  UNIQUE (source, external_id)
);


-- 7. 사용자에게 추천된 공고
CREATE TABLE job_recommendations (
                                     id BIGSERIAL PRIMARY KEY,

                                     user_id BIGINT NOT NULL,

                                     job_posting_id BIGINT NOT NULL,

    -- 어떤 취준노트를 기반으로 추천했는지
    -- 누적 프로필만 기반으로 추천할 수도 있으므로 NULL 허용
                                     career_note_id BIGINT,

                                     recommendation_reason TEXT,

    -- RECOMMENDED / SAVED / APPLYING / APPLIED / CLOSED
                                     status VARCHAR(30) NOT NULL DEFAULT 'RECOMMENDED',

                                     recommended_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_job_recommendation_user
                                         FOREIGN KEY (user_id)
                                             REFERENCES users(id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_job_recommendation_posting
                                         FOREIGN KEY (job_posting_id)
                                             REFERENCES job_postings(id)
                                             ON DELETE CASCADE,

                                     CONSTRAINT fk_job_recommendation_note
                                         FOREIGN KEY (career_note_id)
                                             REFERENCES career_notes(id)
                                             ON DELETE SET NULL,

    -- 한 사용자에게 동일한 공고 중복 추천 방지
                                     CONSTRAINT uq_user_job_recommendation
                                         UNIQUE (user_id, job_posting_id)
);


-- 8. 공고 지원 액션 플랜
CREATE TABLE action_plans (
                              id BIGSERIAL PRIMARY KEY,

    -- 추천받은 공고 1개당 액션플랜 1개
                              job_recommendation_id BIGINT NOT NULL UNIQUE,

                              start_date DATE,

                              deadline DATE,

                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT fk_action_plan_recommendation
                                  FOREIGN KEY (job_recommendation_id)
                                      REFERENCES job_recommendations(id)
                                      ON DELETE CASCADE
);


-- 9. 날짜별 체크리스트
CREATE TABLE checklist_items (
                                 id BIGSERIAL PRIMARY KEY,

                                 action_plan_id BIGINT NOT NULL,

                                 title VARCHAR(300) NOT NULL,

                                 description TEXT,

    -- 캘린더에 표시되는 날짜
                                 due_date DATE NOT NULL,

                                 is_completed BOOLEAN NOT NULL DEFAULT FALSE,

    -- 같은 날짜 내에서 보여줄 순서 등에 활용
                                 sort_order INTEGER NOT NULL DEFAULT 0,

                                 created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT fk_checklist_action_plan
                                     FOREIGN KEY (action_plan_id)
                                         REFERENCES action_plans(id)
                                         ON DELETE CASCADE
);


-- =========================================================
-- 조회 성능용 INDEX
-- =========================================================

-- 사용자의 특정 날짜 취준노트 조회
CREATE INDEX idx_career_notes_user_date
    ON career_notes(user_id, note_date);


-- 사용자별 추천 공고 조회
CREATE INDEX idx_job_recommendations_user
    ON job_recommendations(user_id);


-- 캘린더에서 날짜별 체크리스트 조회
CREATE INDEX idx_checklist_due_date
    ON checklist_items(due_date);


-- 액션플랜별 체크리스트 조회
CREATE INDEX idx_checklist_action_plan
    ON checklist_items(action_plan_id);
