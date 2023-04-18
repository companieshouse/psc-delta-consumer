package uk.gov.companieshouse.psc.delta.service.api;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;

/**
 * The {@code ApiClientService} interface provides an abstraction that can be
 * used when testing {@code ApiClientManager} static methods, without imposing
 * the use of a test framework that supports mocking of static methods.
 */
public interface ApiClientService {

    InternalApiClient getApiClient(String contextId);

    /**
     * Submit Profile Information.
     */
    ApiResponse<Void> putPscFullRecord(
            final String log,
            final String companyId,
            final String notficationId,
            final FullRecordCompanyPSCApi fullRecordCompanyPscApi);
}
