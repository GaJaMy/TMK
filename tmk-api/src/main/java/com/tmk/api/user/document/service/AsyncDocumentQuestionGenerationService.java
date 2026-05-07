package com.tmk.api.user.document.service;

import com.tmk.api.monitoring.event.PrivateQuestionsGeneratedEvent;
import com.tmk.api.question.support.QuestionAnswerSupport;
import com.tmk.api.question.support.QuestionAnswerSupport.QuestionOptionCandidate;
import com.tmk.api.user.document.result.DocumentStatusResult;
import com.tmk.core.document.entity.Document;
import com.tmk.core.document.entity.DocumentChunk;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.ai.DocumentReaderPort;
import com.tmk.core.port.out.ai.EmbeddingPort;
import com.tmk.core.port.out.ai.PrivateQuestionGenerationPort;
import com.tmk.core.port.out.persistence.DocumentChunkPort;
import com.tmk.core.port.out.persistence.DocumentPort;
import com.tmk.core.port.out.persistence.PrivateQuestionPort;
import com.tmk.core.port.out.persistence.UserAccountPort;
import com.tmk.core.port.out.storage.FileStoragePort;
import com.tmk.core.question.entity.PrivateQuestion;
import com.tmk.core.question.entity.PrivateQuestionOption;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PrivateQuestionDraft;
import com.tmk.core.user.entity.UserAccount;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncDocumentQuestionGenerationService {

    private static final int MAX_CHUNK_LENGTH = 1200;

    private final DocumentPort documentPort;
    private final DocumentChunkPort documentChunkPort;
    private final PrivateQuestionPort privateQuestionPort;
    private final UserAccountPort userAccountPort;
    private final DocumentReaderPort documentReaderPort;
    private final EmbeddingPort embeddingPort;
    private final PrivateQuestionGenerationPort privateQuestionGenerationPort;
    private final FileStoragePort fileStoragePort;
    private final DocumentSseService documentSseService;
    private final PlatformTransactionManager transactionManager;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Async
    public void processDocumentAsync(Long documentId) {
        try {
            processDocument(documentId);
        } catch (Exception e) {
            log.error("Async document question generation failed: documentId={}", documentId, e);
            markDocumentFailed(documentId);
        }
    }

    private void processDocument(Long documentId) {
        Document document = documentPort.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
        UserAccount userAccount = userAccountPort.findById(document.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        String rawText = readDocument(document);
        List<String> chunkContents = splitIntoChunks(rawText);
        List<float[]> embeddings = embeddingPort.embed(chunkContents);
        List<PrivateQuestionDraft> generatedDrafts = privateQuestionGenerationPort.generateQuestions(
                userAccount.getCountryCode(),
                document.getTitle(),
                chunkContents
        );
        List<PrivateQuestionDraft> drafts = sanitizeDrafts(generatedDrafts);

        if (drafts.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<DocumentChunk> documentChunks = createDocumentChunks(document, chunkContents, embeddings, now);
        List<PrivateQuestion> privateQuestions = createPrivateQuestions(document, userAccount, drafts, now);

        StatusNotification completedNotification = completeDocument(documentId, documentChunks, privateQuestions);
        documentSseService.publish(completedNotification.userId(), completedNotification.status());
        deleteSourceFile(document.getSourceReference());
    }

    private String readDocument(Document document) {
        return switch (document.getSourceType()) {
            case PDF_UPLOAD -> documentReaderPort.readPdf(document.getSourceReference());
            case MD_UPLOAD -> documentReaderPort.readMarkdown(document.getSourceReference());
        };
    }

    private List<String> splitIntoChunks(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }

        String[] paragraphs = rawText.split("\\R\\R+");
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            String normalized = paragraph.trim();
            if (!StringUtils.hasText(normalized)) {
                continue;
            }

            if (!current.isEmpty() && current.length() + normalized.length() + 2 > MAX_CHUNK_LENGTH) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }

            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(normalized);
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }

        if (chunks.isEmpty()) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }
        return chunks;
    }

    private List<DocumentChunk> createDocumentChunks(
            Document document,
            List<String> chunkContents,
            List<float[]> embeddings,
            OffsetDateTime now
    ) {
        if (chunkContents.size() != embeddings.size()) {
            throw new BusinessException(ErrorCode.DOCUMENT_PROCESSING_FAILED);
        }

        List<DocumentChunk> documentChunks = new ArrayList<>();
        for (int index = 0; index < chunkContents.size(); index++) {
            documentChunks.add(DocumentChunk.create(
                    document,
                    (short) index,
                    chunkContents.get(index),
                    embeddings.get(index),
                    now
            ));
        }
        return documentChunks;
    }

    private List<PrivateQuestion> createPrivateQuestions(
            Document document,
            UserAccount userAccount,
            List<PrivateQuestionDraft> drafts,
            OffsetDateTime now
    ) {
        return drafts.stream()
                .map(draft -> createPrivateQuestion(document, userAccount, draft, now))
                .toList();
    }

    List<PrivateQuestionDraft> sanitizeDrafts(List<PrivateQuestionDraft> drafts) {
        if (drafts == null || drafts.isEmpty()) {
            return List.of();
        }

        List<PrivateQuestionDraft> sanitizedDrafts = new ArrayList<>();
        for (PrivateQuestionDraft draft : drafts) {
            if (!isValidDraft(draft)) {
                continue;
            }

            List<String> normalizedOptions = normalizeOptions(draft.type(), draft.options());
            if (normalizedOptions == null) {
                continue;
            }

            sanitizedDrafts.add(new PrivateQuestionDraft(
                    draft.content().trim(),
                    draft.type(),
                    draft.difficulty(),
                    draft.answer().trim(),
                    draft.explanation().trim(),
                    normalizedOptions
            ));
        }

        int droppedCount = drafts.size() - sanitizedDrafts.size();
        if (droppedCount > 0) {
            log.warn("Dropped invalid generated drafts: dropped={}, total={}", droppedCount, drafts.size());
        }
        return sanitizedDrafts;
    }

    private boolean isValidDraft(PrivateQuestionDraft draft) {
        return draft != null
                && draft.type() != null
                && draft.difficulty() != null
                && StringUtils.hasText(draft.content())
                && StringUtils.hasText(draft.answer())
                && StringUtils.hasText(draft.explanation());
    }

    private List<String> normalizeOptions(QuestionType questionType, List<String> options) {
        List<String> normalizedOptions = normalizeTextOptions(options);

        if (questionType == QuestionType.SHORT_ANSWER) {
            return List.of();
        }
        if (questionType == QuestionType.MULTIPLE_CHOICE) {
            if (normalizedOptions.size() < 5) {
                return null;
            }
            return List.copyOf(normalizedOptions.subList(0, 5));
        }
        if (questionType == QuestionType.TRUE_FALSE) {
            if (normalizedOptions.size() < 2) {
                return null;
            }
            return List.copyOf(normalizedOptions.subList(0, 2));
        }
        return null;
    }

    private List<String> normalizeTextOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        List<String> normalizedOptions = new ArrayList<>();
        for (String option : options) {
            if (StringUtils.hasText(option)) {
                normalizedOptions.add(option.trim());
            }
        }
        return normalizedOptions;
    }

    private List<PrivateQuestionOption> createOptions(List<String> options) {
        if (options == null || options.isEmpty()) {
            return List.of();
        }

        List<PrivateQuestionOption> questionOptions = new ArrayList<>();
        for (int index = 0; index < options.size(); index++) {
            questionOptions.add(PrivateQuestionOption.create((short) (index + 1), options.get(index)));
        }
        return questionOptions;
    }

    private PrivateQuestion createPrivateQuestion(
            Document document,
            UserAccount userAccount,
            PrivateQuestionDraft draft,
            OffsetDateTime now
    ) {
        List<PrivateQuestionOption> questionOptions = createOptions(draft.options());
        List<QuestionOptionCandidate> candidates = questionOptions.stream()
                .map(option -> new QuestionOptionCandidate(option.getOptionNumber(), option.getContent()))
                .toList();
        String normalizedAnswer = QuestionAnswerSupport.normalizeStoredAnswer(draft.type(), draft.answer(), candidates);
        return PrivateQuestion.create(
                userAccount.getId(),
                document.getId(),
                draft.content(),
                draft.type(),
                draft.difficulty(),
                normalizedAnswer,
                draft.explanation(),
                userAccount.getCountryCode(),
                questionOptions,
                now
        );
    }

    private StatusNotification completeDocument(
            Long documentId,
            List<DocumentChunk> documentChunks,
            List<PrivateQuestion> privateQuestions
    ) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        return transactionTemplate.execute(status -> {
            Document targetDocument = documentPort.findById(documentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));

            documentChunkPort.deleteAllByDocumentId(documentId);
            privateQuestionPort.deleteAllByDocumentId(documentId);
            documentChunkPort.saveAll(documentChunks);
            privateQuestionPort.saveAll(privateQuestions);
            targetDocument.complete(privateQuestions.size(), OffsetDateTime.now());
            documentPort.save(targetDocument);
            applicationEventPublisher.publishEvent(
                    new PrivateQuestionsGeneratedEvent(targetDocument.getUserId(), privateQuestions.size())
            );
            return new StatusNotification(targetDocument.getUserId(), DocumentStatusResult.from(targetDocument));
        });
    }

    private void markDocumentFailed(Long documentId) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
        StatusNotification failedNotification = transactionTemplate.execute(status -> {
            Document targetDocument = documentPort.findById(documentId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.DOCUMENT_NOT_FOUND));
            targetDocument.fail(OffsetDateTime.now());
            documentPort.save(targetDocument);
            return new StatusNotification(targetDocument.getUserId(), DocumentStatusResult.from(targetDocument));
        });
        if (failedNotification != null) {
            documentSseService.publish(failedNotification.userId(), failedNotification.status());
        }
    }

    private void deleteSourceFile(String sourceReference) {
        try {
            fileStoragePort.delete(sourceReference);
        } catch (Exception e) {
            log.error("Failed to delete source file after document completion: sourceReference={}", sourceReference, e);
        }
    }

    private record StatusNotification(Long userId, DocumentStatusResult status) {
    }
}
