package uk.gov.companieshouse.psc.delta.processor;

import consumer.exception.NonRetryableErrorException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.Message;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.psc.delta.mapper.MapperUtils;
import uk.gov.companieshouse.psc.delta.service.api.ApiClientService;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;
import uk.gov.companieshouse.psc.delta.utils.TestHelper;
import uk.gov.companieshouse.logging.Logger;


import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PscDeltaProcessorTest {
    private TestHelper testHelper = new TestHelper();
    private PscDeltaProcessor deltaProcessor;
    //private MapperUtils mapperUtils;

    @Mock
    private Logger logger;
    @Mock
    private ApiClientService apiClientService;
    @Mock
    private PscApiTransformer transformer;
    @Mock
    FullRecordCompanyPSCApi mockFullRecordPSC;
    

    @BeforeEach
    void setUp() {
        deltaProcessor = new PscDeltaProcessor(logger, apiClientService, transformer);
        //mapperUtils = new MapperUtils();
        //ReflectionTestUtils.setField(deltaProcessor, "mapperUtils", mapperUtils);
    }

    @Test
    void When_InvalidChsDeltaMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelta(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(0)).
                putPscFullRecord(any(),any(),any(), any());
    }

    @Test
    void When_InvalidChsDeleteDeltaMessage_Expect_NonRetryableError() {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createInvalidChsDeltaMessage();
        assertThrows(NonRetryableErrorException.class, () -> deltaProcessor.processDelete(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(0)).
                deletePscFullRecord(any(),any());
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid ChsDelta is given")
    void When_ValidChsDeltaMessage_Expect_ProcessorDoesNotThrow_CallsTransformer() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(false);
        when(transformer.transform(any(PscDelta.class))).thenReturn(mockFullRecordPSC);
        Assertions.assertDoesNotThrow(() -> deltaProcessor.processDelta(mockChsDeltaMessage));
        verify(transformer).transform(any(PscDelta.class));
        Mockito.verify(apiClientService, times(1)).
                putPscFullRecord(any(), any(), any(), any());
        
    }

    @Test
    @DisplayName("Confirms the Processor does not throw when a valid delete ChsDelta is given")
    void When_ValidChsDeleteDeltaMessage_Expect_ProcessorDoesNotThrow() throws IOException {
        Message<ChsDelta> mockChsDeltaMessage = testHelper.createChsDeltaMessage(true);
        Assertions.assertDoesNotThrow(() -> deltaProcessor.processDelete(mockChsDeltaMessage));
        Mockito.verify(apiClientService, times(1)).
                deletePscFullRecord(any(), any());
    }
}
