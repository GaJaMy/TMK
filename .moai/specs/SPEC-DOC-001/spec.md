---
id: SPEC-DOC-001
version: "1.0.0"
status: approved
created: "2026-03-26"
updated: "2026-03-26"
author: MoAI
priority: HIGH
issue_number: 0
---

# SPEC-DOC-001: Document Registration and Question Generation

## 1. Overview

문서 등록 API를 구현하고, 등록된 문서를 처리하여 문제를 자동 생성하는 파이프라인을 구현한다.
OpenAI 실제 연동은 제외하고, Port 인터페이스와 Stub 구현체로 로직 흐름만 구성한다.

## 2. Requirements

### 문서 등록
- POST `/internal/v1/documents` → 문서를 PENDING 상태로 등록 후 즉시 응답
- 등록 후 비동기로 처리 파이프라인 실행

### 문서 처리 파이프라인 (비동기)
1. 상태를 PROCESSING으로 업데이트
2. source에서 텍스트 추출 (TextExtractionPort - stub)
3. 텍스트 청킹 (500자 단위, 50자 오버랩)
4. 각 청크에 대해 임베딩 생성 (EmbeddingPort - stub, 1536차원 zero vector)
5. DocumentChunk 저장
6. 문제 생성 (QuestionGenerationPort - stub, 미리 정의된 샘플 문제 반환)
7. Question + QuestionOption 저장
8. 상태를 COMPLETED로 업데이트
9. 실패 시 FAILED로 업데이트

### 문서 상태 조회
- GET `/internal/v1/documents/{documentId}/status` → Document + chunk/question 수 반환

### 문제 목록 조회 (기존 stub → 구현)
- GET `/api/v1/questions?type=&difficulty=&page=0&size=10` → 페이징된 문제 목록

### 문제 상세 조회 (기존 stub → 구현)
- GET `/api/v1/questions/{questionId}` → 문제 상세 (options 포함)

## 3. New Components

### New Port Interfaces (tmk-core/port/out)

**TextExtractionPort**
```java
String extract(String source);
```

**EmbeddingPort**
```java
float[] embed(String text);
int getDimension();
```

**QuestionGenerationPort**
```java
List<GeneratedQuestion> generateQuestions(Long documentId, String context);
```

### New Value Object (tmk-core/document/vo)

**GeneratedQuestion** (record)
```java
record GeneratedQuestion(
    String content,
    QuestionType type,
    Difficulty difficulty,
    String answer,
    String explanation,
    List<String> options  // only for MULTIPLE_CHOICE (5 items)
)
```

### New Service (tmk-core/document/service)

**DocumentProcessingService**
- Orchestrates the full pipeline
- `process(Long documentId)` - full synchronous pipeline execution
- Injected ports: DocumentPort, DocumentChunkPort, QuestionPort, TextExtractionPort, EmbeddingPort, QuestionGenerationPort

### New Stub Adapters (tmk-api/adapter/out/ai)

**StubTextExtractionAdapter** implements TextExtractionPort
- Returns dummy text: "This is sample extracted text from document: {source}\n\nSection 1...\n\nSection 2..."

**StubEmbeddingAdapter** implements EmbeddingPort
- Returns float[1536] with all zeros

**StubQuestionGenerationAdapter** implements QuestionGenerationPort
- Returns 3 hardcoded questions:
  1. MULTIPLE_CHOICE / EASY
  2. SHORT_ANSWER / MEDIUM
  3. TRUE_FALSE / HARD

### Async Executor (tmk-api)

**AsyncDocumentProcessor** (@Component)
```java
@Async
public void processAsync(Long documentId) {
    documentProcessingService.process(documentId);
}
```

## 4. Port Additions

**DocumentChunkPort** - add:
```java
long countByDocumentId(Long documentId);
```

**QuestionPort** - add:
```java
List<Question> findByFilters(QuestionType type, Difficulty difficulty, int offset, int limit);
long countByFilters(QuestionType type, Difficulty difficulty);
long countByDocumentId(Long documentId);
```

## 5. Updated Services

**RegisterDocumentService**
```java
@Transactional
public Document register(String title, String source) {
    Document document = Document.builder()
        .title(title)
        .source(source)
        .status(DocumentStatus.PENDING)
        .createdAt(OffsetDateTime.now())
        .build();
    return documentPort.save(document);
}
```

