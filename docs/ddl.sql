-- =============================================================
-- TMK (Test My Knowledge) DDL
-- DB      : PostgreSQL
-- Version : v2.0.0
-- Date    : 2026-04-27
-- Note    : 외래키 제약조건은 문서상 생략하고 애플리케이션 레벨에서 관리 가능
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
    role         VARCHAR(20)  NOT NULL DEFAULT 'USER',
    country_code VARCHAR(10)  NOT NULL,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_username UNIQUE (username),
    CONSTRAINT chk_user_role CHECK (role IN ('USER', 'ADMIN'))
);

COMMENT ON TABLE "user" IS '사용자';
COMMENT ON COLUMN "user".username IS '로그인 아이디';
COMMENT ON COLUMN "user".password IS 'bcrypt 암호화 비밀번호';
COMMENT ON COLUMN "user".role IS '권한(USER, ADMIN)';
COMMENT ON COLUMN "user".country_code IS '문제 생성 언어 결정을 위한 국가 코드';


-- =============================================================
-- TABLE: topic
-- =============================================================
CREATE TABLE topic
(
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    active      BOOLEAN      NOT NULL DEFAULT TRUE,
    created_by  BIGINT       NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_topic_name UNIQUE (name)
);

COMMENT ON TABLE topic IS '공용 문제 Topic';
COMMENT ON COLUMN topic.created_by IS '생성한 admin 사용자 ID';


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
    status                   VARCHAR(20)   NOT NULL DEFAULT 'PROCESSING',
    generated_question_count INTEGER       NOT NULL DEFAULT 0,
    created_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at               TIMESTAMPTZ   NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_document_source_type
        CHECK (source_type IN ('PDF_UPLOAD', 'NOTION', 'URL')),
    CONSTRAINT chk_document_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE document IS '사용자 문서 메타데이터';
COMMENT ON COLUMN document.source_reference IS '업로드 파일명 또는 노션/URL 참조값';
COMMENT ON COLUMN document.generated_question_count IS '생성된 문제 수';


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
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE document_chunk IS '문서 청크 및 임베딩';


-- =============================================================
-- TABLE: question
-- =============================================================
CREATE TABLE question
(
    id            BIGSERIAL   PRIMARY KEY,
    owner_user_id BIGINT,
    topic_id      BIGINT,
    document_id   BIGINT,
    created_by    BIGINT,
    scope         VARCHAR(20) NOT NULL,
    source_type   VARCHAR(40) NOT NULL,
    content       TEXT        NOT NULL,
    type          VARCHAR(30) NOT NULL,
    difficulty    VARCHAR(10) NOT NULL,
    answer        TEXT        NOT NULL,
    explanation   TEXT        NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_question_scope
        CHECK (scope IN ('PUBLIC', 'PRIVATE')),
    CONSTRAINT chk_question_source_type
        CHECK (source_type IN ('PUBLIC_MANUAL', 'PRIVATE_DOCUMENT_GENERATED')),
    CONSTRAINT chk_question_type
        CHECK (type IN ('MULTIPLE_CHOICE', 'SHORT_ANSWER', 'TRUE_FALSE')),
    CONSTRAINT chk_question_difficulty
        CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD'))
);

COMMENT ON TABLE question IS '문제';
COMMENT ON COLUMN question.owner_user_id IS '개인 문제 소유 사용자 ID';
COMMENT ON COLUMN question.topic_id IS '공용 문제 Topic ID';
COMMENT ON COLUMN question.document_id IS '문제 생성 기반 문서 ID';
COMMENT ON COLUMN question.created_by IS '공용 문제를 등록한 admin ID';
COMMENT ON COLUMN question.language_code IS '문제/정답/해설 언어 코드';


-- =============================================================
-- TABLE: question_option
-- =============================================================
CREATE TABLE question_option
(
    id            BIGSERIAL PRIMARY KEY,
    question_id   BIGINT    NOT NULL,
    option_number SMALLINT  NOT NULL,
    content       TEXT      NOT NULL,

    CONSTRAINT chk_question_option_number
        CHECK (option_number BETWEEN 1 AND 5)
);

COMMENT ON TABLE question_option IS '문제 선택지';


-- =============================================================
-- TABLE: exam
-- =============================================================
CREATE TABLE exam
(
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL,
    topic_id        BIGINT,
    document_id     BIGINT,
    source_type     VARCHAR(30) NOT NULL,
    total_questions SMALLINT    NOT NULL,
    time_limit      SMALLINT    NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    started_at      TIMESTAMPTZ,
    expired_at      TIMESTAMPTZ,
    submitted_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_exam_source_type
        CHECK (source_type IN ('PUBLIC_TOPIC', 'PRIVATE_DOCUMENT')),
    CONSTRAINT chk_exam_status
        CHECK (status IN ('CREATED', 'IN_PROGRESS', 'SUBMITTED')),
    CONSTRAINT chk_exam_total_questions
        CHECK (total_questions > 0),
    CONSTRAINT chk_exam_time_limit
        CHECK (time_limit > 0)
);

COMMENT ON TABLE exam IS '시험';
COMMENT ON COLUMN exam.source_type IS '시험 문제 출처 유형';


-- =============================================================
-- TABLE: exam_question
-- =============================================================
CREATE TABLE exam_question
(
    id          BIGSERIAL PRIMARY KEY,
    exam_id     BIGINT    NOT NULL,
    question_id BIGINT    NOT NULL,
    order_num   SMALLINT  NOT NULL,
    my_answer   TEXT,
    is_correct  BOOLEAN
);

COMMENT ON TABLE exam_question IS '시험 문제 및 사용자 답안';


-- =============================================================
-- INDEX
-- =============================================================

CREATE INDEX idx_user_role
    ON "user" (role);

CREATE INDEX idx_topic_active
    ON topic (active);

CREATE INDEX idx_document_user_id_created_at
    ON document (user_id, created_at DESC);

CREATE INDEX idx_document_status
    ON document (status);

CREATE INDEX idx_document_chunk_document_id
    ON document_chunk (document_id);

CREATE INDEX idx_document_chunk_embedding_hnsw
    ON document_chunk USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

CREATE INDEX idx_question_owner_user_id
    ON question (owner_user_id);

CREATE INDEX idx_question_topic_id
    ON question (topic_id);

CREATE INDEX idx_question_document_id
    ON question (document_id);

CREATE INDEX idx_question_scope_topic_difficulty
    ON question (scope, topic_id, difficulty);

CREATE INDEX idx_question_scope_document_difficulty
    ON question (scope, document_id, difficulty);

CREATE INDEX idx_question_option_question_id
    ON question_option (question_id);

CREATE INDEX idx_exam_user_id_created_at
    ON exam (user_id, created_at DESC);

CREATE INDEX idx_exam_expired_at_in_progress
    ON exam (expired_at)
    WHERE status = 'IN_PROGRESS';

CREATE INDEX idx_exam_question_exam_id_order
    ON exam_question (exam_id, order_num);

CREATE INDEX idx_exam_question_question_id
    ON exam_question (question_id);
