package uk.gov.companieshouse.psc.delta.processor;

import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;
import uk.gov.companieshouse.psc.delta.mapper.KindMapper;
import uk.gov.companieshouse.psc.delta.mapper.MapperUtils;
import uk.gov.companieshouse.psc.delta.service.ApiClientService;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;

@Component
public class PscDeltaProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);

    private final PscApiTransformer transformer;
    private final ApiClientService apiClientService;
    private final KindMapper kindMapper;
    private final ObjectMapper objectMapper;

    public PscDeltaProcessor(PscApiTransformer transformer, ApiClientService apiClientService, KindMapper kindMapper,
            ObjectMapper objectMapper) {
        this.transformer = transformer;
        this.apiClientService = apiClientService;
        this.kindMapper = kindMapper;
        this.objectMapper = objectMapper;
    }

    public void processDelta(Message<ChsDelta> chsDelta) {
        LOGGER.info("Processing PSC delta", DataMapHolder.getLogMap());

        ChsDelta payload = chsDelta.getPayload();
        PscDelta pscDelta;
        try {
            pscDelta = objectMapper.readValue(payload.getData(), PscDelta.class);
        } catch (JsonProcessingException ex) {
            final String msg = "Failed to extract PSC delta";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new RetryableErrorException(msg, ex);
        }

        Psc psc = pscDelta.getPscs().getFirst(); // We will only ever get one PSC per request

        DataMapHolder.get()
                .companyNumber(psc.getCompanyNumber())
                .itemId(psc.getInternalId());

        FullRecordCompanyPSCApi fullRecordCompanyPscApi = transformer.transform(pscDelta);
        LOGGER.info("Successfully transformed PSC", DataMapHolder.getLogMap());

        apiClientService.putPscFullRecord(fullRecordCompanyPscApi.getExternalData().getCompanyNumber(),
                fullRecordCompanyPscApi.getExternalData().getNotificationId(),
                fullRecordCompanyPscApi);
    }

    public void processDelete(Message<ChsDelta> chsDelta) {
        LOGGER.info("Processing PSC delete delta", DataMapHolder.getLogMap());

        final ChsDelta payload = chsDelta.getPayload();
        final String contextId = payload.getContextId();

        PscDeleteDelta pscDelete;
        try {
            pscDelete = objectMapper.readValue(payload.getData(), PscDeleteDelta.class);
        } catch (JsonProcessingException ex) {
            final String msg = "Failed to extract PSC delete delta";
            LOGGER.info(msg, DataMapHolder.getLogMap());
            throw new RetryableErrorException(msg, ex);
        }

        DataMapHolder.get()
                .companyNumber(pscDelete.getCompanyNumber())
                .itemId(pscDelete.getInternalId());

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

        apiClientService.deletePscFullRecord(clientRequest);
    }
}
