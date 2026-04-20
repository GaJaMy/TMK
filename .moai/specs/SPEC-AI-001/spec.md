---
id: SPEC-AI-001
version: "1.0.0"
status: approved
created: "2026-04-20"
updated: "2026-04-20"
author: MoAI
priority: HIGH
issue_number: 0
---

# SPEC-AI-001: OpenAI Integration - Replace Stub Adapters with Real Implementations

## 1. Overview

SPEC-DOC-001에서 Stub으로 구현된 AI 어댑터 3개를 실제 OpenAI API 및 PDF 파싱 라이브러리로 교체한다.
기존 포트 인터페이스(TextExtractionPort, EmbeddingPort, QuestionGenerationPort)는 변경하지 않으며,
어댑터 계층만 교체하여 실제 문서 처리 파이프라인이 동작하도록 한다.

## 2. Requirements (EARS Format)

### REQ-AI-001: PDF Text Extraction
**When** a document is registered with a PDF file source,
**the system shall** extract text content from the PDF using Apache PDFBox,
**so that** the extracted text can be chunked and embedded for question generation.

### REQ-AI-002: OpenAI Embedding Generation
**When** a text chunk is ready for embedding,
**the system shall** call OpenAI Embeddings API with model `text-embedding-3-small`,
**so that** a 1536-dimensional vector is returned for pgvector storage.

### REQ-AI-003: OpenAI Question Generation
**When** document chunks with context are available,
**the system shall** call OpenAI Chat Completions API (configurable model, default `gpt-4o-mini`) with structured JSON output,
**so that** questions of types MULTIPLE_CHOICE, SHORT_ANSWER, and TRUE_FALSE are generated with proper format.

### REQ-AI-004: Profile-Based Adapter Switching
**When** the application runs with the `local` or `test` profile,
**the system shall** use Stub adapters (existing behavior).
**When** running without those profiles (default/prod),
**the system shall** use real OpenAI/PDF adapters.

### REQ-AI-005: Configuration Properties
**The system shall** expose OpenAI configuration via `application.yml` with:
- `openai.api-key`: API key (environment variable)
- `openai.embedding.model`: embedding model name (default: text-embedding-3-small)
- `openai.chat.model`: chat model name (default: gpt-4o-mini)
- `openai.chat.temperature`: temperature (default: 0.7)
- `openai.chat.max-tokens`: max tokens (default: 4096)

## 3. Scope

### In Scope
- PdfTextExtractionAdapter (Apache PDFBox)
- OpenAiEmbeddingAdapter (OpenAI Embeddings API)
- OpenAiQuestionGenerationAdapter (OpenAI Chat Completions API with JSON mode)
- OpenAiProperties (@ConfigurationProperties)
- Profile-based conditional bean registration
- build.gradle dependency additions
- application.yml configuration
- Unit tests for all new adapters

### Out of Scope
- DocumentProcessingService changes (already complete)
- Port interface changes
- pgvector ANN search query (separate SPEC)
- RAG retrieval pipeline (separate SPEC)
- Frontend changes

## 4. Technical Approach

### Dependencies
- `com.openai:openai-java:0.39.0` (official OpenAI Java SDK)
- `org.apache.pdfbox:pdfbox:3.0.4` (PDF text extraction)

### New Components (tmk-api/adapter/out/ai/)
1. **OpenAiProperties** - @ConfigurationProperties("openai")
2. **OpenAiEmbeddingAdapter** - implements EmbeddingPort
3. **OpenAiQuestionGenerationAdapter** - implements QuestionGenerationPort
4. **PdfTextExtractionAdapter** - implements TextExtractionPort

### Adapter Switching Strategy
- Stub adapters: `@Profile({"local", "test"})` annotation
- Real adapters: `@Profile("!local & !test")` or `@ConditionalOnProperty`

### Question Generation Prompt Strategy
- System prompt: Define role as educational question generator
- User prompt: Include document context and specify output JSON format
- Response format: Structured JSON with array of questions
- Each question: content, type, difficulty, answer, explanation, options
