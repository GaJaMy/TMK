package com.tmk.api.adapter.out.ai;

import com.tmk.core.document.vo.GeneratedQuestion;
import com.tmk.core.port.out.QuestionGenerationPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"local", "test"})
public class StubQuestionGenerationAdapter implements QuestionGenerationPort {

    @Override
    public List<GeneratedQuestion> generateQuestions(Long documentId, String context) {
        return List.of(
            new GeneratedQuestion(
                "Which of the following best describes Dependency Injection?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.EASY,
                "2",
                "Dependency Injection is a design pattern where an object receives its dependencies from an external source rather than creating them itself.",
                List.of(
                    "A design pattern where objects create their own dependencies",
                    "A design pattern where objects receive their dependencies from external sources",
                    "A framework for managing database connections",
                    "A method for optimizing memory usage",
                    "A technique for parallel processing"
                )
            ),
            new GeneratedQuestion(
                "What is the main purpose of Clean Architecture?",
                QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM,
                "Clean Architecture separates concerns into independent layers (domain, application, infrastructure, presentation) to promote testability, maintainability, and framework independence.",
                "Clean Architecture by Robert C. Martin aims to create systems that are independent of frameworks, testable, independent of the UI, independent of the database, and independent of any external agency.",
                List.of()
            ),
            new GeneratedQuestion(
                "Spring Boot auto-configuration automatically sets up beans based on what is available on the classpath.",
                QuestionType.TRUE_FALSE,
                Difficulty.HARD,
                "TRUE",
                "Spring Boot's auto-configuration mechanism (@EnableAutoConfiguration) inspects the classpath and conditionally creates beans using @ConditionalOnClass, @ConditionalOnMissingBean, etc.",
                List.of()
            )
        );
    }
}