**GetDocumentStatusService** - needs DocumentChunkPort + QuestionPort injected
```java
public DocumentStatusInfo getStatus(Long documentId) {
    Document document = documentPort.findById(documentId)
        .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
    long chunkCount = documentChunkPort.countByDocumentId(documentId);
    long questionCount = questionPort.countByDocumentId(documentId);
    return new DocumentStatusInfo(document, chunkCount, questionCount);
}
```

(DocumentStatusInfo is a simple record in the service package or core package)

**GetQuestionListService**
```java
public List<Question> getList(QuestionType type, Difficulty difficulty, int page, int size) {
    int offset = page * size;
    return questionPort.findByFilters(type, difficulty, offset, size);
}

public long count(QuestionType type, Difficulty difficulty) {
    return questionPort.countByFilters(type, difficulty);
}
```

**GetQuestionDetailService**
```java
public Question getDetail(Long questionId) {
    return questionPort.findById(questionId)
        .orElseThrow(() -> new BusinessException(ErrorCode.QUESTION_NOT_FOUND));
}
```

## 6. Use Case Updates

**DocumentUseCase**
```java
public RegisterDocumentResult register(String title, String source) {
    Document document = registerDocumentService.register(title, source);
    asyncDocumentProcessor.processAsync(document.getId());
    return new RegisterDocumentResult(document.getId(), document.getTitle(), document.getStatus().name(), document.getCreatedAt());
}

public DocumentStatusResult getStatus(Long documentId) {
    DocumentStatusInfo info = getDocumentStatusService.getStatus(documentId);
    return new DocumentStatusResult(
        info.document().getId(), info.document().getTitle(),
        info.document().getStatus().name(), info.chunkCount(),
        info.questionCount(), info.document().getCreatedAt()
    );
}
```

**QuestionUseCase**
```java
public QuestionListResult getList(String type, String difficulty, int page, int size) {
    QuestionType questionType = type != null ? QuestionType.valueOf(type) : null;
    Difficulty diff = difficulty != null ? Difficulty.valueOf(difficulty) : null;
    List<Question> questions = getQuestionListService.getList(questionType, diff, page, size);
    long total = getQuestionListService.count(questionType, diff);
    int totalPages = (int) Math.ceil((double) total / size);
    List<QuestionSummary> summaries = questions.stream()
        .map(q -> new QuestionSummary(q.getId(), q.getContent(), q.getType().name(), q.getDifficulty().name()))
        .toList();
    return new QuestionListResult(summaries, page, size, total, totalPages);
}

public QuestionDetailResult getDetail(Long questionId) {
    Question question = getQuestionDetailService.getDetail(questionId);
    List<OptionResult> options = question.getOptions().stream()
        .map(o -> new OptionResult(o.getOptionNumber().intValue(), o.getContent()))
        .toList();
    return new QuestionDetailResult(question.getId(), question.getContent(),
        question.getType().name(), question.getDifficulty().name(), options,
        question.getAnswer(), question.getExplanation());
}
```

## 7. Repository Updates

**DocumentChunkRepository** - add:
```java
long countByDocumentId(Long documentId);
```

**QuestionRepository** - add:
```java
@Query("SELECT q FROM Question q WHERE (:type IS NULL OR q.type = :type) AND (:difficulty IS NULL OR q.difficulty = :difficulty) ORDER BY q.id DESC")
List<Question> findByFilters(@Param("type") QuestionType type, @Param("difficulty") Difficulty difficulty, Pageable pageable);

long countByTypeAndDifficulty(QuestionType type, Difficulty difficulty);
long countByDocumentId(Long documentId);
```

Note: For `findByFilters` with null type/difficulty, use JPQL with IS NULL check.
For `countByFilters`, handle null enums in service layer (call countByTypeAndDifficulty only when both non-null, else use countAll variants).

## 8. Error Codes Addition

Add to ErrorCode enum:
```java
DOCUMENT_PROCESSING_FAILED("DOCUMENT_002", "문서 처리 중 오류가 발생했습니다.", 500),
```

## 9. Config Updates

Add `@EnableAsync` to a Spring configuration class in tmk-api.

## 10. DocumentChunk entity note

`DocumentChunk.embedding` is stored as String with `columnDefinition = "vector(1536)"`.
Convert float[] to pgvector string format: `"[0.1,0.2,...]"`
