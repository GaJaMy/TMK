package com.tmk.api.admin.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

import com.tmk.api.admin.question.result.AdminPublicQuestionDetailResult;
import com.tmk.api.admin.question.result.AdminPublicQuestionResult;
import com.tmk.core.exception.BusinessException;
import com.tmk.core.exception.ErrorCode;
import com.tmk.core.port.out.persistence.PublicQuestionPort;
import com.tmk.core.port.out.persistence.TopicPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.PublicQuestion;
import com.tmk.core.question.entity.PublicQuestionOption;
import com.tmk.core.question.entity.QuestionType;
import com.tmk.core.question.vo.PublicQuestionSearchResult;
import com.tmk.core.topic.entity.Topic;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AdminPublicQuestionServiceTest {

    @Mock
    private PublicQuestionPort publicQuestionPort;

    @Mock
    private TopicPort topicPort;

    @InjectMocks
    private AdminPublicQuestionService adminPublicQuestionService;

    @Test
    void getPublicQuestionsReturnsFilteredList() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        PublicQuestionSearchResult firstQuestion = new PublicQuestionSearchResult(
                1001L,
                "question-1",
                QuestionType.SHORT_ANSWER,
                Difficulty.EASY,
                1L,
                "Spring",
                true,
                now
        );
        PublicQuestionSearchResult secondQuestion = new PublicQuestionSearchResult(
                1002L,
                "question-2",
                QuestionType.TRUE_FALSE,
                Difficulty.HARD,
                2L,
                "Java",
                false,
                now.minusDays(1)
        );

        given(publicQuestionPort.search(1L, Difficulty.EASY, QuestionType.SHORT_ANSWER, true))
                .willReturn(List.of(firstQuestion, secondQuestion));

        List<AdminPublicQuestionResult> result =
                adminPublicQuestionService.getPublicQuestions(1L, Difficulty.EASY, QuestionType.SHORT_ANSWER, true);

        assertThat(result).containsExactly(
                new AdminPublicQuestionResult(
                        1001L,
                        "question-1",
                        QuestionType.SHORT_ANSWER,
                        Difficulty.EASY,
                        1L,
                        "Spring",
                        true,
                        now
                ),
                new AdminPublicQuestionResult(
                        1002L,
                        "question-2",
                        QuestionType.TRUE_FALSE,
                        Difficulty.HARD,
                        2L,
                        "Java",
                        false,
                        now.minusDays(1)
                )
        );
    }

    @Test
    void getPublicQuestionReturnsDetail() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        PublicQuestion question = PublicQuestion.builder()
                .id(1001L)
                .topicId(1L)
                .createdByAdminId(101L)
                .active(true)
                .content("question-1")
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(Difficulty.EASY)
                .answer("answer")
                .explanation("explanation")
                .createdAt(now)
                .updatedAt(now)
                .options(List.of(
                        PublicQuestionOption.create((short) 2, "option-2"),
                        PublicQuestionOption.create((short) 1, "option-1")
                ))
                .build();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(publicQuestionPort.findById(1001L)).willReturn(Optional.of(question));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));

        AdminPublicQuestionDetailResult result = adminPublicQuestionService.getPublicQuestion(1001L);

        assertThat(result).isEqualTo(new AdminPublicQuestionDetailResult(
                1001L,
                "question-1",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.EASY,
                1L,
                "Spring",
                "answer",
                "explanation",
                List.of("option-1", "option-2"),
                true,
                now
        ));
    }

    @Test
    void getPublicQuestionThrowsWhenQuestionDoesNotExist() {
        given(publicQuestionPort.findById(1001L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPublicQuestionService.getPublicQuestion(1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PUBLIC_QUESTION_NOT_FOUND.getMessage());
    }

    @Test
    void createPublicQuestionSavesQuestionWhenTopicIsActive() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();
        PublicQuestion question = PublicQuestion.builder()
                .id(1001L)
                .topicId(1L)
                .createdByAdminId(101L)
                .active(true)
                .content("question-1")
                .type(QuestionType.MULTIPLE_CHOICE)
                .difficulty(Difficulty.EASY)
                .answer("answer")
                .explanation("explanation")
                .createdAt(now)
                .updatedAt(now)
                .options(List.of(
                        PublicQuestionOption.create((short) 1, "option-1"),
                        PublicQuestionOption.create((short) 2, "option-2"),
                        PublicQuestionOption.create((short) 3, "option-3"),
                        PublicQuestionOption.create((short) 4, "option-4"),
                        PublicQuestionOption.create((short) 5, "option-5")
                ))
                .build();

        given(topicPort.findById(1L)).willReturn(Optional.of(topic));
        given(publicQuestionPort.save(org.mockito.ArgumentMatchers.any(PublicQuestion.class))).willReturn(question);

        AdminPublicQuestionDetailResult result = adminPublicQuestionService.createPublicQuestion(
                101L,
                1L,
                "question-1",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.EASY,
                "answer",
                "explanation",
                List.of("option-1", "option-2", "option-3", "option-4", "option-5")
        );

        assertThat(result.questionId()).isEqualTo(1001L);
        assertThat(result.topicName()).isEqualTo("Spring");
        assertThat(result.options()).containsExactly("option-1", "option-2", "option-3", "option-4", "option-5");
    }

    @Test
    void createPublicQuestionThrowsWhenTopicDoesNotExist() {
        given(topicPort.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPublicQuestionService.createPublicQuestion(
                101L,
                1L,
                "question-1",
                QuestionType.SHORT_ANSWER,
                Difficulty.EASY,
                "answer",
                "explanation",
                List.of()
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.TOPIC_NOT_FOUND.getMessage());
    }

    @Test
    void createPublicQuestionThrowsWhenTopicIsInactive() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .description(null)
                .active(false)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(topicPort.findById(1L)).willReturn(Optional.of(topic));

        assertThatThrownBy(() -> adminPublicQuestionService.createPublicQuestion(
                101L,
                1L,
                "question-1",
                QuestionType.SHORT_ANSWER,
                Difficulty.EASY,
                "answer",
                "explanation",
                List.of()
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.TOPIC_INACTIVE.getMessage());
    }

    @Test
    void changePublicQuestionStatusDeactivatesQuestion() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        PublicQuestion question = PublicQuestion.builder()
                .id(1001L)
                .topicId(1L)
                .createdByAdminId(101L)
                .active(true)
                .content("question-1")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.EASY)
                .answer("answer")
                .explanation("explanation")
                .createdAt(now)
                .updatedAt(now)
                .options(List.of())
                .build();
        Topic topic = Topic.builder()
                .id(1L)
                .name("Spring")
                .description(null)
                .active(true)
                .createdByAdminId(101L)
                .createdAt(now)
                .updatedAt(now)
                .build();

        given(publicQuestionPort.findById(1001L)).willReturn(Optional.of(question));
        given(publicQuestionPort.save(org.mockito.ArgumentMatchers.any(PublicQuestion.class)))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(topicPort.findById(1L)).willReturn(Optional.of(topic));

        AdminPublicQuestionDetailResult result =
                adminPublicQuestionService.changePublicQuestionStatus(1001L, false);

        assertThat(result.active()).isFalse();
        assertThat(result.topicName()).isEqualTo("Spring");
    }

    @Test
    void changePublicQuestionStatusThrowsWhenQuestionDoesNotExist() {
        given(publicQuestionPort.findById(1001L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPublicQuestionService.changePublicQuestionStatus(1001L, false))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PUBLIC_QUESTION_NOT_FOUND.getMessage());
    }

    @Test
    void deletePublicQuestionDeletesWhenQuestionExists() {
        OffsetDateTime now = OffsetDateTime.parse("2026-04-27T09:00:00+09:00");
        PublicQuestion question = PublicQuestion.builder()
                .id(1001L)
                .topicId(1L)
                .createdByAdminId(101L)
                .active(true)
                .content("question-1")
                .type(QuestionType.SHORT_ANSWER)
                .difficulty(Difficulty.EASY)
                .answer("answer")
                .explanation("explanation")
                .createdAt(now)
                .updatedAt(now)
                .options(List.of())
                .build();

        given(publicQuestionPort.findById(1001L)).willReturn(Optional.of(question));

        adminPublicQuestionService.deletePublicQuestion(1001L);

        then(publicQuestionPort).should().deleteById(1001L);
    }

    @Test
    void deletePublicQuestionThrowsWhenQuestionDoesNotExist() {
        given(publicQuestionPort.findById(1001L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminPublicQuestionService.deletePublicQuestion(1001L))
                .isInstanceOf(BusinessException.class)
                .hasMessage(ErrorCode.PUBLIC_QUESTION_NOT_FOUND.getMessage());
    }
}
