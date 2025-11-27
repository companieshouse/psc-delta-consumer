package uk.gov.companieshouse.psc.delta.matcher;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class CustomRequestMatcherTest {

    @ParameterizedTest
    @MethodSource("provideFindMismatchTestCases")
    void testFindMismatch(final String expectedJson, final String actualJson, final String expectedMismatch) throws Exception {
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expected = mapper.readTree(expectedJson);
        final JsonNode actual = mapper.readTree(actualJson);
        final Optional<String> mismatch = new CustomRequestMatcher("", "", List.of()).findMismatch(expected, actual, "");

        if (expectedMismatch == null) {
            assertThat("Mismatch result should be empty", mismatch.isEmpty(), is(true));
        } else {
            assertThat("Mismatch result should be present", mismatch.isPresent(), is(true));
            assertThat("Mismatch result", mismatch.get(), is(expectedMismatch));
        }
    }

    private static Stream<Arguments> provideFindMismatchTestCases() {
        return Stream.of(
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        """
                        {
                            "a": {
                                "b": 2
                            }
                        }
                        """,
                        "Value mismatch at: a.b (expected: 1, actual: 2)"
                ),
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        """
                        {
                            "a": {}
                        }
                        """,
                        "Missing field: a.b"
                ),
                Arguments.of(
                        """
                        [1, 2, 3]
                        """,
                        """
                        [1, 2]
                        """,
                        "Array index out of bounds at: [2]"
                ),
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        null
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideMatchBodyTestCases")
    void testMatchBody(final String expectedOutput, final String actualBody, final List<String> fieldsToIgnore,
        final boolean expectedResult) {
        final CustomRequestMatcher matcher = new CustomRequestMatcher(expectedOutput, "test-url", fieldsToIgnore);
        final boolean result = matcher.matchBody(actualBody);
        assertThat("Mismatch in matchBody result", result, is(expectedResult));
    }

    private static Stream<Arguments> provideMatchBodyTestCases() {
        return Stream.of(
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": {}
                            }
                        }
                        """,
                        """
                        {
                            "a": {
                                "b": {"c": 2}
                            }
                        }
                        """,
                        List.of("a.b.c"), true
                ),
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        """
                        {
                            "a": {
                                "b": 1,
                                "c": 2
                            }
                        }
                        """,
                        List.of("a.c"), true
                ),
                Arguments.of(
                        """
                        {
                            "a": {
                                "b": 1
                            }
                        }
                        """,
                        """
                        {
                            "a": {
                                "b": 2
                            }
                        }
                        """,
                        List.of(), false
                )
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindMismatchJsonFileTestCases")
    void testFindMismatchWithJsonFiles(final String expectedFile, final String actualFile, final String expectedMismatch) throws Exception {
        final String expectedJson = Files.readString(Path.of(expectedFile));
        final String actualJson = Files.readString(Path.of(actualFile));

        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode expectedNode = mapper.readTree(expectedJson);
        final JsonNode actualNode = mapper.readTree(actualJson);
        final CustomRequestMatcher matcher = new CustomRequestMatcher(null, null, List.of());

        final Optional<String> mismatch = matcher.findMismatch(expectedNode, actualNode, "");

        if (expectedMismatch == null) {
            assertThat(mismatch.isPresent(), is(false));
        } else {
            assertThat(mismatch.isPresent(), is(true));
            assertThat(mismatch.get(), is(expectedMismatch));
        }
    }

    private static Stream<Arguments> provideFindMismatchJsonFileTestCases() {
        return Stream.of(
            Arguments.of(
                "src/itest/resources/json/output/super_secure_entity_psc_expected_output.json",
                "src/itest/resources/json/output/actual_super_secure_entity_psc_output.json",
                "Unexpected field: internal_data.delta_at"
            ),
            Arguments.of(
                "src/itest/resources/json/output/super_secure_entity_psc_expected_output.json",
                "src/itest/resources/json/output/super_secure_entity_psc_expected_output.json",
                null
            )
        );
    }

    @ParameterizedTest
    @MethodSource("provideJsonFileTestCases")
    void testMatchBodyWithJsonFiles(final String expectedJson, final String actualJson, final List<String> fieldsToIgnore, final boolean expectedResult) {
        final CustomRequestMatcher matcher = new CustomRequestMatcher(expectedJson, "test-url", fieldsToIgnore);
        final boolean result = matcher.matchBody(actualJson);
        assertThat("Mismatch in matchBody result with JSON files", result, is(expectedResult));
    }

    private static Stream<Arguments> provideJsonFileTestCases() throws IOException {
        final String expectedJson = Files.readString(Path.of("src/itest/resources/json/output/super_secure_entity_psc_expected_output.json"));
        final String actualJson = Files.readString(Path.of("src/itest/resources/json/output/actual_super_secure_entity_psc_output.json"));

        return Stream.of(Arguments.of(expectedJson, actualJson,
                List.of("external_data.data.etag", "external_data.data.identityVerificationDetails",
                    "internal_data.delta_at"), true),
                Arguments.of(expectedJson, actualJson, List.of(), false)
        );
    }
}
