package com.tmk.api.adapter.out.ai;

import com.tmk.core.port.out.TextExtractionPort;
import org.springframework.stereotype.Component;

@Component
public class StubTextExtractionAdapter implements TextExtractionPort {

    @Override
    public String extract(String source) {
        return "This is sample extracted text from document source: " + source + "\n\n"
                + "Section 1: Introduction\n"
                + "Spring Boot is an opinionated framework that simplifies the development of production-ready "
                + "applications. It provides auto-configuration, embedded servers, and a wide range of starter dependencies.\n\n"
                + "Section 2: Key Concepts\n"
                + "Dependency Injection (DI) is a design pattern where objects receive their dependencies from external sources "
                + "rather than creating them internally. Spring's IoC container manages this process automatically.\n\n"
                + "Section 3: Architecture\n"
                + "Clean Architecture separates concerns into layers: domain, application, infrastructure, and presentation. "
                + "This promotes testability, maintainability, and independence from frameworks.\n\n"
                + "Section 4: Testing\n"
                + "Unit tests verify individual components in isolation. Integration tests verify that components work together "
                + "correctly. Both are essential for maintaining software quality.";
    }
}
