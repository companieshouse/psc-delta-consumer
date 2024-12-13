package uk.gov.companieshouse.psc.delta.service;

import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;
import uk.gov.companieshouse.psc.delta.processor.DeletePscApiClientRequest;

@Component
public class ApiClientService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String URI = "/company/%s/persons-with-significant-control/%s/full_record";

    private final Supplier<InternalApiClient> internalApiClientSupplier;
    private final ResponseHandler responseHandler;

    public ApiClientService(Supplier<InternalApiClient> internalApiClientSupplier,
            ResponseHandler responseHandler) {
        this.internalApiClientSupplier = internalApiClientSupplier;
        this.responseHandler = responseHandler;
    }

    public void putPscFullRecord(String companyNumber, String notificationId,
            FullRecordCompanyPSCApi fullRecordCompanyPscApi) {
        final String formattedUri = String.format(URI, companyNumber, notificationId);
        LOGGER.info("Sending PUT request to API", DataMapHolder.getLogMap());

        try {
            internalApiClientSupplier.get()
                    .privatePscFullRecordResourceHandler()
                    .putPscFullRecord(formattedUri, fullRecordCompanyPscApi)
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }

    public void deletePscFullRecord(DeletePscApiClientRequest clientRequest) {
        final String formattedUri = String.format(URI, clientRequest.getCompanyNumber(), clientRequest.getNotificationId());
        LOGGER.info("Sending DELETE request to API", DataMapHolder.getLogMap());

        try {
            internalApiClientSupplier.get()
                    .privatePscFullRecordResourceHandler()
                    .deletePscFullRecord(formattedUri, clientRequest.getDeltaAt(), clientRequest.getKind())
                    .execute();
        } catch (ApiErrorResponseException ex) {
            responseHandler.handle(ex);
        } catch (URIValidationException ex) {
            responseHandler.handle(ex);
        }
    }
}
