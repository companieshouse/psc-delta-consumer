package uk.gov.companieshouse.psc.delta.service.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordDelete;
import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordPut;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.processor.DeletePscApiClientRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ApiClientServiceImplTest {

    private static final String CONTEXT_ID = "testContext";
    private static final String COMPANY_NUMBER = "test12345";
    private static final String NOTIFICATION_ID = "testId123456";
    private static final String DELTA_AT = "20240219123045999999";
    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";

    private final String uri = "/company/%s/persons-with-significant-control/%s/full_record";

    private ApiClientServiceImpl apiClientService;

    @Mock
    private DeletePscApiClientRequest clientRequest;
    @Mock
    private Logger logger;

    @BeforeEach
    void setUp(){
        apiClientService = new ApiClientServiceImpl(logger);
        ReflectionTestUtils.setField(apiClientService, "chsApiKey", "testKey");
        ReflectionTestUtils.setField(apiClientService, "apiUrl", "http://localhost:8888");
    }

    @Test
    void returnOkResponseWhenValidPutRequestSentToApi(){
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        String expectedUri = String.format(uri, COMPANY_NUMBER, NOTIFICATION_ID);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PscFullRecordPut.class));

        ApiResponse<Void> response = apiClientServiceSpy.putPscFullRecord(CONTEXT_ID,
                COMPANY_NUMBER,
                NOTIFICATION_ID,
                new FullRecordCompanyPSCApi());

        verify(apiClientServiceSpy).executeOp(anyString(), eq("putPscFullRecord"),
                eq(expectedUri),
                any(PscFullRecordPut.class));

        assertThat(response).isEqualTo(expectedResponse);
    }

    @Test
    void returnOkResponseWhenValidDeleteRequestSentToApi(){
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PscFullRecordDelete.class));
        when(clientRequest.getContextId()).thenReturn(CONTEXT_ID);
        when(clientRequest.getNotificationId()).thenReturn(NOTIFICATION_ID);
        when(clientRequest.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(clientRequest.getDeltaAt()).thenReturn(DELTA_AT);
        when(clientRequest.getKind()).thenReturn(INDIVIDUAL_KIND);

        ApiResponse<Void> response = apiClientServiceSpy.deletePscFullRecord(clientRequest);

        assertThat(response).isEqualTo(expectedResponse);
    }
}
