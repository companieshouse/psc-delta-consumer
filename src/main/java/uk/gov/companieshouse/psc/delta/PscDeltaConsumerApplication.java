package uk.gov.companieshouse.psc.delta;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PscDeltaConsumerApplication {

    public static final String NAMESPACE = "psc-delta-consumer";

    public static void main(String[] args) {
        SpringApplication.run(PscDeltaConsumerApplication.class, args);
    }

}