package uk.gov.companieshouse.psc.delta.mapper;

import consumer.exception.NonRetryableErrorException;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscDeleteDelta.KindEnum;

@Component
public class KindMapper {

    public String mapKindForDelete(KindEnum kindEnum) {
        switch (kindEnum) {
            case INDIVIDUAL:
                return "individual-person-with-significant-control";
            case CORPORATE_ENTITY:
                return "corporate-entity-person-with-significant-control";
            case LEGAL_PERSON:
                return "legal-person-person-with-significant-control";
            case SUPER_SECURE:
                return "super-secure-person-with-significant-control";
            case INDIVIDUAL_BENEFICIAL_OWNER:
                return "individual-beneficial-owner";
            case CORPORATE_ENTITY_BENEFICIAL_OWNER:
                return "corporate-entity-beneficial-owner";
            case LEGAL_PERSON_BENEFICIAL_OWNER:
                return "legal-person-beneficial-owner";
            case SUPER_SECURE_BENEFICIAL_OWNER:
                return "super-secure-beneficial-owner";
            default:
                throw new NonRetryableErrorException("Invalid Kind type supplied in delete");
        }
    }
}
