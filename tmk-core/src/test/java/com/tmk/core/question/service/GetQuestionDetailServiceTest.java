package com.tmk.core.question.service;

import com.tmk.core.common.Topic;
import com.tmk.core.common.ContentScope;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionSourceType;
import com.tmk.core.question.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetQuestionDetailServiceTest {

    @Mock
    private QuestionPort questionPort;

    @InjectMocks
    private GetQuestionDetailService getQuestionDetailService;

    private Question buildQuestion() {
        OffsetDateTime now = OffsetDateTime.now();
        return Question.builder()
                .documentId(1L)
                .content("Test question?")
                .ownerUserId(null)
                .scope(ContentScope.PUBLIC)
                .sourceType(QuestionSourceType.PUBLIC_DOCUMENT_GENERATED)
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(Difficulty.EASY)
                .topic(Topic.SPRING)
                .answer("A")
                .explanation("Because...")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void getDetail_found_returnsQuestion() {
        // Arrange
        Long questionId = 1L;
        Question question = buildQuestion();
        when(questionPort.findById(questionId)).thenReturn(Optional.of(question));

        // Act
        Question result = getQuestionDetailService.getDetail(questionId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEqualTo("Test question?");
        assertThat(result.getType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
    }

    @Test
    void getDetail_notFound_throwsBusinessException() {
        // Arrange
        Long questionId = 99L;
        when(questionPort.findById(questionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> getQuestionDetailService.getDetail(questionId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.QUESTION_NOT_FOUND));
    }
}
