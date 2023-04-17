package uk.gov.companieshouse.psc.delta.service.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordPut;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class ApiClientServiceImplTest {

    private final String contextId = "testContext";
    private final String companyNumber = "test12345";
    private final String pscId = "testId123456";

    private final String uri = "/company/%s/persons-with-significant-control/%s/individual";

    private ApiClientServiceImpl apiClientService;

    @Mock
    private Logger logger;

    @BeforeEach
    public void setUp(){
        apiClientService = new ApiClientServiceImpl(logger);
        ReflectionTestUtils.setField(apiClientService, "chsApiKey", "testKey");
        ReflectionTestUtils.setField(apiClientService, "apiUrl", "http://localhost:8888");
    }

    @Test
    public void returnOkResponseWhenValidPutRequestSentToApi(){
        final ApiResponse<Void> expectedResponse = new ApiResponse<>(HttpStatus.OK.value(), null, null);
        String expectedUri = String.format(uri, companyNumber, pscId);
        ApiClientServiceImpl apiClientServiceSpy = Mockito.spy(apiClientService);
        doReturn(expectedResponse).when(apiClientServiceSpy).executeOp(anyString(), anyString(),
                anyString(),
                any(PscFullRecordPut.class));

        ApiResponse<Void> response = apiClientServiceSpy.putPscFullRecord(contextId,
                companyNumber,
                pscId,
                new FullRecordCompanyPSCApi());

        verify(apiClientServiceSpy).executeOp(anyString(), eq("putPscFullRecord"),
                eq(expectedUri),
                any(PscFullRecordPut.class));

        assertThat(response).isEqualTo(expectedResponse);
    }
}
