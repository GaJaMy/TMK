-- =============================================================
-- TMK (Test My Knowledge) DDL
-- DB      : PostgreSQL 14+
-- Version : v3.0.0
-- Date    : 2026-04-27
-- Basis   : TMK(Test My Knowledge).md, ERD 설계.md, API 명세서.md
-- Note    : 목표 요구사항 기준 스키마, DB 외래키 제약은 두지 않음
-- =============================================================

CREATE EXTENSION IF NOT EXISTS vector;


-- =============================================================
-- TABLE: user
-- =============================================================
CREATE TABLE "user"
(
    id           BIGSERIAL    PRIMARY KEY,
    username     VARCHAR(50)  NOT NULL,
    password     VARCHAR(255) NOT NULL,
    active       BOOLEAN      NOT NULL DEFAULT TRUE,
    country_code VARCHAR(10)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_username UNIQUE (username)
);

COMMENT ON TABLE "user" IS '일반 사용자 계정';
COMMENT ON COLUMN "user".username IS '로그인 아이디';
COMMENT ON COLUMN "user".password IS 'bcrypt 암호화 비밀번호';
COMMENT ON COLUMN "user".active IS '계정 활성 여부';
COMMENT ON COLUMN "user".country_code IS '문제/정답/해설 생성 언어 결정을 위한 국가 코드';


-- =============================================================
-- TABLE: admin
-- =============================================================
CREATE TABLE admin
(
    id                  BIGSERIAL    PRIMARY KEY,
    username            VARCHAR(50)  NOT NULL,
    password            VARCHAR(255) NOT NULL,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by_admin_id BIGINT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_admin_username UNIQUE (username)
);

COMMENT ON TABLE admin IS '관리자 계정';
COMMENT ON COLUMN admin.created_by_admin_id IS '이 계정을 생성한 관리자 ID';


-- =============================================================
-- TABLE: topic
-- =============================================================
CREATE TABLE topic
(
    id                  BIGSERIAL    PRIMARY KEY,
    name                VARCHAR(100) NOT NULL,
    description         TEXT,
    active              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by_admin_id BIGINT       NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_topic_name UNIQUE (name)
);

COMMENT ON TABLE topic IS '공용 문제 Topic';
COMMENT ON COLUMN topic.active IS 'Topic 활성 여부';
COMMENT ON COLUMN topic.created_by_admin_id IS '생성한 관리자 ID';


-- =============================================================
-- TABLE: document
-- =============================================================
CREATE TABLE document
(
    id                       BIGSERIAL     PRIMARY KEY,
    user_id                  BIGINT        NOT NULL,
    title                    VARCHAR(500)  NOT NULL,
    source_type              VARCHAR(20)   NOT NULL,
    source_reference         VARCHAR(1000) NOT NULL,
    status                   VARCHAR(20)   NOT NULL,
    generated_question_count INTEGER       NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_document_source_type
        CHECK (source_type IN ('PDF_UPLOAD', 'NOTION', 'URL')),
    CONSTRAINT chk_document_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED')),
    CONSTRAINT chk_document_generated_question_count
        CHECK (generated_question_count >= 0)
);

COMMENT ON TABLE document IS '사용자 문서 메타데이터';
COMMENT ON COLUMN document.source_reference IS '업로드 파일명 또는 노션/URL 참조값';
COMMENT ON COLUMN document.status IS '문서 처리 상태';
COMMENT ON COLUMN document.generated_question_count IS '생성된 개인 문제 수';


-- =============================================================
-- TABLE: document_chunk
-- =============================================================
CREATE TABLE document_chunk
(
    id          BIGSERIAL    PRIMARY KEY,
    document_id BIGINT       NOT NULL,
    chunk_index SMALLINT     NOT NULL,
    content     TEXT         NOT NULL,
    embedding   vector(1536) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_document_chunk_document_id_chunk_index UNIQUE (document_id, chunk_index),
    CONSTRAINT chk_document_chunk_index
        CHECK (chunk_index >= 0)
);

COMMENT ON TABLE document_chunk IS '문서 청크 및 임베딩';


-- =============================================================
-- TABLE: private_question
-- =============================================================
CREATE TABLE private_question
(
    id           BIGSERIAL   PRIMARY KEY,
    user_id       BIGINT      NOT NULL,
    document_id   BIGINT      NOT NULL,
    content       TEXT        NOT NULL,
    type          VARCHAR(30) NOT NULL,
    difficulty    VARCHAR(10) NOT NULL,
    answer        TEXT        NOT NULL,
    explanation   TEXT        NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_private_question_type
        CHECK (type IN ('MULTIPLE_CHOICE', 'SHORT_ANSWER', 'TRUE_FALSE')),
    CONSTRAINT chk_private_question_difficulty
        CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD'))
);

