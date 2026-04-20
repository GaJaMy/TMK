# SPEC-AI-001 Acceptance Criteria

## AC-1: PDF Text Extraction

**Given** a valid PDF file path as document source
**When** PdfTextExtractionAdapter.extract() is called
**Then** the text content from all pages is returned as a single string

**Given** an invalid or missing file path
**When** PdfTextExtractionAdapter.extract() is called
**Then** a BusinessException with appropriate error code is thrown

## AC-2: OpenAI Embedding Generation

**Given** a text chunk string
**When** OpenAiEmbeddingAdapter.embed() is called
**Then** a float[] array of exactly 1536 dimensions is returned

**Given** OpenAiEmbeddingAdapter.getDimension() is called
**Then** 1536 is returned

## AC-3: Question Generation

**Given** a documentId and context string
**When** OpenAiQuestionGenerationAdapter.generateQuestions() is called
**Then** a List<GeneratedQuestion> with at least 2 questions is returned
**And** each question has non-null content, type, difficulty, answer, explanation
**And** MULTIPLE_CHOICE questions have exactly 5 options
**And** SHORT_ANSWER and TRUE_FALSE questions have empty options list

## AC-4: Profile Switching

**Given** the application runs with profile "local"
**When** Spring context initializes
**Then** Stub adapters are loaded (StubTextExtractionAdapter, StubEmbeddingAdapter, StubQuestionGenerationAdapter)

**Given** the application runs without "local" or "test" profile
**When** Spring context initializes
**Then** real adapters are loaded (PdfTextExtractionAdapter, OpenAiEmbeddingAdapter, OpenAiQuestionGenerationAdapter)

## AC-5: Configuration

**Given** OPENAI_API_KEY environment variable is set
**When** OpenAiProperties is loaded
**Then** apiKey field contains the environment variable value
**And** default embedding model is "text-embedding-3-small"
**And** default chat model is "gpt-4o-mini"

## Quality Gates

- All existing tests pass (no regression)
- New adapter unit tests pass
- Build succeeds: `./gradlew build`
