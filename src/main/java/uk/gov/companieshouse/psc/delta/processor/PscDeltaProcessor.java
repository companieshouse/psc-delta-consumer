package uk.gov.companieshouse.psc.delta.processor;

import static java.lang.String.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.NonRetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;
import uk.gov.companieshouse.psc.delta.service.api.ApiClientService;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;

@Component
public class PscDeltaProcessor {

    private final PscApiTransformer transformer;
    private final Logger logger;
    private final ApiClientService apiClientService;

    /**
     * Constructor for processor.
     */
    @Autowired
    public PscDeltaProcessor(
            Logger logger, ApiClientService apiClientService, PscApiTransformer transformer) {
        this.logger = logger;
        this.apiClientService = apiClientService;
        this.transformer = transformer;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<ChsDelta> chsDelta) {

        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        PscDelta pscDelta;
        try {
            pscDelta = new ObjectMapper().readValue(payload.getData(), PscDelta.class);
            Psc psc = pscDelta.getPscs().get(0); // We will only ever get one PSC per request

            DataMapHolder.get()
                    .companyNumber(psc.getCompanyNumber())
                    .itemId(psc.getPscId());

            logger.infoContext(contextId, "Successfully extracted psc delta",
                    DataMapHolder.getLogMap());
        } catch (Exception ex) {
            logger.errorContext(contextId, ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Error when extracting psc delta", ex);
        }

        FullRecordCompanyPSCApi fullRecordCompanyPscApi;
        try {
            fullRecordCompanyPscApi = transformer.transform(pscDelta);
            logger.infoContext(contextId, "Successfully transformed psc",
                    DataMapHolder.getLogMap());
        } catch (Exception ex) {
            logger.errorContext(contextId, ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw new NonRetryableErrorException("Error when transforming into api object", ex);
        }

        apiClientService.putPscFullRecord(contextId,
                fullRecordCompanyPscApi.getExternalData().getCompanyNumber(),
                fullRecordCompanyPscApi.getExternalData().getNotificationId(),
                fullRecordCompanyPscApi);
    }
}
