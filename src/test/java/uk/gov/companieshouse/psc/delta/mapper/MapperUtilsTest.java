package uk.gov.companieshouse.psc.delta.mapper;

import static java.util.Map.entry;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import consumer.exception.NonRetryableErrorException;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class MapperUtilsTest {

    @ParameterizedTest
    @MethodSource("provideNaturesOfControlMapLiteralTestCases")
    @DisplayName("Test MapperUtils.getNaturesOfControlMap with various company types and expected Map sizes")
    void testGetNaturesOfControlMapWithLiterals(final String companyNumber, final String mapName, final int expectedSize, final Map<String, String> expectedMap) {
        final Map<String, String> result = MapperUtils.getNaturesOfControlMap(companyNumber);

        assertThat("Resulting map should not be null", result, is(notNullValue()));
        assertThat("Map size should match expected for " + mapName, result.size(), is(expectedSize));
        for (final Map.Entry<String, String> entry : expectedMap.entrySet()) {
            assertThat("Map should contain expected key: " + entry.getKey(), result.containsKey(entry.getKey()), is(true));
            assertThat("Value for key should match expected: " + entry.getKey(), result.get(entry.getKey()), is(entry.getValue()));
        }
    }

    private static Stream<Arguments> provideNaturesOfControlMapLiteralTestCases() {
        // Explicit types used here for Map.ofEntries to prevent potential compilation slowdown due to the large number of vararg parameters
        final var defaultMap = Map.<String, String>ofEntries(
                entry("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON", "ownership-of-shares-25-to-50-percent"),
                entry("OWNERSHIPOFSHARES_50TO75PERCENT_AS_PERSON", "ownership-of-shares-50-to-75-percent"),
                entry("OWNERSHIPOFSHARES_75TO100PERCENT_AS_PERSON", "ownership-of-shares-75-to-100-percent"),
                entry("OWNERSHIPOFSHARES_25TO50PERCENT_AS_TRUST", "ownership-of-shares-25-to-50-percent-as-trust"),
                entry("OWNERSHIPOFSHARES_50TO75PERCENT_AS_TRUST", "ownership-of-shares-50-to-75-percent-as-trust"),
                entry("OWNERSHIPOFSHARES_75TO100PERCENT_AS_TRUST", "ownership-of-shares-75-to-100-percent-as-trust"),
                entry("OWNERSHIPOFSHARES_25TO50PERCENT_AS_FIRM", "ownership-of-shares-25-to-50-percent-as-firm"),
                entry("OWNERSHIPOFSHARES_50TO75PERCENT_AS_FIRM", "ownership-of-shares-50-to-75-percent-as-firm"),
                entry("OWNERSHIPOFSHARES_75TO100PERCENT_AS_FIRM", "ownership-of-shares-75-to-100-percent-as-firm"),
                entry("VOTINGRIGHTS_25TO50PERCENT_AS_PERSON", "voting-rights-25-to-50-percent"),
                entry("VOTINGRIGHTS_50TO75PERCENT_AS_PERSON", "voting-rights-50-to-75-percent"),
                entry("VOTINGRIGHTS_75TO100PERCENT_AS_PERSON", "voting-rights-75-to-100-percent"),
                entry("VOTINGRIGHTS_25TO50PERCENT_AS_TRUST", "voting-rights-25-to-50-percent-as-trust"),
                entry("VOTINGRIGHTS_50TO75PERCENT_AS_TRUST", "voting-rights-50-to-75-percent-as-trust"),
                entry("VOTINGRIGHTS_75TO100PERCENT_AS_TRUST", "voting-rights-75-to-100-percent-as-trust"),
                entry("VOTINGRIGHTS_25TO50PERCENT_AS_FIRM", "voting-rights-25-to-50-percent-as-firm"),
                entry("VOTINGRIGHTS_50TO75PERCENT_AS_FIRM", "voting-rights-50-to-75-percent-as-firm"),
                entry("VOTINGRIGHTS_75TO100PERCENT_AS_FIRM", "voting-rights-75-to-100-percent-as-firm"),
                entry("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_PERSON", "right-to-appoint-and-remove-directors"),
                entry("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_TRUST", "right-to-appoint-and-remove-directors-as-trust"),
                entry("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_FIRM", "right-to-appoint-and-remove-directors-as-firm"),
                entry("SIGINFLUENCECONTROL_AS_PERSON", "significant-influence-or-control"),
                entry("SIGINFLUENCECONTROL_AS_TRUST", "significant-influence-or-control-as-trust"),
                entry("SIGINFLUENCECONTROL_AS_FIRM", "significant-influence-or-control-as-firm"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_PERSON", "part-right-to-share-surplus-assets-25-to-50-percent"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_PERSON", "part-right-to-share-surplus-assets-50-to-75-percent"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_PERSON", "part-right-to-share-surplus-assets-75-to-100-percent"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_TRUST", "part-right-to-share-surplus-assets-25-to-50-percent-as-trust"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_TRUST", "part-right-to-share-surplus-assets-50-to-75-percent-as-trust"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_TRUST", "part-right-to-share-surplus-assets-75-to-100-percent-as-trust"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM", "part-right-to-share-surplus-assets-25-to-50-percent-as-firm"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_FIRM", "part-right-to-share-surplus-assets-50-to-75-percent-as-firm"),
                entry("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_FIRM", "part-right-to-share-surplus-assets-75-to-100-percent-as-firm"),
                entry("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_PERSON", "right-to-appoint-and-remove-person"),
                entry("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_FIRM", "right-to-appoint-and-remove-person-as-firm"),
                entry("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_TRUST", "right-to-appoint-and-remove-person-as-trust"),
                entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_PERSON", "ownership-of-shares-more-than-25-percent-registered-overseas-entity"),
                entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_TRUST", "ownership-of-shares-more-than-25-percent-as-trust-registered-overseas-entity"),
                entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_FIRM", "ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity"),
                entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_PERSON", "voting-rights-more-than-25-percent-registered-overseas-entity"),
                entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_TRUST", "voting-rights-more-than-25-percent-as-trust-registered-overseas-entity"),
                entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_FIRM", "voting-rights-more-than-25-percent-as-firm-registered-overseas-entity"),
                entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_PERSON", "right-to-appoint-and-remove-directors-registered-overseas-entity"),
                entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_TRUST", "right-to-appoint-and-remove-directors-as-trust-registered-overseas-entity"),
                entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_FIRM", "right-to-appoint-and-remove-directors-as-firm-registered-overseas-entity"),
                entry("OE_SIGINFLUENCECONTROL_AS_PERSON", "significant-influence-or-control-registered-overseas-entity"),
                entry("OE_SIGINFLUENCECONTROL_AS_TRUST", "significant-influence-or-control-as-trust-registered-overseas-entity"),
                entry("OE_SIGINFLUENCECONTROL_AS_FIRM", "significant-influence-or-control-as-firm-registered-overseas-entity"),
                entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_PERSON", "right-to-share-surplus-assets-25-to-50-percent-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_PERSON", "right-to-share-surplus-assets-50-to-75-percent-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_PERSON", "right-to-share-surplus-assets-75-to-100-percent-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_TRUST", "right-to-share-surplus-assets-25-to-50-percent-as-trust-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_TRUST", "right-to-share-surplus-assets-50-to-75-percent-as-trust-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_TRUST", "right-to-share-surplus-assets-75-to-100-percent-as-trust-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM", "right-to-share-surplus-assets-25-to-50-percent-as-firm-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_FIRM", "right-to-share-surplus-assets-50-to-75-percent-as-firm-limited-liability-partnership"),
                entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_FIRM", "right-to-share-surplus-assets-75-to-100-percent-as-firm-limited-liability-partnership"),
                entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_PERSON", "right-to-appoint-and-remove-members-limited-liability-partnership"),
                entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_FIRM", "right-to-appoint-and-remove-members-as-firm-limited-liability-partnership"),
                entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_TRUST", "right-to-appoint-and-remove-members-as-trust-limited-liability-partnership"),
                entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_CONTROLOVERTRUST", "ownership-of-shares-more-than-25-percent-as-control-over-trust-registered-overseas-entity"),
                entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_CONTROLOVERTRUST", "voting-rights-more-than-25-percent-as-control-over-trust-registered-overseas-entity"),
                entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_CONTROLOVERTRUST", "right-to-appoint-and-remove-directors-as-control-over-trust-registered-overseas-entity"),
                entry("OE_SIGINFLUENCECONTROL_AS_CONTROLOVERTRUST", "significant-influence-or-control-as-control-over-trust-registered-overseas-entity"),
                entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_CONTROLOVERFIRM", "ownership-of-shares-more-than-25-percent-as-control-over-firm-registered-overseas-entity"),
                entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_CONTROLOVERFIRM", "voting-rights-more-than-25-percent-as-control-over-firm-registered-overseas-entity"),
                entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_CONTROLOVERFIRM", "right-to-appoint-and-remove-directors-as-control-over-firm-registered-overseas-entity"),
                entry("OE_SIGINFLUENCECONTROL_AS_CONTROLOVERFIRM", "significant-influence-or-control-as-control-over-firm-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEPERSON_ENGLANDWALES", "registered-owner-as-nominee-person-england-wales-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEPERSON_SCOTLAND", "registered-owner-as-nominee-person-scotland-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEPERSON_NORTHERNIRELAND", "registered-owner-as-nominee-person-northern-ireland-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_ENGLANDWALES", "registered-owner-as-nominee-another-entity-england-wales-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_SCOTLAND", "registered-owner-as-nominee-another-entity-scotland-registered-overseas-entity"),
                entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_NORTHERNIRELAND", "registered-owner-as-nominee-another-entity-northern-ireland-registered-overseas-entity")
        );

        final var llpMap = Map.ofEntries(
            entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_PERSON", "right-to-share-surplus-assets-25-to-50-percent-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_PERSON", "right-to-share-surplus-assets-50-to-75-percent-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_PERSON", "right-to-share-surplus-assets-75-to-100-percent-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_TRUST", "right-to-share-surplus-assets-25-to-50-percent-as-trust-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_TRUST", "right-to-share-surplus-assets-50-to-75-percent-as-trust-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_TRUST", "right-to-share-surplus-assets-75-to-100-percent-as-trust-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM", "right-to-share-surplus-assets-25-to-50-percent-as-firm-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_FIRM", "right-to-share-surplus-assets-50-to-75-percent-as-firm-limited-liability-partnership"),
            entry("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_FIRM", "right-to-share-surplus-assets-75-to-100-percent-as-firm-limited-liability-partnership"),
            entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_PERSON", "right-to-appoint-and-remove-members-limited-liability-partnership"),
            entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_FIRM", "right-to-appoint-and-remove-members-as-firm-limited-liability-partnership"),
            entry("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_TRUST", "right-to-appoint-and-remove-members-as-trust-limited-liability-partnership"),
            entry("VOTINGRIGHTS_25TO50PERCENT_AS_PERSON", "voting-rights-25-to-50-percent-limited-liability-partnership"),
            entry("VOTINGRIGHTS_50TO75PERCENT_AS_PERSON", "voting-rights-50-to-75-percent-limited-liability-partnership"),
            entry("VOTINGRIGHTS_75TO100PERCENT_AS_PERSON", "voting-rights-75-to-100-percent-limited-liability-partnership"),
            entry("VOTINGRIGHTS_25TO50PERCENT_AS_TRUST", "voting-rights-25-to-50-percent-as-trust-limited-liability-partnership"),
            entry("VOTINGRIGHTS_50TO75PERCENT_AS_TRUST", "voting-rights-50-to-75-percent-as-trust-limited-liability-partnership"),
            entry("VOTINGRIGHTS_75TO100PERCENT_AS_TRUST", "voting-rights-75-to-100-percent-as-trust-limited-liability-partnership"),
            entry("VOTINGRIGHTS_25TO50PERCENT_AS_FIRM", "voting-rights-25-to-50-percent-as-firm-limited-liability-partnership"),
            entry("VOTINGRIGHTS_50TO75PERCENT_AS_FIRM", "voting-rights-50-to-75-percent-as-firm-limited-liability-partnership"),
            entry("VOTINGRIGHTS_75TO100PERCENT_AS_FIRM", "voting-rights-75-to-100-percent-as-firm-limited-liability-partnership"),
            entry("SIGINFLUENCECONTROL_AS_PERSON", "significant-influence-or-control-limited-liability-partnership"),
            entry("SIGINFLUENCECONTROL_AS_TRUST", "significant-influence-or-control-as-trust-limited-liability-partnership"),
            entry("SIGINFLUENCECONTROL_AS_FIRM", "significant-influence-or-control-as-firm-limited-liability-partnership")
        );

        final var roeMap = Map.ofEntries(
            entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_PERSON", "ownership-of-shares-more-than-25-percent-registered-overseas-entity"),
            entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_TRUST", "ownership-of-shares-more-than-25-percent-as-trust-registered-overseas-entity"),
            entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_FIRM", "ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity"),
            entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_PERSON", "voting-rights-more-than-25-percent-registered-overseas-entity"),
            entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_TRUST", "voting-rights-more-than-25-percent-as-trust-registered-overseas-entity"),
            entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_FIRM", "voting-rights-more-than-25-percent-as-firm-registered-overseas-entity"),
            entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_PERSON", "right-to-appoint-and-remove-directors-registered-overseas-entity"),
            entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_TRUST", "right-to-appoint-and-remove-directors-as-trust-registered-overseas-entity"),
            entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_FIRM", "right-to-appoint-and-remove-directors-as-firm-registered-overseas-entity"),
            entry("OE_SIGINFLUENCECONTROL_AS_PERSON", "significant-influence-or-control-registered-overseas-entity"),
            entry("OE_SIGINFLUENCECONTROL_AS_TRUST", "significant-influence-or-control-as-trust-registered-overseas-entity"),
            entry("OE_SIGINFLUENCECONTROL_AS_FIRM", "significant-influence-or-control-as-firm-registered-overseas-entity"),
            entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_CONTROLOVERTRUST", "ownership-of-shares-more-than-25-percent-as-control-over-trust-registered-overseas-entity"),
            entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_CONTROLOVERTRUST", "voting-rights-more-than-25-percent-as-control-over-trust-registered-overseas-entity"),
            entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_CONTROLOVERTRUST", "right-to-appoint-and-remove-directors-as-control-over-trust-registered-overseas-entity"),
            entry("OE_SIGINFLUENCECONTROL_AS_CONTROLOVERTRUST", "significant-influence-or-control-as-control-over-trust-registered-overseas-entity"),
            entry("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_CONTROLOVERFIRM", "ownership-of-shares-more-than-25-percent-as-control-over-firm-registered-overseas-entity"),
            entry("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_CONTROLOVERFIRM", "voting-rights-more-than-25-percent-as-control-over-firm-registered-overseas-entity"),
            entry("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_CONTROLOVERFIRM", "right-to-appoint-and-remove-directors-as-control-over-firm-registered-overseas-entity"),
            entry("OE_SIGINFLUENCECONTROL_AS_CONTROLOVERFIRM", "significant-influence-or-control-as-control-over-firm-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEPERSON_ENGLANDWALES", "registered-owner-as-nominee-person-england-wales-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEPERSON_SCOTLAND", "registered-owner-as-nominee-person-scotland-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEPERSON_NORTHERNIRELAND", "registered-owner-as-nominee-person-northern-ireland-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_ENGLANDWALES", "registered-owner-as-nominee-another-entity-england-wales-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_SCOTLAND", "registered-owner-as-nominee-another-entity-scotland-registered-overseas-entity"),
            entry("OE_REGOWNER_AS_NOMINEEANOTHERENTITY_NORTHERNIRELAND", "registered-owner-as-nominee-another-entity-northern-ireland-registered-overseas-entity")
        );

        return Stream.of(
                Arguments.of("A", "defaultMap", defaultMap.size(), defaultMap),
                Arguments.of("SO123456", "llpMap", llpMap.size(), llpMap),
                Arguments.of("NC123456", "llpMap", llpMap.size(), llpMap),
                Arguments.of("OC123456", "llpMap", llpMap.size(), llpMap),
                Arguments.of("OE123456", "roeMap", roeMap.size(), roeMap),
                Arguments.of("XX123456", "defaultMap", defaultMap.size(), defaultMap)
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseLocalDateTestCases")
    @DisplayName("Test MapperUtils.parseLocalDate with various date strings")
    void testParseLocalDate(final String inputDate, final LocalDate expectedDate) {
        final LocalDate result = MapperUtils.parseLocalDate(inputDate);
        assertThat("Parsed date should match expected value.", result, is(expectedDate));
    }

    private static Stream<Arguments> provideParseLocalDateTestCases() {
        return Stream.of(
                Arguments.of("20251121", LocalDate.of(2025, 11, 21)),
                Arguments.of("20000101", LocalDate.of(2000, 1, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("provideParseLocalDateFailureTestCases")
    @DisplayName("Test MapperUtils.parseLocalDate with invalid date strings throwing NonRetryableErrorException")
    void testParseLocalDateFailures(final String inputDate, final String expectedMessage) {
        final NonRetryableErrorException exception = assertThrows(NonRetryableErrorException.class,
            () -> MapperUtils.parseLocalDate(inputDate));
        assertThat(exception.getMessage(), is(expectedMessage));
    }

    private static Stream<Arguments> provideParseLocalDateFailureTestCases() {
        return Stream.of(
                Arguments.of("invalid-date", "Failed to parse date/time: [invalid-date000000]"),
                Arguments.of("20251301", "Failed to parse date/time: [20251301000000]"),
                Arguments.of("20250010", "Failed to parse date/time: [20250010000000]"),
                Arguments.of("20251132", "Failed to parse date/time: [20251132000000]"),
                Arguments.of("abcd1234", "Failed to parse date/time: [abcd1234000000]"),
                Arguments.of("202511/21", "Failed to parse date/time: [202511/21000000]"),
                Arguments.of("21112025", "Failed to parse date/time: [21112025000000]")
        );
    }

    @ParameterizedTest
    @MethodSource("provideEncodeTestCases")
    @DisplayName("Test MapperUtils.encode with various input strings")
    void testEncode(final String input, final String expectedEncoded) {
        final String result = MapperUtils.encode(input);
        assertThat("Encoded string should match expected value.", result, is(expectedEncoded));
    }

    private static Stream<Arguments> provideEncodeTestCases() {
        return Stream.of(
            Arguments.of("test", "mXm6nVCBXNugvCMFGUFjgHTubIQ"),
            Arguments.of("hello", "aMHeRPHq2jCw943eLXcYLPJ4scI"),
            Arguments.of("", "S9xjyoZ5j2rg95i-sVo9Gv15Jmc"),
            Arguments.of(null, "vuIAhYYbRDhqzx9b3e_jd6Uhres")
        );
    }

}
