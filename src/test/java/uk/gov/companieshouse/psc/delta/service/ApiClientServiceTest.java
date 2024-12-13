package uk.gov.companieshouse.psc.delta.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.delta.PrivateDeltaResourceHandler;
import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordDelete;
import uk.gov.companieshouse.api.handler.delta.pscfullrecord.request.PscFullRecordPut;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.psc.delta.processor.DeletePscApiClientRequest;

@ExtendWith(MockitoExtension.class)
class ApiClientServiceTest {

    private static final String COMPANY_NUMBER = "company_number";
    private static final String NOTIFICATION_ID = "notification_id";
    private static final String DELTA_AT = "20240219123045999999";
    private static final String INDIVIDUAL_KIND = "individual-person-with-significant-control";
    private static final String URI = "/company/%s/persons-with-significant-control/%s/full_record";
    private static final ApiResponse<Void> SUCCESS_RESPONSE = new ApiResponse<>(200, null);

    @InjectMocks
    private ApiClientService apiClientService;

    @Mock
    private Supplier<InternalApiClient> internalApiClientSupplier;
    @Mock
    private ResponseHandler responseHandler;

    @Mock
    private FullRecordCompanyPSCApi fullRecordCompanyPSCApi;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private PrivateDeltaResourceHandler privateDeltaResourceHandler;
    @Mock
    private PscFullRecordPut pscFullRecordPut;
    @Mock
    private PscFullRecordDelete pscFullRecordDelete;
    @Mock
    private DeletePscApiClientRequest deletePscApiClientRequest;

    @Test
    void shouldSuccessfullySendPutRequestToApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscFullRecord(anyString(), any(FullRecordCompanyPSCApi.class)))
                .thenReturn(pscFullRecordPut);
        when(pscFullRecordPut.execute()).thenReturn(SUCCESS_RESPONSE);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.putPscFullRecord(COMPANY_NUMBER, NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        verify(privateDeltaResourceHandler).putPscFullRecord(formattedUri, fullRecordCompanyPSCApi);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldSendPutRequestAndHandleNon200ResponseFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscFullRecord(anyString(), any(FullRecordCompanyPSCApi.class)))
                .thenReturn(pscFullRecordPut);
        when(pscFullRecordPut.execute()).thenThrow(ApiErrorResponseException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.putPscFullRecord(COMPANY_NUMBER, NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        verify(privateDeltaResourceHandler).putPscFullRecord(formattedUri, fullRecordCompanyPSCApi);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void shouldSendPutRequestAndHandleURIValidationExceptionFromApi() throws Exception {
        // given
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.putPscFullRecord(anyString(), any(FullRecordCompanyPSCApi.class)))
                .thenReturn(pscFullRecordPut);
        when(pscFullRecordPut.execute()).thenThrow(URIValidationException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.putPscFullRecord(COMPANY_NUMBER, NOTIFICATION_ID, fullRecordCompanyPSCApi);

        // then
        verify(privateDeltaResourceHandler).putPscFullRecord(formattedUri, fullRecordCompanyPSCApi);
        verify(responseHandler).handle(any(URIValidationException.class));
    }

    @Test
    void shouldSuccessfullySendDeleteRequestToApi() throws Exception {
        // given
        when(deletePscApiClientRequest.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(deletePscApiClientRequest.getNotificationId()).thenReturn(NOTIFICATION_ID);
        when(deletePscApiClientRequest.getDeltaAt()).thenReturn(DELTA_AT);
        when(deletePscApiClientRequest.getKind()).thenReturn(INDIVIDUAL_KIND);
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscFullRecord(anyString(), anyString(), anyString())).thenReturn(pscFullRecordDelete);
        when(pscFullRecordDelete.execute()).thenReturn(SUCCESS_RESPONSE);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.deletePscFullRecord(deletePscApiClientRequest);

        // then
        verify(privateDeltaResourceHandler).deletePscFullRecord(formattedUri, DELTA_AT, INDIVIDUAL_KIND);
        verifyNoInteractions(responseHandler);
    }

    @Test
    void shouldSendDeleteRequestAndHandleNon200ResponseFromApi() throws Exception {
        // given
        when(deletePscApiClientRequest.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(deletePscApiClientRequest.getNotificationId()).thenReturn(NOTIFICATION_ID);
        when(deletePscApiClientRequest.getDeltaAt()).thenReturn(DELTA_AT);
        when(deletePscApiClientRequest.getKind()).thenReturn(INDIVIDUAL_KIND);
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscFullRecord(anyString(), anyString(), anyString())).thenReturn(pscFullRecordDelete);
        when(pscFullRecordDelete.execute()).thenThrow(ApiErrorResponseException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.deletePscFullRecord(deletePscApiClientRequest);

        // then
        verify(privateDeltaResourceHandler).deletePscFullRecord(formattedUri, DELTA_AT, INDIVIDUAL_KIND);
        verify(responseHandler).handle(any(ApiErrorResponseException.class));
    }

    @Test
    void shouldSendDeleteRequestAndHandleURIValidationExceptionFromApi() throws Exception {
        // given
        when(deletePscApiClientRequest.getCompanyNumber()).thenReturn(COMPANY_NUMBER);
        when(deletePscApiClientRequest.getNotificationId()).thenReturn(NOTIFICATION_ID);
        when(deletePscApiClientRequest.getDeltaAt()).thenReturn(DELTA_AT);
        when(deletePscApiClientRequest.getKind()).thenReturn(INDIVIDUAL_KIND);
        when(internalApiClientSupplier.get()).thenReturn(internalApiClient);
        when(internalApiClient.privatePscFullRecordResourceHandler()).thenReturn(privateDeltaResourceHandler);
        when(privateDeltaResourceHandler.deletePscFullRecord(anyString(), anyString(), anyString())).thenReturn(pscFullRecordDelete);
        when(pscFullRecordDelete.execute()).thenThrow(URIValidationException.class);

        final String formattedUri = String.format(URI, COMPANY_NUMBER, NOTIFICATION_ID);

        // when
        apiClientService.deletePscFullRecord(deletePscApiClientRequest);

        // then
        verify(privateDeltaResourceHandler).deletePscFullRecord(formattedUri, DELTA_AT, INDIVIDUAL_KIND);
        verify(responseHandler).handle(any(URIValidationException.class));
    }
}