COMMENT ON TABLE private_question IS '사용자 문서에서 AI가 생성한 개인 문제';
COMMENT ON COLUMN private_question.language_code IS '문제/정답/해설 생성 언어 코드';


-- =============================================================
-- TABLE: private_question_option
-- =============================================================
CREATE TABLE private_question_option
(
    id                   BIGSERIAL PRIMARY KEY,
    private_question_id  BIGINT    NOT NULL,
    option_number        SMALLINT  NOT NULL,
    content              TEXT      NOT NULL,

    CONSTRAINT uq_private_question_option_question_id_option_number
        UNIQUE (private_question_id, option_number),
    CONSTRAINT chk_private_question_option_number
        CHECK (option_number BETWEEN 1 AND 5)
);

COMMENT ON TABLE private_question_option IS '개인 문제 선택지';


-- =============================================================
-- TABLE: public_question
-- =============================================================
CREATE TABLE public_question
(
    id                  BIGSERIAL   PRIMARY KEY,
    topic_id            BIGINT      NOT NULL,
    created_by_admin_id BIGINT      NOT NULL,
    active              BOOLEAN     NOT NULL DEFAULT TRUE,
    content             TEXT        NOT NULL,
    type                VARCHAR(30) NOT NULL,
    difficulty          VARCHAR(10) NOT NULL,
    answer              TEXT        NOT NULL,
    explanation         TEXT        NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_public_question_type
        CHECK (type IN ('MULTIPLE_CHOICE', 'SHORT_ANSWER', 'TRUE_FALSE')),
    CONSTRAINT chk_public_question_difficulty
        CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD'))
);

COMMENT ON TABLE public_question IS '관리자가 직접 등록한 공용 문제';
COMMENT ON COLUMN public_question.active IS '공용 문제 활성 여부';


-- =============================================================
-- TABLE: public_question_option
-- =============================================================
CREATE TABLE public_question_option
(
    id                  BIGSERIAL PRIMARY KEY,
    public_question_id  BIGINT    NOT NULL,
    option_number       SMALLINT  NOT NULL,
    content             TEXT      NOT NULL,

    CONSTRAINT uq_public_question_option_question_id_option_number
        UNIQUE (public_question_id, option_number),
    CONSTRAINT chk_public_question_option_number
        CHECK (option_number BETWEEN 1 AND 5)
);

COMMENT ON TABLE public_question_option IS '공용 문제 선택지';


-- =============================================================
-- TABLE: exam
-- =============================================================
CREATE TABLE exam
(
    id                  BIGSERIAL   PRIMARY KEY,
    user_id             BIGINT      NOT NULL,
    topic_id            BIGINT,
    document_id         BIGINT,
    source_type         VARCHAR(30) NOT NULL,
    total_questions     SMALLINT    NOT NULL,
    time_limit_minutes  SMALLINT    NOT NULL,
    status              VARCHAR(20) NOT NULL,
    started_at          TIMESTAMPTZ,
    expired_at          TIMESTAMPTZ,
    submitted_at        TIMESTAMPTZ,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_exam_source_type
        CHECK (source_type IN ('PUBLIC_TOPIC', 'PRIVATE_DOCUMENT')),
    CONSTRAINT chk_exam_status
        CHECK (status IN ('CREATED', 'IN_PROGRESS', 'SUBMITTED')),
    CONSTRAINT chk_exam_total_questions
        CHECK (total_questions > 0),
    CONSTRAINT chk_exam_time_limit_minutes
        CHECK (time_limit_minutes > 0),
    CONSTRAINT chk_exam_source_target
        CHECK (
            (source_type = 'PUBLIC_TOPIC' AND topic_id IS NOT NULL AND document_id IS NULL)
            OR
            (source_type = 'PRIVATE_DOCUMENT' AND topic_id IS NULL AND document_id IS NOT NULL)
        ),
    CONSTRAINT chk_exam_started_and_expired_pair
        CHECK (
            (started_at IS NULL AND expired_at IS NULL)
            OR
            (started_at IS NOT NULL AND expired_at IS NOT NULL)
        )
);

COMMENT ON TABLE exam IS '시험 세션';
COMMENT ON COLUMN exam.source_type IS '시험 출처 유형';
COMMENT ON COLUMN exam.time_limit_minutes IS '사용자 지정 시험 시간(분)';


