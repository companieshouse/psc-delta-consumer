package uk.gov.companieshouse.psc.delta.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ApplicationConfigTest {

    @Test
    void applicationContextLoadsSuccessfully() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class)) {
            assertNotNull(context.getBean(ApplicationConfig.class));
        }
    }

    @Test
    void applicationContextFailsToLoadWithInvalidConfig() {
        assertThrows(Exception.class, () -> {
            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(InvalidConfig.class)) {
                context.getBean(ApplicationConfig.class);
            }
        });
    }

    static class InvalidConfig {
        // Empty class to simulate invalid configuration
    }
}
