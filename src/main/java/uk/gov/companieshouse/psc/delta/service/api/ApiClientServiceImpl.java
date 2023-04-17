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

    @Value("${api.pscFullRecord-data-api-key}")
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

    public ApiResponse<Void> putPscFullRecord(String log, String companyId, String customerId, FullRecordCompanyPSCApi fullRecordCompanyPSCApi) {

        final String uri = String.format("/company/%s/persons-with-significant-control/%s/individual", companyId, customerId);

        Map<String, Object> logMap = createLogMap(companyId, "PUT", uri);
        logger.infoContext(log, String.format("PUT %s", uri), logMap);
        return executeOp(log, "putPscFullRecord", uri,
                getApiClient(log).privatePscFullRecordResourceHandler()
                        .putPscFullRecord(uri, fullRecordCompanyPSCApi));
    }

    private HttpClient getHttpClient(String contextId) {
        ApiKeyHttpClient httpClient = new ApiKeyHttpClient(chsApiKey);
        httpClient.setRequestId(contextId);
        return httpClient;
    }

    private Map<String, Object> createLogMap(String consumerId, String method, String path) {
        final Map<String, Object> logMap = new HashMap<>();
        logMap.put("id", consumerId);
        logMap.put("method", method);
        logMap.put("path", path);
        return logMap;
    }
}
