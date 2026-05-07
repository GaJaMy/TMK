package com.tmk.api.question.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.tmk.api.question.support.QuestionAnswerSupport.QuestionOptionCandidate;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.question.entity.QuestionType;
import java.util.List;
import org.junit.jupiter.api.Test;

class QuestionAnswerSupportTest {

    @Test
    void normalizeStoredAnswerReturnsOptionNumberForMultipleChoiceTextAnswer() {
        String answer = QuestionAnswerSupport.normalizeStoredAnswer(
                QuestionType.MULTIPLE_CHOICE,
                "Bean",
                List.of(
                        new QuestionOptionCandidate((short) 1, "Bean"),
                        new QuestionOptionCandidate((short) 2, "Service")
                )
        );

        assertThat(answer).isEqualTo("1");
    }

    @Test
    void normalizeStoredAnswerReturnsOptionNumberForTrueFalseLabelAnswer() {
        String answer = QuestionAnswerSupport.normalizeStoredAnswer(
                QuestionType.TRUE_FALSE,
                "거짓",
                List.of(
                        new QuestionOptionCandidate((short) 1, "참"),
                        new QuestionOptionCandidate((short) 2, "거짓")
                )
        );

        assertThat(answer).isEqualTo("2");
    }

    @Test
    void normalizeStoredAnswerReturnsTrimmedTextForShortAnswer() {
        String answer = QuestionAnswerSupport.normalizeStoredAnswer(
                QuestionType.SHORT_ANSWER,
                "  Bean  ",
                List.of()
        );

        assertThat(answer).isEqualTo("Bean");
    }

    @Test
    void normalizeStoredAnswerThrowsWhenChoiceAnswerDoesNotMatchOption() {
        assertThatThrownBy(() -> QuestionAnswerSupport.normalizeStoredAnswer(
                QuestionType.MULTIPLE_CHOICE,
                "Repository",
                List.of(
                        new QuestionOptionCandidate((short) 1, "Bean"),
                        new QuestionOptionCandidate((short) 2, "Service")
                )
        ))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void toDisplayAnswerReturnsOptionContentForStoredOptionNumber() {
        String answer = QuestionAnswerSupport.toDisplayAnswer(
                QuestionType.MULTIPLE_CHOICE,
                "1",
                List.of(
                        new QuestionOptionCandidate((short) 1, "Bean"),
                        new QuestionOptionCandidate((short) 2, "Service")
                )
        );

        assertThat(answer).isEqualTo("Bean");
    }

    @Test
    void isCorrectAnswerComparesShortAnswerCaseInsensitively() {
        boolean correct = QuestionAnswerSupport.isCorrectAnswer(QuestionType.SHORT_ANSWER, "bean", "Bean");

        assertThat(correct).isTrue();
    }

    @Test
    void isCorrectAnswerComparesChoiceAnswerByNormalizedStoredValue() {
        boolean correct = QuestionAnswerSupport.isCorrectAnswer(QuestionType.MULTIPLE_CHOICE, "1", "1");

        assertThat(correct).isTrue();
    }
}
