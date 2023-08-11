package uk.gov.companieshouse.psc.delta.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

/**
 * Configuration class for logging.
 */
@Configuration
public class LoggingConfig {

    /**
     * Main application logger with component specific namespace.
     *
     * @return the {@link LoggerFactory} for the specified namespace
     */
    @Bean
    public Logger logger() {
        return LoggerFactory.getLogger("psc-delta-consumer");
    }
}