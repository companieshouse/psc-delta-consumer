package uk.gov.companieshouse.psc.delta.matcher;

import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

public class CustomRequestMatcher implements ValueMatcher<Request> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String PUT = "PUT";

    private final String expectedOutput;
    private final String expectedUrl;
    private final List<String> fieldsToIgnore;

    public CustomRequestMatcher(String output, String expectedUrl) {
        this.expectedOutput = output;
        this.expectedUrl = expectedUrl;
        this.fieldsToIgnore = new ArrayList<>();
    }

    public CustomRequestMatcher(String output, String expectedUrl, List<String> fieldsToIgnore) {
        this.expectedOutput = output;
        this.expectedUrl = expectedUrl;
        this.fieldsToIgnore = fieldsToIgnore;
    }

    @Override
    public MatchResult match(Request request) {
        return MatchResult.of(
                this.matchUrl(request.getUrl()) &&
                        this.matchMethod(request.getMethod().toString()) &&
                        this.matchBody(request.getBodyAsString()));
    }

    private boolean matchUrl(String actualUrl) {
        boolean urlResult = this.expectedUrl.equals(actualUrl);
        if (!urlResult) {
            LOGGER.error("URL does not match - expected: [%s], actual: [%s]".formatted(expectedUrl, actualUrl));
        }

        return urlResult;
    }

    private boolean matchMethod(String actualMethod) {
        boolean typeResult = PUT.equals(actualMethod);
        if (!typeResult) {
            LOGGER.error("Method does not match - expected: [%s], actual: [%s]".formatted(PUT, actualMethod));
        }

        return typeResult;
    }

    private boolean matchBody(String actualBody) {
        try {
            JSONObject expectedBody = new JSONObject(this.expectedOutput);
            JSONObject actual = new JSONObject(actualBody);
            this.fieldsToIgnore.forEach((fieldName) -> {
                try {
                    this.removeField(actual, fieldName);
                } catch (JSONException var4) {
                    JSONException e = var4;
                    throw new RuntimeException(e);
                }
            });
            ObjectMapper mapper = new ObjectMapper();
            JsonNode expectedNode = mapper.readTree(expectedBody.toString());
            JsonNode actualNode = mapper.readTree(actual.toString());
            boolean bodyResult = expectedNode.equals(actualNode);
            if (!bodyResult) {
                String var10001 = String.valueOf(expectedBody);
                LOGGER.error("Body does not match - expected: [%s], actual: [%s]".formatted(var10001, actualBody));
            }

            return bodyResult;
        } catch (JsonProcessingException | JSONException var8) {
            Exception ex = var8;
            LOGGER.error("Error processing JSON", ex);
            return false;
        }
    }

    public JSONObject removeField(JSONObject json, String fieldName) throws JSONException {
        String key = fieldName.split("\\.")[0];
        if (json.has(fieldName)) {
            json.remove(key);
        } else if (json.has(key)) {
            if (json.get(key) instanceof JSONObject) {
                JSONObject value = json.getJSONObject(key);
                this.removeField(value, fieldName.substring(fieldName.indexOf(".") + 1));
            } else {
                json.remove(key);
            }
        }

        return json;
    }
}