package uk.gov.companieshouse.psc.delta.matcher;

import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.matching.MatchResult;
import com.github.tomakehurst.wiremock.matching.ValueMatcher;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
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

    public CustomRequestMatcher(final String output, final String expectedUrl, final List<String> fieldsToIgnore) {
        this.expectedOutput = output;
        this.expectedUrl = expectedUrl;
        this.fieldsToIgnore = fieldsToIgnore;
    }

    @Override
    public MatchResult match(final Request request) {
        return MatchResult.of(
                this.matchUrl(request.getUrl()) &&
                        this.matchMethod(request.getMethod().toString()) &&
                        this.matchBody(request.getBodyAsString()));
    }

    private boolean matchUrl(final String actualUrl) {
        final boolean urlResult = this.expectedUrl.equals(actualUrl);
        if (!urlResult) {
            LOGGER.error("URL does not match - expected: [%s], actual: [%s]".formatted(expectedUrl, actualUrl));
        }

        return urlResult;
    }

    private boolean matchMethod(final String actualMethod) {
        final boolean typeResult = PUT.equals(actualMethod);
        if (!typeResult) {
            LOGGER.error("Method does not match - expected: [%s], actual: [%s]".formatted(PUT, actualMethod));
        }

        return typeResult;
    }

    boolean matchBody(final String actualBody) {
        try {
            final JSONObject expectedBody = new JSONObject(this.expectedOutput);
            final JSONObject actual = new JSONObject(actualBody);

            // Remove fields to ignore from the actual JSON
            this.fieldsToIgnore.forEach((fieldName) -> {
                try {
                    this.removeField(actual, fieldName);
                } catch (final JSONException e) {
                    throw new RuntimeException(e);
                }
            });

            final ObjectMapper mapper = new ObjectMapper();
            final JsonNode expectedNode = mapper.readTree(expectedBody.toString());
            final JsonNode actualNode = mapper.readTree(actual.toString());

            // Compare JSON nodes and log mismatches
            final Optional<String> mismatchLocation = findMismatch(expectedNode, actualNode, "");
            if (mismatchLocation.isPresent()) {
                LOGGER.error("Body mismatch at: %s".formatted(mismatchLocation.get()));
                return false;
            }

            return true;
        } catch (final JSONException e) {
            LOGGER.error("Error processing JSON", e);
            return false;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    Optional<String> findMismatch(final JsonNode expected, final JsonNode actual, final String path) {
        if (!expected.equals(actual)) {
            if (expected.isObject() && actual.isObject()) {
                final Iterator<String> fieldNames = expected.fieldNames();
                while (fieldNames.hasNext()) {
                    final String fieldName = fieldNames.next();
                    final String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    if (!actual.has(fieldName)) {
                        return Optional.of("Missing field: " + fieldPath);
                    }
                    final Optional<String> mismatch = findMismatch(expected.get(fieldName), actual.get(fieldName), fieldPath);
                    if (mismatch.isPresent()) {
                        return mismatch;
                    }
                }
                // Check for extra fields in the actual JsonNode
                final Iterator<String> actualFieldNames = actual.fieldNames();
                while (actualFieldNames.hasNext()) {
                    final String fieldName = actualFieldNames.next();
                    final String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;
                    if (!expected.has(fieldName)) {
                        return Optional.of("Unexpected field: " + fieldPath);
                    }
                }
            } else if (expected.isArray() && actual.isArray()) {
                for (int i = 0; i < expected.size(); i++) {
                    if (i >= actual.size()) {
                        return Optional.of("Array index out of bounds at: " + path + "[" + i + "]");
                    }
                    final Optional<String> mismatch = findMismatch(expected.get(i), actual.get(i), path + "[" + i + "]");
                    if (mismatch.isPresent()) {
                        return mismatch;
                    }
                }
            } else {
                return Optional.of("Value mismatch at: " + path + " (expected: " + expected + ", actual: " + actual + ")");
            }
        }
        return Optional.empty();
    }

    private void removeField(final JSONObject json, final String fieldName) throws JSONException {
        final String[] parts = fieldName.split("\\.");
        final String key = parts[0];

        if (parts.length == 1) {
            // Base case: Remove the field if it exists at the current level
            json.remove(key);
        } else if (json.has(key) && json.get(key) instanceof JSONObject) {
            // Recursive case: Traverse into the nested object
            final JSONObject nestedObject = json.getJSONObject(key);
            removeField(nestedObject, String.join(".", Arrays.stream(parts).skip(1).toArray(String[]::new)));
        }
    }
}
