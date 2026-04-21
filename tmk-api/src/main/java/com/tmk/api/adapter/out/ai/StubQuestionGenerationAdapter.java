package com.tmk.api.adapter.out.ai;

import com.tmk.core.document.vo.GeneratedQuestion;
import com.tmk.core.port.out.ai.QuestionGenerationPort;
import com.tmk.core.question.entity.Difficulty;
import com.tmk.core.question.entity.QuestionType;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Profile({"local", "test"})
public class StubQuestionGenerationAdapter implements QuestionGenerationPort {

    @Override
    public List<GeneratedQuestion> generateQuestions(Long documentId, String topic, String context) {
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
            ),
            new GeneratedQuestion(
                "What does the `@Transactional` annotation primarily provide in Spring?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.EASY,
                "3",
                "It defines a transactional boundary so a unit of work can commit or roll back consistently.",
                List.of(
                    "It enables dependency injection",
                    "It registers a bean in the container",
                    "It defines a transactional boundary",
                    "It configures HTTP security",
                    "It creates database indexes"
                )
            ),
            new GeneratedQuestion(
                "A repository in Clean Architecture belongs to the infrastructure layer as an implementation, while the abstraction can live in the core layer.",
                QuestionType.TRUE_FALSE,
                Difficulty.EASY,
                "TRUE",
                "The port abstraction is usually owned by the core/application side, and the concrete persistence adapter belongs to infrastructure.",
                List.of()
            ),
            new GeneratedQuestion(
                "Explain why constructor injection is generally preferred over field injection in Spring.",
                QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM,
                "Constructor injection makes dependencies explicit, supports immutability, and improves testability by requiring all mandatory collaborators at creation time.",
                "Constructor injection surfaces required dependencies in the type itself and avoids hidden mutable framework wiring.",
                List.of()
            ),
            new GeneratedQuestion(
                "Which choice best describes the role of a Port in Hexagonal Architecture?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                "1",
                "A Port is an application-owned abstraction that defines how the core communicates with external systems or driving actors.",
                List.of(
                    "An abstraction owned by the application core for interaction boundaries",
                    "A concrete JPA repository implementation",
                    "A deployment-only network interface",
                    "A Spring MVC controller method",
                    "A Docker container entrypoint"
                )
            ),
            new GeneratedQuestion(
                "In JPA, `FetchType.LAZY` delays loading an association until it is actually accessed.",
                QuestionType.TRUE_FALSE,
                Difficulty.MEDIUM,
                "TRUE",
                "Lazy loading is designed to avoid loading related data until needed, though transaction boundaries and proxies matter.",
                List.of()
            ),
            new GeneratedQuestion(
                "Why can exposing entities directly from the controller become a design problem?",
                QuestionType.SHORT_ANSWER,
                Difficulty.MEDIUM,
                "It couples API responses to persistence structure, leaks internal fields, and makes API evolution and serialization behavior harder to control.",
                "DTOs help separate external contracts from internal domain and persistence concerns.",
                List.of()
            ),
            new GeneratedQuestion(
                "Which option best explains why Redis is suitable for refresh token storage?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.HARD,
                "4",
                "Redis supports TTL-based expiration and fast key-based access, which fits token lifecycle management well.",
                List.of(
                    "It provides strong relational joins for user-token lookup",
                    "It automatically signs JWT tokens",
                    "It replaces the need for access tokens",
                    "It supports fast access with expiration control",
                    "It guarantees permanent archival storage"
                )
            ),
            new GeneratedQuestion(
                "If a service directly depends on a concrete external client class instead of a port abstraction, the architecture becomes more tightly coupled.",
                QuestionType.TRUE_FALSE,
                Difficulty.HARD,
                "TRUE",
                "Depending on concrete infrastructure from the core weakens testability and violates the intended dependency direction.",
                List.of()
            ),
            new GeneratedQuestion(
                "Describe a practical reason to separate API, core, and infra modules in a Spring project.",
                QuestionType.SHORT_ANSWER,
                Difficulty.HARD,
                "Separating modules clarifies dependency direction, keeps business rules independent from frameworks, and makes testing and replacement of infrastructure concerns easier.",
                "This separation supports cleaner boundaries and reduces accidental coupling between delivery and domain logic.",
                List.of()
            ),
            new GeneratedQuestion(
                "Which statement best describes why pgvector may be chosen inside PostgreSQL rather than using a separate vector database for an MVP?",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.HARD,
                "2",
                "It keeps relational data and vector search in one operational store, reducing complexity and cost for an MVP.",
                List.of(
                    "It eliminates the need for embeddings entirely",
                    "It allows relational and vector data to be managed together",
                    "It guarantees better semantic quality than LLMs",
                    "It removes all indexing costs",
                    "It makes Redis unnecessary"
                )
            )
        );
    }
}
