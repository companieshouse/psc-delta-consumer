package uk.gov.companieshouse.psc.delta.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import consumer.exception.RetryableErrorException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDeleteDelta;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;
import uk.gov.companieshouse.psc.delta.mapper.KindMapper;
import uk.gov.companieshouse.psc.delta.mapper.MapperUtils;
import uk.gov.companieshouse.psc.delta.service.ApiClientService;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;

@Component
public class PscDeltaProcessor {

    private final PscApiTransformer transformer;
    private final Logger logger;
    private final ApiClientService apiClientService;
    private final KindMapper kindMapper;
    private final ObjectMapper objectMapper;

    public PscDeltaProcessor(Logger logger, ApiClientService apiClientService, PscApiTransformer transformer,
            KindMapper kindMapper, ObjectMapper objectMapper) {
        this.logger = logger;
        this.apiClientService = apiClientService;
        this.transformer = transformer;
        this.kindMapper = kindMapper;
        this.objectMapper = objectMapper;
    }

    public void processDelta(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();
        PscDelta pscDelta;
        try {
            pscDelta = objectMapper.readValue(payload.getData(), PscDelta.class);
            Psc psc = pscDelta.getPscs().get(0); // We will only ever get one PSC per request

            DataMapHolder.get()
                    .companyNumber(psc.getCompanyNumber())
                    .requestId(contextId)
                    .itemId(psc.getPscId());

            logger.infoContext(contextId, "Successfully extracted psc delta",
                    DataMapHolder.getLogMap());
        } catch (Exception ex) {
            logger.errorContext(contextId, ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw new RetryableErrorException("Error when extracting psc delta", ex);
        }

        FullRecordCompanyPSCApi fullRecordCompanyPscApi;
        try {
            fullRecordCompanyPscApi = transformer.transform(pscDelta);
            logger.infoContext(contextId, "Successfully transformed psc",
                    DataMapHolder.getLogMap());
        } catch (Exception ex) {
            logger.errorContext(contextId, ex.getMessage(), ex, DataMapHolder.getLogMap());
            throw new RetryableErrorException("Error when transforming into api object", ex);
        }

        apiClientService.putPscFullRecord(fullRecordCompanyPscApi.getExternalData().getCompanyNumber(),
                fullRecordCompanyPscApi.getExternalData().getNotificationId(),
                fullRecordCompanyPscApi);
    }

    public void processDelete(Message<ChsDelta> chsDelta) {
        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();

        PscDeleteDelta pscDelete;
        try {
            pscDelete = objectMapper.readValue(payload.getData(), PscDeleteDelta.class);

            DataMapHolder.get().requestId(contextId);

        } catch (Exception ex) {
            throw new RetryableErrorException("Error when extracting psc delete delta", ex);
        }

        logger.info(String.format("PscDeleteDelta extracted for context ID"
                + " [%s] Kafka message: [%s]", contextId, pscDelete));
        final String notificationId = MapperUtils.encode(pscDelete.getInternalId());
        final String kind = kindMapper.mapKindForDelete(pscDelete.getKind());
        final String companyNumber = pscDelete.getCompanyNumber();
        DeletePscApiClientRequest clientRequest = DeletePscApiClientRequest.Builder.builder()
                .contextId(contextId)
                .notificationId(notificationId)
                .companyNumber(companyNumber)
                .deltaAt(pscDelete.getDeltaAt())
                .kind(kind)
                .build();

        logger.info(String.format(
                "Performing a DELETE for PSC id: [%s]", notificationId));
        apiClientService.deletePscFullRecord(clientRequest);
    }
}
