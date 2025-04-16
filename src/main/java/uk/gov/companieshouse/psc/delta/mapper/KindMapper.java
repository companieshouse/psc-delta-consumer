package uk.gov.companieshouse.psc.delta.mapper;

import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscDeleteDelta.KindEnum;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;

@Component
public class KindMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    public String mapKindForDelete(KindEnum kindEnum) {
        return switch (kindEnum) {
            case INDIVIDUAL -> "individual-person-with-significant-control";
            case CORPORATE_ENTITY -> "corporate-entity-person-with-significant-control";
            case LEGAL_PERSON -> "legal-person-person-with-significant-control";
            case SUPER_SECURE -> "super-secure-person-with-significant-control";
            case INDIVIDUAL_BENEFICIAL_OWNER -> "individual-beneficial-owner";
            case CORPORATE_ENTITY_BENEFICIAL_OWNER -> "corporate-entity-beneficial-owner";
            case LEGAL_PERSON_BENEFICIAL_OWNER -> "legal-person-beneficial-owner";
            case SUPER_SECURE_BENEFICIAL_OWNER -> "super-secure-beneficial-owner";
            default -> {
                final String msg = "Invalid Kind type supplied in delete";
                LOGGER.error(msg, DataMapHolder.getLogMap());
                throw new NonRetryableErrorException("Invalid Kind type supplied in delete");
            }
        };
    }
}
