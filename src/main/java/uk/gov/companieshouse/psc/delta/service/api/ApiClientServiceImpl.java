package uk.gov.companieshouse.psc.delta.service.api;

import java.util.HashMap;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.http.ApiKeyHttpClient;
import uk.gov.companieshouse.api.http.HttpClient;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.logging.Logger;


/**
 * Service that sends REST requests via private SDK.
 */
@Primary
@Service
public class ApiClientServiceImpl extends BaseApiClientServiceImpl implements ApiClientService {

    @Value("${api.psc-data-api-key}")
    private String chsApiKey;

    @Value("${api.api-url}")
    private String apiUrl;

    @Value("${api.internal-api-url}")
    private String internalApiUrl;

    /**
     * Construct an {@link ApiClientServiceImpl}.
     *
     * @param logger the CH logger
     */
    @Autowired
    public ApiClientServiceImpl(final Logger logger) {
        super(logger);
    }

    @Override
    public InternalApiClient getApiClient(String contextId) {
        InternalApiClient internalApiClient = new InternalApiClient(getHttpClient(contextId));
        internalApiClient.setBasePath(apiUrl);
        internalApiClient.setInternalBasePath(internalApiUrl);
        return internalApiClient;
    }

    /**
     * Process PSC Full Record Delta message.
     */
    public ApiResponse<Void> putPscFullRecord(String log, String companyId,
            String notficationId, FullRecordCompanyPSCApi fullRecordCompanyPscApi) {

        final String uri = String.format(
                "/company/%s/persons-with-significant-control/%s/full_record",
                    companyId, notficationId);

        Map<String, Object> logMap = createLogMap(companyId, "PUT", uri);
        logger.infoContext(log, String.format("PUT %s", uri), logMap);
        return executeOp(log, "putPscFullRecord", uri,
                getApiClient(log).privatePscFullRecordResourceHandler()
                        .putPscFullRecord(uri, fullRecordCompanyPscApi));
    }

    private HttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    @Override
    public ApiResponse<Void> deletePscFullRecord(String log, 
                                                 String notificationId, 
                                                 String companyNumber) {

        final String uri = String.format(
                "/company/%s/persons-with-significant-control/%s/delete",
                       companyNumber, 
                       notificationId);
        
        Map<String,Object> logMap = createLogMap(notificationId, "DELETE", uri);
        logger.infoContext(log, String.format("Delete %s", uri), logMap);

        return executeOp(log, "deletePscFullRecord", uri, 
                getApiClient(log).privatePscFullRecordResourceHandler()
                    .deletePscFullRecord(uri));
    }
    
    private Map<String, Object> createLogMap(String consumerId, String method, String path) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("id", consumerId);
        logMap.put("method", method);
        logMap.put("path", path);
        return logMap;
    }
}
