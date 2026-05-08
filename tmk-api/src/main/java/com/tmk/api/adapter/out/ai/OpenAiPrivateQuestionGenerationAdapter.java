package com.tmk.api.adapter.out.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tmk.api.config.OpenAiProperties;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.PrivateQuestionGenerationPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PrivateQuestionDraft;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!local & !test")
@RequiredArgsConstructor
public class OpenAiPrivateQuestionGenerationAdapter implements PrivateQuestionGenerationPort {

    private final OpenAiProperties openAiProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<PrivateQuestionDraft> generateQuestions(String languageCode, String documentTitle, List<String> chunkContents) {
        try {
            String combinedChunks = String.join("\n\n---\n\n", chunkContents);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", openAiProperties.getChat().getModel());
            requestBody.put("temperature", openAiProperties.getChat().getTemperature());
            requestBody.put("max_tokens", openAiProperties.getChat().getMaxTokens());
            requestBody.put("response_format", Map.of("type", "json_object"));
            requestBody.put("messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", """
                                    You generate study questions from a document.
                                    Return JSON only with this exact shape:
                                    {
                                      "questions": [
                                        {
                                          "content": "...",
                                          "type": "MULTIPLE_CHOICE|SHORT_ANSWER|TRUE_FALSE",
                                          "difficulty": "EASY|NORMAL|HARD",
                                          "answer": "...",
                                          "explanation": "...",
                                          "options": ["...", "..."]
                                        }
                                      ]
                                    }
                                    Hard rules:
                                    - Use the language implied by the country code.
                                    - Generate only questions grounded in the supplied document.
                                    - MULTIPLE_CHOICE must have exactly 5 options.
                                    - TRUE_FALSE must have exactly 2 options.
                                    - SHORT_ANSWER must have no options.
                                    - For SHORT_ANSWER, always return "options": [].
                                    - SHORT_ANSWER must ask for exactly one specific answer, not a list, set, range, explanation, or multiple facts.
                                    - SHORT_ANSWER must be answerable with one short canonical answer such as a single term, concept name, keyword, value, or short phrase.
                                    - Do not create SHORT_ANSWER questions that require explanation, description, reasoning process, or sentence-length answers.
                                    - Do not create SHORT_ANSWER questions that ask "how many", "which ones", "what are the types", "describe", "explain", "list", or anything that can naturally produce multiple valid answers.
                                    - If a fact can be answered by more than one equally correct wording, avoid making it a SHORT_ANSWER question.
                                    - For SHORT_ANSWER, write the question so the expected answer is unambiguously one item only.
                                    - Example of a valid SHORT_ANSWER style: "Spring 컨테이너에서 관리되는 객체를 무엇이라고 하나요?"
                                    - Example of an invalid SHORT_ANSWER style: "Spring의 핵심 개념 몇 가지를 쓰세요."
                                    - Example of an invalid SHORT_ANSWER style: "빈이 무엇인지 설명하세요."
                                    - For MULTIPLE_CHOICE, always return exactly 5 non-empty options.
                                    - For TRUE_FALSE, always return exactly 2 non-empty options.
                                    - TRUE_FALSE questions must be written as a declarative statement that can be judged as true or false.
                                    - Do not write TRUE_FALSE questions in open-question form such as "what", "which", "how", "why", "what can be used", or "what is".
                                    - Do not use TRUE_FALSE when the wording naturally expects a term, concept, method, list, or descriptive answer.
                                    - If the prompt would naturally be answered with a keyword or phrase, use SHORT_ANSWER or MULTIPLE_CHOICE instead of TRUE_FALSE.
                                    - Valid TRUE_FALSE style example: "Saga 패턴은 분산 트랜잭션의 원자성을 보장하기 위해 사용될 수 있다."
                                    - Invalid TRUE_FALSE style example: "MSA 환경에서 트랜잭션의 원자성을 보장하기 위한 방법으로 무엇을 사용할 수 있는가?"
                                    - Do not return 4-option or 6-option multiple choice questions.
                                    - Do not return any question if you cannot satisfy the type/option-count rule exactly.
                                    - Exclude invalid questions instead of returning malformed questions.
                                    - Every question must have non-empty content, answer, and explanation.
                                    - The "type" value must be exactly one of MULTIPLE_CHOICE, SHORT_ANSWER, TRUE_FALSE.
                                    - The "difficulty" value must be exactly one of EASY, NORMAL, HARD.
                                    - Return JSON only. No markdown, no commentary, no code fences.
                                    - Generate as many valid questions as the document reasonably supports.
                                    - Return at least 5 questions whenever the document contains enough material.
                                    - If the document is rich enough for more than 5 questions, continue generating beyond 5 instead of stopping early.
                                    - Only return fewer than 5 questions when the document truly does not contain enough grounded material.
                                    Before returning, validate every item against these rules.
                                    """
                    ),
                    Map.of(
                            "role", "user",
                            "content", "countryCode=" + languageCode + "\ndocumentTitle=" + documentTitle + "\nchunks=\n" + combinedChunks
                    )
            ));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                    .header("Authorization", "Bearer " + openAiProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                log.error("OpenAI question generation failed: status={}, body={}", response.statusCode(), response.body());
                throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            JsonNode contentNode = objectMapper.readTree(content);
            JsonNode questionsNode = contentNode.path("questions");

            List<QuestionPayload> payloads = objectMapper.readValue(
                    objectMapper.writeValueAsString(questionsNode),
                    new TypeReference<>() {
                    }
            );

            return payloads.stream()
                    .map(payload -> new PrivateQuestionDraft(
                            payload.content(),
                            QuestionType.valueOf(payload.type()),
                            Difficulty.valueOf(payload.difficulty()),
                            payload.answer(),
                            payload.explanation(),
                            payload.options() == null ? List.of() : payload.options()
                    ))
                    .toList();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    private record QuestionPayload(
            String content,
            String type,
            String difficulty,
            String answer,
            String explanation,
            List<String> options
    ) {
    }
}
