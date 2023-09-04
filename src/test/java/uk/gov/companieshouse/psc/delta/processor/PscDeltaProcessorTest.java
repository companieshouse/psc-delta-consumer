package uk.gov.companieshouse.psc.delta.processor;

import consumer.exception.NonRetryableErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.psc.delta.service.api.ApiClientService;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;
import uk.gov.companieshouse.logging.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscDeltaProcessorTest {
    private PscDeltaProcessor deltaProcessor;
    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private PscApiTransformer transformer;

    @BeforeEach
    void setUp() {
        deltaProcessor = new PscDeltaProcessor(logger, apiClientService, transformer);
    }

    @Test
    @DisplayName("Transforms a kafka message containing a ChsDelta payload into a PscDelta")
    void When_ValidChsDeltaMessage_Expect_ValidPscDeltaMapping() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = createChsDeltaMessage();
        PscDelta expectedDelta = createPscDelta();
        FullRecordCompanyPSCApi apiObject = createFullRecordPsc();
        when(transformer.transform(expectedDelta)).thenReturn(apiObject);

        deltaProcessor.processDelta(mockChsDeltaMessage);

        verify(transformer).transform(expectedDelta);
    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, ()->deltaProcessor.processDelta(mockChsDeltaMessage));
    }

    private Message<ChsDelta> createChsDeltaMessage() throws IOException {
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream("super-secure-psc-delta-example.json"));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);
        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData(data)
                .setContextId("context_id")
                .setAttempt(1)
                .build();
        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("PSC_DELTA_RETRY_COUNT", 1)
                .build();
    }

    public Message<ChsDelta> createInvalidChsDeltaMessage() {
        ChsDelta mockChsDelta = ChsDelta.newBuilder()
                .setData("This is some invalid data")
                .setContextId("context_id")
                .setAttempt(1)
                .build();
        return MessageBuilder
                .withPayload(mockChsDelta)
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("PSC_DELTA_RETRY_COUNT", 1)
                .build();
    }

    private PscDelta createPscDelta() {

        Psc psc = new Psc();

        psc.setCompanyNumber("00623672");
        psc.setPscId("3");
        psc.setInternalId("5");
        psc.setKind(Psc.KindEnum.SUPER_SECURE);
        psc.setCeasedOn("20180201");

        return new PscDelta().addPscsItem(psc);
    }

    private FullRecordCompanyPSCApi createFullRecordPsc() {
        FullRecordCompanyPSCApi fullRecordCompanyPscApi = new FullRecordCompanyPSCApi();
        ExternalData externalData = new ExternalData();
        Data data = new Data();

        externalData.setId("5");
        externalData.setInternalId("5");
        externalData.setNotificationId("5");
        externalData.setCompanyNumber("00623672");

        data.setCeasedOn(LocalDate.of(2018, 2, 1));
        data.setKind("super-secure-person-with-significant-control");

        externalData.setData(data);
        fullRecordCompanyPscApi.setExternalData(externalData);

        return fullRecordCompanyPscApi;
    }

    }
