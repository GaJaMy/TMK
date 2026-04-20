# SPEC-AI-001 Implementation Plan

## Task Decomposition

### Task 1: Add Dependencies
- Add `com.openai:openai-java:0.39.0` to tmk-api/build.gradle
- Add `org.apache.pdfbox:pdfbox:3.0.4` to tmk-api/build.gradle

### Task 2: OpenAI Configuration
- Create `OpenAiProperties.java` with @ConfigurationProperties("openai")
- Add openai configuration section to application.yml
- Use environment variable for API key: `${OPENAI_API_KEY}`

### Task 3: PdfTextExtractionAdapter
- Implement TextExtractionPort using PDFBox 3.x
- source parameter is the file path to the PDF
- Extract all text pages and concatenate
- Handle IOExceptions gracefully

### Task 4: OpenAiEmbeddingAdapter
- Implement EmbeddingPort using OpenAI Java SDK
- Use text-embedding-3-small model (1536 dimensions)
- Convert response to float[] array

### Task 5: OpenAiQuestionGenerationAdapter
- Implement QuestionGenerationPort using OpenAI Chat Completions
- Design system prompt for educational question generation
- Use JSON response format for structured output
- Parse JSON response into List<GeneratedQuestion>
- Generate mix of MULTIPLE_CHOICE (5 options), SHORT_ANSWER, TRUE_FALSE
- Include difficulty distribution: EASY, MEDIUM, HARD

### Task 6: Profile-Based Switching
- Add @Profile({"local", "test"}) to all 3 Stub adapters
- New adapters activated by default (no profile annotation needed)
- Or use @ConditionalOnProperty for explicit control

### Task 7: Unit Tests
- Test OpenAiProperties binding
- Test PdfTextExtractionAdapter with sample PDF
- Test adapter JSON parsing logic (mock OpenAI responses)

## Risk Analysis

| Risk | Impact | Mitigation |
|------|--------|------------|
| OpenAI API rate limits | Medium | Implement retry with exponential backoff |
| PDF parsing failures | Low | Graceful error handling, document status → FAILED |
| JSON parsing errors from LLM | Medium | Validate JSON structure, fallback to retry |
| API key exposure | High | Environment variable only, never in code |

## Dependencies
- Existing: TextExtractionPort, EmbeddingPort, QuestionGenerationPort (tmk-core)
- Existing: DocumentProcessingService pipeline (tmk-core)
- Existing: Stub adapters (will be conditionally loaded)
