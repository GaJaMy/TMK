-- =============================================================
-- TMK (Test My Knowledge) DDL
-- DB      : PostgreSQL
-- Version : v1.0.0
-- Date    : 2026-03-10
-- Note    : 외래키 제약조건 미적용 (애플리케이션 레벨에서 무결성 관리)
-- =============================================================

-- pgvector 확장 활성화 (벡터 타입 및 HNSW 인덱스 사용을 위해 필수)
CREATE EXTENSION IF NOT EXISTS vector;


-- =============================================================
-- TABLE: user
-- =============================================================
CREATE TABLE "user"
(
    id            BIGSERIAL    PRIMARY KEY,
    email         VARCHAR(255) NOT NULL,
    password      VARCHAR(255),
    provider      VARCHAR(20)  NOT NULL,
    provider_id   VARCHAR(255),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_user_email
        UNIQUE (email),
    CONSTRAINT chk_user_provider
        CHECK (provider IN ('LOCAL', 'GOOGLE', 'KAKAO', 'NAVER'))
);

COMMENT ON TABLE  "user"                IS '사용자';
COMMENT ON COLUMN "user".id             IS 'PK';
COMMENT ON COLUMN "user".email      IS '이메일 (로그인 식별자)';
COMMENT ON COLUMN "user".password   IS 'bcrypt 암호화 비밀번호. 소셜 로그인 시 NULL';
COMMENT ON COLUMN "user".provider   IS '가입 경로 (LOCAL, GOOGLE, KAKAO, NAVER)';
COMMENT ON COLUMN "user".provider_id    IS '소셜 로그인 제공자 사용자 식별자';
COMMENT ON COLUMN "user".created_at     IS '생성 일시';
COMMENT ON COLUMN "user".updated_at     IS '수정 일시';


-- =============================================================
-- TABLE: email_verification
-- =============================================================
CREATE TABLE email_verification
(
    id         BIGSERIAL    PRIMARY KEY,
    email      VARCHAR(255) NOT NULL,
    code       VARCHAR(10)  NOT NULL,
    verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    expired_at TIMESTAMPTZ  NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_email_verification_email
        UNIQUE (email)
);

COMMENT ON TABLE  email_verification            IS '이메일 인증';
COMMENT ON COLUMN email_verification.id         IS 'PK';
COMMENT ON COLUMN email_verification.email      IS '인증 대상 이메일';
COMMENT ON COLUMN email_verification.code       IS '인증 코드 (6자리)';
COMMENT ON COLUMN email_verification.verified   IS '인증 완료 여부';
COMMENT ON COLUMN email_verification.expired_at IS '인증 코드 만료 일시';
COMMENT ON COLUMN email_verification.created_at IS '생성 일시';


-- =============================================================
-- TABLE: document
-- =============================================================
CREATE TABLE document
(
    id         BIGSERIAL    PRIMARY KEY,
    title      VARCHAR(500) NOT NULL,
    source     VARCHAR(500) NOT NULL,
    status     VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_document_status
        CHECK (status IN ('PROCESSING', 'COMPLETED', 'FAILED'))
);

COMMENT ON TABLE  document            IS '문서 (문제 생성 기반 원본, 내부 API로만 등록)';
COMMENT ON COLUMN document.id         IS 'PK';
COMMENT ON COLUMN document.title      IS '문서 제목';
COMMENT ON COLUMN document.source     IS 'PDF 파일 저장 경로. 재처리 시 이 경로를 통해 PDF를 재파싱';
COMMENT ON COLUMN document.status     IS '처리 상태 (PROCESSING: 처리 중, COMPLETED: 완료, FAILED: 실패)';
COMMENT ON COLUMN document.created_at IS '생성 일시';


