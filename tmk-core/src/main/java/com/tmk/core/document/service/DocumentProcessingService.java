package com.tmk.core.document.service;

import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.document.entity.DocumentStatus;
import com.tmk.core.document.vo.GeneratedQuestion;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.DocumentChunkPort;
import com.tmk.core.port.out.DocumentPort;
import com.tmk.core.port.out.EmbeddingPort;
import com.tmk.core.port.out.QuestionGenerationPort;
import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.port.out.TextExtractionPort;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionOption;
import com.tmk.core.question.entity.QuestionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentProcessingService {

    private static final int CHUNK_SIZE = 500;
    private static final int CHUNK_OVERLAP = 50;

    private final DocumentPort documentPort;
    private final DocumentChunkPort documentChunkPort;
    private final QuestionPort questionPort;
    private final TextExtractionPort textExtractionPort;
    private final EmbeddingPort embeddingPort;
    private final QuestionGenerationPort questionGenerationPort;

    @Transactional
    public void process(Long documentId) {
        Document document = documentPort.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

        try {
            document.updateStatus(DocumentStatus.PROCESSING);
            documentPort.save(document);

            String text = textExtractionPort.extract(document.getSource());
            List<String> chunks = chunkText(text);

            List<DocumentChunk> documentChunks = new ArrayList<>();
            for (int i = 0; i < chunks.size(); i++) {
                float[] embedding = embeddingPort.embed(chunks.get(i));
                documentChunks.add(DocumentChunk.builder()
                        .documentId(documentId)
                        .chunkIndex((short) i)
                        .content(chunks.get(i))
                        .embedding(embeddingToString(embedding))
                        .createdAt(OffsetDateTime.now())
                        .build());
            }
            documentChunkPort.saveAll(documentChunks);

            String context = String.join("\n\n", chunks);
            List<GeneratedQuestion> generatedQuestions = questionGenerationPort.generateQuestions(documentId, context);

            for (GeneratedQuestion gq : generatedQuestions) {
                OffsetDateTime now = OffsetDateTime.now();
                Question question = Question.builder()
                        .documentId(documentId)
                        .content(gq.content())
                        .type(gq.type())
                        .difficulty(gq.difficulty())
                        .answer(gq.answer())
                        .explanation(gq.explanation())
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                Question savedQuestion = questionPort.save(question);

                if (gq.type() == QuestionType.MULTIPLE_CHOICE && gq.options() != null) {
                    for (int i = 0; i < gq.options().size(); i++) {
                        QuestionOption option = QuestionOption.builder()
                                .question(savedQuestion)
                                .optionNumber((short) (i + 1))
                                .content(gq.options().get(i))
                                .build();
                        savedQuestion.getOptions().add(option);
                    }
                    questionPort.save(savedQuestion);
                }
            }

            document.updateStatus(DocumentStatus.COMPLETED);
            documentPort.save(document);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            document.updateStatus(DocumentStatus.FAILED);
            documentPort.save(document);
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
    }

    private List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) {
            return chunks;
        }
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            chunks.add(text.substring(start, end));
            if (end == text.length()) break;
            start = end - CHUNK_OVERLAP;
        }
        return chunks;
    }

    private String embeddingToString(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