-- =============================================================
-- TABLE: exam_question
-- =============================================================
CREATE TABLE exam_question
(
    id                   BIGSERIAL PRIMARY KEY,
    exam_id              BIGINT    NOT NULL,
    public_question_id   BIGINT,
    private_question_id  BIGINT,
    question_scope       VARCHAR(20) NOT NULL,
    order_num            SMALLINT  NOT NULL,
    my_answer            TEXT,
    is_correct           BOOLEAN,

    CONSTRAINT chk_exam_question_scope
        CHECK (question_scope IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_exam_question_order_num
        CHECK (order_num > 0),
    CONSTRAINT uq_exam_question_exam_id_order_num
        UNIQUE (exam_id, order_num),
    CONSTRAINT chk_exam_question_reference
        CHECK (
            (question_scope = 'PUBLIC' AND public_question_id IS NOT NULL AND private_question_id IS NULL)
            OR
            (question_scope = 'PRIVATE' AND public_question_id IS NULL AND private_question_id IS NOT NULL)
        )
);

COMMENT ON TABLE exam_question IS '시험 문항, 답안, 채점 결과';


-- =============================================================
-- TABLE: daily_activity_stat
-- =============================================================
CREATE TABLE daily_activity_stat
(
    id                                 BIGSERIAL   PRIMARY KEY,
    stat_date                          DATE        NOT NULL,
    user_page_access_attempt_count     INTEGER     NOT NULL DEFAULT 0,
    exam_run_count                     INTEGER     NOT NULL DEFAULT 0,
    document_registration_count        INTEGER     NOT NULL DEFAULT 0,
    generated_private_question_count   INTEGER     NOT NULL DEFAULT 0,
    created_at                         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                         TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_daily_activity_stat_date UNIQUE (stat_date),
    CONSTRAINT chk_daily_activity_stat_user_page_access_attempt_count
        CHECK (user_page_access_attempt_count >= 0),
    CONSTRAINT chk_daily_activity_stat_exam_run_count
        CHECK (exam_run_count >= 0),
    CONSTRAINT chk_daily_activity_stat_document_registration_count
        CHECK (document_registration_count >= 0),
    CONSTRAINT chk_daily_activity_stat_generated_private_question_count
        CHECK (generated_private_question_count >= 0)
);

COMMENT ON TABLE daily_activity_stat IS '관리자 모니터링용 일별 집계';


-- =============================================================
-- INDEX: user
-- =============================================================
CREATE INDEX idx_user_active
    ON "user" (active);


-- =============================================================
-- INDEX: admin
-- =============================================================
CREATE INDEX idx_admin_active
    ON admin (active);

CREATE INDEX idx_admin_created_by_admin_id
    ON admin (created_by_admin_id);


-- =============================================================
-- INDEX: topic
-- =============================================================
CREATE INDEX idx_topic_active
    ON topic (active);

CREATE INDEX idx_topic_created_by_admin_id
    ON topic (created_by_admin_id);


-- =============================================================
-- INDEX: document
-- =============================================================
CREATE INDEX idx_document_user_id_created_at
    ON document (user_id, created_at DESC);

CREATE INDEX idx_document_status
    ON document (status);


-- =============================================================
-- INDEX: document_chunk
-- =============================================================
CREATE INDEX idx_document_chunk_document_id
    ON document_chunk (document_id);

CREATE INDEX idx_document_chunk_embedding_hnsw
    ON document_chunk USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);


-- =============================================================
-- INDEX: private_question
-- =============================================================
CREATE INDEX idx_private_question_user_id
    ON private_question (user_id);

CREATE INDEX idx_private_question_document_id
    ON private_question (document_id);

CREATE INDEX idx_private_question_difficulty
    ON private_question (difficulty);


-- =============================================================
-- INDEX: private_question_option
-- =============================================================
CREATE INDEX idx_private_question_option_question_id
    ON private_question_option (private_question_id);


-- =============================================================
-- INDEX: public_question
-- =============================================================
CREATE INDEX idx_public_question_topic_id
    ON public_question (topic_id);

CREATE INDEX idx_public_question_active
    ON public_question (active);

CREATE INDEX idx_public_question_type_difficulty
    ON public_question (type, difficulty);

CREATE INDEX idx_public_question_created_by_admin_id
    ON public_question (created_by_admin_id);


-- =============================================================
-- INDEX: public_question_option
-- =============================================================
CREATE INDEX idx_public_question_option_question_id
    ON public_question_option (public_question_id);


-- =============================================================
-- INDEX: exam
-- =============================================================
CREATE INDEX idx_exam_user_id_created_at
    ON exam (user_id, created_at DESC);

CREATE INDEX idx_exam_topic_id
    ON exam (topic_id);

CREATE INDEX idx_exam_document_id
    ON exam (document_id);

CREATE INDEX idx_exam_expired_at_in_progress
    ON exam (expired_at)
    WHERE status = 'IN_PROGRESS';


-- =============================================================
-- INDEX: exam_question
-- =============================================================
CREATE INDEX idx_exam_question_exam_id_order
    ON exam_question (exam_id, order_num);

CREATE INDEX idx_exam_question_public_question_id
    ON exam_question (public_question_id);

CREATE INDEX idx_exam_question_private_question_id
    ON exam_question (private_question_id);


-- =============================================================
-- INDEX: daily_activity_stat
-- =============================================================
CREATE INDEX idx_daily_activity_stat_created_at
    ON daily_activity_stat (created_at DESC);