-- =============================================================
-- TABLE: document_chunk
-- =============================================================
CREATE TABLE document_chunk
(
    id          BIGSERIAL    PRIMARY KEY,
    document_id BIGINT       NOT NULL,   -- ref: document.id (FK 제약조건 미적용)
    chunk_index SMALLINT     NOT NULL,
    content     TEXT         NOT NULL,
    embedding   vector(1536) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE  document_chunk             IS '문서 청크 및 임베딩 벡터 (pgvector)';
COMMENT ON COLUMN document_chunk.id          IS 'PK';
COMMENT ON COLUMN document_chunk.document_id IS '원본 문서 ID (ref: document.id)';
COMMENT ON COLUMN document_chunk.chunk_index IS '문서 내 청크 순서 (0부터 시작)';
COMMENT ON COLUMN document_chunk.content     IS '청크 원문 텍스트';
COMMENT ON COLUMN document_chunk.embedding   IS 'OpenAI text-embedding-3-small 임베딩 벡터 (1536차원)';
COMMENT ON COLUMN document_chunk.created_at  IS '생성 일시';


-- =============================================================
-- TABLE: question
-- =============================================================
CREATE TABLE question
(
    id          BIGSERIAL   PRIMARY KEY,
    document_id BIGINT,                 -- ref: document.id (문서 기반 생성 문제인 경우만 값 존재)
    content     TEXT        NOT NULL,
    type        VARCHAR(30) NOT NULL,
    difficulty  VARCHAR(10) NOT NULL,
    source_type VARCHAR(40) NOT NULL,
    answer      TEXT        NOT NULL,
    explanation TEXT        NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_question_type
        CHECK (type IN ('MULTIPLE_CHOICE', 'FILL_IN_BLANK', 'IMPLEMENTATION')),
    CONSTRAINT chk_question_difficulty
        CHECK (difficulty IN ('EASY', 'NORMAL', 'HARD')),
    CONSTRAINT chk_question_source_type
        CHECK (source_type IN ('ADMIN_MANUAL', 'PUBLIC_DOCUMENT_GENERATED', 'PRIVATE_DOCUMENT_GENERATED'))
);

COMMENT ON TABLE  question             IS '문제';
COMMENT ON COLUMN question.id          IS 'PK';
COMMENT ON COLUMN question.document_id IS '기반 문서 ID. 관리자 수동 등록 문제는 NULL';
COMMENT ON COLUMN question.content     IS '문제 내용';
COMMENT ON COLUMN question.type        IS '문제 유형 (MULTIPLE_CHOICE, FILL_IN_BLANK, IMPLEMENTATION)';
COMMENT ON COLUMN question.difficulty  IS '난이도 (EASY, NORMAL, HARD)';
COMMENT ON COLUMN question.source_type IS '문제 출처 (ADMIN_MANUAL, PUBLIC_DOCUMENT_GENERATED, PRIVATE_DOCUMENT_GENERATED)';
COMMENT ON COLUMN question.answer      IS '정답';
COMMENT ON COLUMN question.explanation IS '해설';
COMMENT ON COLUMN question.created_at  IS '생성 일시';
COMMENT ON COLUMN question.updated_at  IS '수정 일시';


-- =============================================================
-- TABLE: question_option
-- =============================================================
CREATE TABLE question_option
(
    id            BIGSERIAL PRIMARY KEY,
    question_id   BIGINT    NOT NULL,   -- ref: question.id (FK 제약조건 미적용)
    option_number SMALLINT  NOT NULL,
    content       TEXT      NOT NULL,

    CONSTRAINT chk_question_option_number
        CHECK (option_number BETWEEN 1 AND 5)
);

COMMENT ON TABLE  question_option               IS '문제 선택지 (객관식 전용)';
COMMENT ON COLUMN question_option.id            IS 'PK';
COMMENT ON COLUMN question_option.question_id   IS '문제 ID (ref: question.id)';
COMMENT ON COLUMN question_option.option_number IS '선택지 번호 (1~5)';
COMMENT ON COLUMN question_option.content       IS '선택지 내용';


-- =============================================================
-- TABLE: exam
-- =============================================================
CREATE TABLE exam
(
    id              BIGSERIAL   PRIMARY KEY,
    user_id         BIGINT      NOT NULL,   -- ref: user.id (FK 제약조건 미적용)
    total_questions SMALLINT    NOT NULL,
    time_limit      SMALLINT    NOT NULL DEFAULT 30,
    status          VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    started_at      TIMESTAMPTZ NOT NULL,
    expired_at      TIMESTAMPTZ NOT NULL,
    submitted_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    CONSTRAINT chk_exam_status
        CHECK (status IN ('IN_PROGRESS', 'SUBMITTED')),
    CONSTRAINT chk_exam_total_questions
        CHECK (total_questions >= 10),
    CONSTRAINT chk_exam_time_limit
        CHECK (time_limit > 0)
);

COMMENT ON TABLE  exam                  IS '시험';
COMMENT ON COLUMN exam.id               IS 'PK';
COMMENT ON COLUMN exam.user_id          IS '응시 사용자 ID (ref: user.id)';
COMMENT ON COLUMN exam.total_questions  IS '총 문제 수 (최소 10)';
COMMENT ON COLUMN exam.time_limit       IS '시험 제한 시간 (분, 기본 30)';
COMMENT ON COLUMN exam.status           IS '시험 상태 (IN_PROGRESS, SUBMITTED)';
COMMENT ON COLUMN exam.started_at       IS '시험 시작 일시';
COMMENT ON COLUMN exam.expired_at       IS '시험 만료 일시';
COMMENT ON COLUMN exam.submitted_at     IS '제출 일시. 미제출 시 NULL';
COMMENT ON COLUMN exam.created_at       IS '생성 일시';


-- =============================================================
-- TABLE: exam_question
-- =============================================================
CREATE TABLE exam_question
(
    id          BIGSERIAL PRIMARY KEY,
    exam_id     BIGINT    NOT NULL,   -- ref: exam.id (FK 제약조건 미적용)
    question_id BIGINT    NOT NULL,   -- ref: question.id (FK 제약조건 미적용)
    order_num   SMALLINT  NOT NULL,
    my_answer   TEXT,
    is_correct  BOOLEAN
);

COMMENT ON TABLE  exam_question             IS '시험 문제 (시험-문제 연결 및 답안/채점)';
COMMENT ON COLUMN exam_question.id          IS 'PK';
COMMENT ON COLUMN exam_question.exam_id     IS '시험 ID (ref: exam.id)';
COMMENT ON COLUMN exam_question.question_id IS '문제 ID (ref: question.id)';
COMMENT ON COLUMN exam_question.order_num   IS '문제 출제 순서';
COMMENT ON COLUMN exam_question.my_answer   IS '사용자 제출 답안. 미응답 시 NULL';
COMMENT ON COLUMN exam_question.is_correct  IS '정답 여부. 채점 전 NULL';


-- =============================================================
-- INDEX
-- =============================================================

-- user
CREATE INDEX idx_user_provider_provider_id
    ON "user" (provider, provider_id)
    WHERE provider_id IS NOT NULL;

-- email_verification
CREATE INDEX idx_email_verification_expired_at
    ON email_verification (expired_at);

-- document
CREATE INDEX idx_document_source
    ON document (source);

CREATE INDEX idx_document_status
    ON document (status)
    WHERE status IN ('PROCESSING', 'FAILED');

-- document_chunk
CREATE INDEX idx_document_chunk_document_id
    ON document_chunk (document_id);

-- HNSW 벡터 유사도 인덱스 (코사인 유사도)
-- m: 노드당 연결 수 (높을수록 정확도↑ 메모리↑)
-- ef_construction: 인덱스 빌드 시 탐색 범위 (높을수록 품질↑ 빌드 시간↑)
CREATE INDEX idx_document_chunk_embedding_hnsw
    ON document_chunk USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);

-- question
CREATE INDEX idx_question_document_id
    ON question (document_id);

CREATE INDEX idx_question_type_difficulty
    ON question (type, difficulty);

-- question_option
CREATE INDEX idx_question_option_question_id
    ON question_option (question_id);

-- exam
CREATE INDEX idx_exam_user_id
    ON exam (user_id);

CREATE INDEX idx_exam_user_id_status
    ON exam (user_id, status);

CREATE INDEX idx_exam_expired_at_in_progress
    ON exam (expired_at)
    WHERE status = 'IN_PROGRESS';

-- exam_question
CREATE INDEX idx_exam_question_exam_id_order
    ON exam_question (exam_id, order_num);

CREATE INDEX idx_exam_question_question_id
    ON exam_question (question_id);
