package uk.gov.companieshouse.psc.delta.mapper;

import consumer.exception.NonRetryableErrorException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Component;

@Component
public class MapperUtils {

    public static final String TIME_START_OF_DAY = "000000";
    public static final String DATE_PATTERN = "yyyyMMdd";
    public static final String DATETIME_PATTERN = "yyyyMMddHHmmss";
    public static final DateTimeFormatter UTC_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.UK).withZone(ZoneId.of("UTC"));

    /**
     * Parse a date string (expected format: DATE_PATTERN).
     * Implementation note: Instant conversion requires input level of detail is Seconds,
     * so TIME_START_OF_DAY is appended before conversion.
     *
     * @param rawDateString the date string
     * @return the LocalDate corresponding to the parsed string (at UTC by definition)
     * @throws NonRetryableErrorException if date parsing fails
     */

    public static LocalDate parseLocalDate(String rawDateString)
            throws NonRetryableErrorException {
        return convertToLocalDate(rawDateString + TIME_START_OF_DAY, DATE_PATTERN);
    }

    private static LocalDate convertToLocalDate(final String s, final String effectivePattern)
            throws NonRetryableErrorException {
        try {
            return LocalDate.parse(s, UTC_DATETIME_FORMATTER);
        } catch (DateTimeParseException exception) {
            throw new NonRetryableErrorException(String.format("%s: date/time pattern not matched:"
                    + " [%s]", effectivePattern), null);
        }
    }

    /**
     * encode the String passed in for use in links and ids.
     */
    public static String encode(String unencodedString) {
        String salt = "ks734s_sdgOc4£b2";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(unencodedString + salt));
    }
    /**
     * Create a hashmap for natures of control.
     */

    public static HashMap<String,String> getNaturesOfControlMap() {

        HashMap<String,String> naturesOfControlMap = new HashMap<>();

        naturesOfControlMap.put("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON",
                "ownership-of-shares-25-to-50-percent");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_50TO75PERCENT_AS_PERSON",
                "ownership-of-shares-50-to-75-percent");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_75TO100PERCENT_AS_PERSON",
                "ownership-of-shares-75-to-100-percent");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_25TO50PERCENT_AS_TRUST" ,
                "ownership-of-shares-25-to-50-percent-as-trust");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_50TO75PERCENT_AS_TRUST" ,
                "ownership-of-shares-50-to-75-percent-as-trust");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_75TO100PERCENT_AS_TRUST" ,
                "ownership-of-shares-75-to-100-percent-as-trust");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_25TO50PERCENT_AS_FIRM" ,
                "ownership-of-shares-25-to-50-percent-as-firm");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_50TO75PERCENT_AS_FIRM" ,
                "ownership-of-shares-50-to-75-percent-as-firm");
        naturesOfControlMap.put("OWNERSHIPOFSHARES_75TO100PERCENT_AS_FIRM" ,
                "ownership-of-shares-75-to-100-percent-as-firm");
        naturesOfControlMap.put("VOTINGRIGHTS_25TO50PERCENT_AS_PERSON" ,
                "voting-rights-25-to-50-percent");
        naturesOfControlMap.put("VOTINGRIGHTS_50TO75PERCENT_AS_PERSON" ,
                "voting-rights-50-to-75-percent");
        naturesOfControlMap.put("VOTINGRIGHTS_75TO100PERCENT_AS_PERSON" ,
                "voting-rights-75-to-100-percent");
        naturesOfControlMap.put("VOTINGRIGHTS_25TO50PERCENT_AS_TRUST" ,
                "voting-rights-25-to-50-percent-as-trust");
        naturesOfControlMap.put("VOTINGRIGHTS_50TO75PERCENT_AS_TRUST" ,
                "voting-rights-50-to-75-percent-as-trust");
        naturesOfControlMap.put("VOTINGRIGHTS_75TO100PERCENT_AS_TRUST" ,
                "voting-rights-75-to-100-percent-as-trust");
        naturesOfControlMap.put("VOTINGRIGHTS_25TO50PERCENT_AS_FIRM" ,
                "voting-rights-25-to-50-percent-as-firm");
        naturesOfControlMap.put("VOTINGRIGHTS_50TO75PERCENT_AS_FIRM" ,
                "voting-rights-50-to-75-percent-as-firm");
        naturesOfControlMap.put("VOTINGRIGHTS_75TO100PERCENT_AS_FIRM" ,
                "voting-rights-75-to-100-percent-as-firm");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_PERSON" ,
                "right-to-appoint-and-remove-directors");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_TRUST",
                "right-to-appoint-and-remove-directors-as-trust");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_FIRM" ,
                "right-to-appoint-and-remove-directors-as-firm");
        naturesOfControlMap.put("SIGINFLUENCECONTROL_AS_PERSON",
                "significant-influence-or-control");
        naturesOfControlMap.put("SIGINFLUENCECONTROL_AS_TRUST",
                "significant-influence-or-control-as-trust");
        naturesOfControlMap.put("SIGINFLUENCECONTROL_AS_FIRM",
                "significant-influence-or-control-as-firm");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_PERSON",
                "part-right-to-share-surplus-assets-25-to-50-percent");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_PERSON",
                "part-right-to-share-surplus-assets-50-to-75-percent");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_PERSON",
                "part-right-to-share-surplus-assets-75-to-100-percent");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_TRUST",
                "part-right-to-share-surplus-assets-25-to-50-percent-as-trust");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_TRUST",
                "part-right-to-share-surplus-assets-50-to-75-percent-as-trust");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_TRUST",
                "part-right-to-share-surplus-assets-75-to-100-percent-as-trust");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM",
                "part-right-to-share-surplus-assets-25-to-50-percent-as-firm");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_FIRM",
                "part-right-to-share-surplus-assets-50-to-75-percent-as-firm");
        naturesOfControlMap.put("PART_RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_FIRM",
                "part-right-to-share-surplus-assets-75-to-100-percent-as-firm");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_PERSON",
                "right-to-appoint-and-remove-person");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_FIRM",
                "right-to-appoint-and-remove-person-as-firm");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEPERSONS_AS_TRUST",
                "right-to-appoint-and-remove-person-as-trust");
        naturesOfControlMap.put("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_PERSON",
                "ownership-of-shares-more-than-25-percent-registered-overseas-entity");
        naturesOfControlMap.put("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_TRUST",
                "ownership-of-shares-more-than-25-percent-as-trust-registered-overseas-entity");
        naturesOfControlMap.put("OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_FIRM",
                "ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity");
        naturesOfControlMap.put("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_PERSON",
                "voting-rights-more-than-25-percent-registered-overseas-entity");
        naturesOfControlMap.put("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_TRUST",
                "voting-rights-more-than-25-percent-as-trust-registered-overseas-entity");
        naturesOfControlMap.put("OE_VOTINGRIGHTS_MORETHAN25PERCENT_AS_FIRM",
                "voting-rights-more-than-25-percent-as-firm-registered-overseas-entity");
        naturesOfControlMap.put("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_PERSON",
                "right-to-appoint-and-remove-directors-registered-overseas-entity");
        naturesOfControlMap.put("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_TRUST",
                "right-to-appoint-and-remove-directors-as-trust-registered-overseas-entity");
        naturesOfControlMap.put("OE_RIGHTTOAPPOINTANDREMOVEDIRECTORS_AS_FIRM",
                "right-to-appoint-and-remove-directors-as-firm-registered-overseas-entity");
        naturesOfControlMap.put("OE_SIGINFLUENCECONTROL_AS_PERSON",
                "significant-influence-or-control-registered-overseas-entity");
        naturesOfControlMap.put("OE_SIGINFLUENCECONTROL_AS_TRUST",
                "significant-influence-or-control-as-trust-registered-overseas-entity");
        naturesOfControlMap.put("OE_SIGINFLUENCECONTROL_AS_FIRM",
                "significant-influence-or-control-as-firm-registered-overseas-entity");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_PERSON",
                "right-to-share-surplus-assets-25-to-50-percent-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_PERSON",
                "right-to-share-surplus-assets-50-to-75-percent-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_PERSON",
                "right-to-share-surplus-assets-75-to-100-percent-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_TRUST",
                "right-to-share-surplus-assets-25-to-50-percent"
                        + "-as-trust-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_TRUST",
                "right-to-share-surplus-assets-50-to-75-percent"
                        + "-as-trust-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_TRUST",
                "right-to-share-surplus-assets-75-to-100-percent"
                        + "-as-trust-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM",
                "right-to-share-surplus-assets-25-to-50-percent"
                        + "-as-firm-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_50TO75PERCENT_AS_FIRM",
                "right-to-share-surplus-assets-50-to-75-percent"
                        + "-as-firm-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOSHARESURPLUSASSETS_75TO100PERCENT_AS_FIRM",
                "right-to-share-surplus-assets-75-to-100-percent"
                        + "-as-firm-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_PERSON" ,
                "right-to-appoint-and-remove-members-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_FIRM",
                "right-to-appoint-and-remove-members-as-firm-limited-liability-partnership");
        naturesOfControlMap.put("RIGHTTOAPPOINTANDREMOVEMEMBERS_AS_TRUST",
                "right-to-appoint-and-remove-members-as-trust-limited-liability-partnership");

        return naturesOfControlMap;
    }
}
