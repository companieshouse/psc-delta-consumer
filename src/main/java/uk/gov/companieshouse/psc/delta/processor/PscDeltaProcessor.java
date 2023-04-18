package uk.gov.companieshouse.psc.delta.processor;

import static java.lang.String.format;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.service.api.ApiClientServiceImpl;


@Component
public class PscDeltaProcessor {

    private final Logger logger;
    private final ApiClientServiceImpl apiClientService;

    @Autowired
    public PscDeltaProcessor(Logger logger, ApiClientServiceImpl apiClientService) {
        this.logger = logger;
        this.apiClientService = apiClientService;
    }

    /**
     * Process PSC Statement Delta message.
     */
    public void processDelta(Message<PscDelta> pscDelta) {
        final MessageHeaders headers = pscDelta.getHeaders();
        final PscDelta payload = pscDelta.getPayload();
        logger.info(format("Successfully extracted PSC Delta of %s", payload.toString()));

//        apiClientService.putPscFullRecord(contextId, companyId, notficationId, fullRecordCompanyPSCApi);
    }
}