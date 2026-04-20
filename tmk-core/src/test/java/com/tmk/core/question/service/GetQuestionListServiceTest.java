package com.tmk.core.question.service;

import com.tmk.core.port.out.QuestionPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.Question;
import com.tmk.core.question.entity.QuestionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetQuestionListServiceTest {

    @Mock
    private QuestionPort questionPort;

    @InjectMocks
    private GetQuestionListService getQuestionListService;

    private Question buildQuestion(QuestionType type, Difficulty difficulty) {
        OffsetDateTime now = OffsetDateTime.now();
        return Question.builder()
                .documentId(1L)
                .content("Test question?")
                .type(type)
                .difficulty(difficulty)
                .answer("A")
                .explanation("Because...")
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void getList_withFilters_returnsFilteredList() {
        // Arrange
        QuestionType type = QuestionType.MULTIPLE_CHOICE;
        Difficulty difficulty = Difficulty.EASY;
        int page = 0;
        int size = 10;
        List<Question> expected = List.of(buildQuestion(type, difficulty));
        when(questionPort.findByFilters(type, difficulty, 0, size)).thenReturn(expected);

        // Act
        List<Question> result = getQuestionListService.getList(type, difficulty, page, size);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getType()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(result.get(0).getDifficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void getList_withNullFilters_returnsAllQuestions() {
        // Arrange
        int page = 0;
        int size = 10;
        List<Question> expected = List.of(
                buildQuestion(QuestionType.MULTIPLE_CHOICE, Difficulty.EASY),
                buildQuestion(QuestionType.SHORT_ANSWER, Difficulty.HARD)
        );
        when(questionPort.findByFilters(null, null, 0, size)).thenReturn(expected);

        // Act
        List<Question> result = getQuestionListService.getList(null, null, page, size);

        // Assert
        assertThat(result).hasSize(2);
    }

    @Test
    void getList_calculatesOffsetCorrectly() {
        // Arrange
        int page = 2;
        int size = 10;
        int expectedOffset = 20;
        List<Question> expected = List.of(buildQuestion(QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM));
        when(questionPort.findByFilters(null, null, expectedOffset, size)).thenReturn(expected);

        // Act
        List<Question> result = getQuestionListService.getList(null, null, page, size);

        // Assert
        assertThat(result).hasSize(1);
    }

    @Test
    void count_withFilters_returnsCount() {
        // Arrange
        QuestionType type = QuestionType.MULTIPLE_CHOICE;
        Difficulty difficulty = Difficulty.EASY;
        when(questionPort.countByFilters(type, difficulty)).thenReturn(5L);

        // Act
        long result = getQuestionListService.count(type, difficulty);

        // Assert
        assertThat(result).isEqualTo(5L);
    }

    @Test
    void count_withNoFilters_returnsTotal() {
        // Arrange
        when(questionPort.countByFilters(null, null)).thenReturn(42L);

        // Act
        long result = getQuestionListService.count(null, null);

        // Assert
        assertThat(result).isEqualTo(42L);
    }
}
