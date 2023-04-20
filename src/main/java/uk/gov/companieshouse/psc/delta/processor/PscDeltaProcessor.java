package uk.gov.companieshouse.psc.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.exception.NonRetryableErrorException;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;


@Component
public class PscDeltaProcessor {

    private final PscApiTransformer transformer;
    private final Logger logger;

    @Autowired
    public PscDeltaProcessor(Logger logger, PscApiTransformer transformer) {
        this.logger = logger;
        this.transformer = transformer;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        FullRecordCompanyPSCApi fullRecordCompanyPscApi = new FullRecordCompanyPSCApi();
        logger.info(format("Successfully extracted Chs Delta with context_id %s",
                payload.getContextId()));
        ObjectMapper mapper = new ObjectMapper();
        PscDelta pscDelta;
        try {
            pscDelta = mapper.readValue(payload.getData(),
                    PscDelta.class);
            logger.trace(format("Successfully extracted psc delta of %s",
                    pscDelta.toString()));
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when extracting psc delta", ex);
        }

        try {
            fullRecordCompanyPscApi = transformer.transform(pscDelta);
            logger.info(format("Psc: %s", fullRecordCompanyPscApi)); //remove
        } catch (Exception ex) {
            throw new NonRetryableErrorException(
                    "Error when transforming into api object", ex);
        }
    }
}
