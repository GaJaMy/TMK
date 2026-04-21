package com.tmk.api.adapter.out.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.ResponseFormatJsonObject;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.tmk.api.config.OpenAiProperties;
import com.tmk.core.document.vo.GeneratedQuestion;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.QuestionGenerationPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@Profile("!local & !test")
@RequiredArgsConstructor
public class OpenAiQuestionGenerationAdapter implements QuestionGenerationPort {

    private final OpenAIClient openAIClient;
    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final int TARGET_QUESTION_COUNT = 12;

    @Override
    public List<GeneratedQuestion> generateQuestions(Long documentId, String topic, String context) {
        try {
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(topic, context);

            ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
                    .model(ChatModel.of(openAiProperties.getChat().getModel()))
                    .maxCompletionTokens(openAiProperties.getChat().getMaxTokens())
                    .temperature(openAiProperties.getChat().getTemperature())
                    .responseFormat(ChatCompletionCreateParams.ResponseFormat.ofJsonObject(
                            ResponseFormatJsonObject.builder().build()))
                    .addDeveloperMessage(systemPrompt)
                    .addUserMessage(userPrompt)
                    .build();

            ChatCompletion completion = openAIClient.chat().completions().create(params);

            String jsonContent = completion.choices().stream()
                    .flatMap(choice -> choice.message().content().stream())
                    .findFirst()
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED));

            QuestionList questionList = objectMapper.readValue(jsonContent, QuestionList.class);

            List<GeneratedQuestion> result = questionList.questions.stream()
                    .map(this::toGeneratedQuestion)
                    .toList();

            log.info("Generated {} questions for documentId={}", result.size(), documentId);
            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate questions for documentId={}", documentId, e);
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    private GeneratedQuestion toGeneratedQuestion(QuestionItem item) {
        QuestionType type = QuestionType.valueOf(item.type.toUpperCase());
        Difficulty difficulty = Difficulty.valueOf(item.difficulty.toUpperCase());
        List<String> options = (item.options != null) ? item.options : List.of();

        return new GeneratedQuestion(
                item.content,
                type,
                difficulty,
                item.answer,
                item.explanation,
                options
        );
    }

    private String buildSystemPrompt() {
        return """
                You are an expert educational question generator. Your task is to create high-quality exam questions from the given document content.
                You MUST respond with valid JSON only.

                Rules:
                1. Generate exactly %d questions with a mix of types and difficulties.
                2. Question types: MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE
                3. Difficulty levels: EASY, MEDIUM, HARD
                4. For MULTIPLE_CHOICE: provide exactly 5 options. The answer must be the option number (1-5).
                5. For TRUE_FALSE: the answer must be "TRUE" or "FALSE".
                6. For SHORT_ANSWER: provide a concise but complete answer.
                7. Always include a clear explanation for each question.
                8. Questions should test understanding, not just memorization.
                9. Include at least 4 EASY, 4 MEDIUM, and 4 HARD questions.
                10. Cover the document broadly. Do not generate duplicate or near-duplicate questions.
                11. If the document is short, still produce %d valid questions by varying angle, depth, and format.

                Response format:
                {
                  "questions": [
                    {
                      "content": "question text",
                      "type": "MULTIPLE_CHOICE",
                      "difficulty": "EASY",
                      "answer": "2",
                      "explanation": "explanation text",
                      "options": ["option1", "option2", "option3", "option4", "option5"]
                    }
                  ]
                }
                """.formatted(TARGET_QUESTION_COUNT, TARGET_QUESTION_COUNT);
    }

    private String buildUserPrompt(String topic, String context) {
        return """
                Topic:
                %s

                Based on the following document content, generate exam questions.

                Document Content:
                ---
                %s
                ---

                Generate exactly %d questions covering the key concepts in this document.
                """.formatted(topic, context, TARGET_QUESTION_COUNT);
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QuestionList {
        @JsonProperty("questions")
        public List<QuestionItem> questions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class QuestionItem {
        @JsonProperty("content")
        public String content;

        @JsonProperty("type")
        public String type;

        @JsonProperty("difficulty")
        public String difficulty;

        @JsonProperty("answer")
        public String answer;

        @JsonProperty("explanation")
        public String explanation;

        @JsonProperty("options")
        public List<String> options;
    }
}
