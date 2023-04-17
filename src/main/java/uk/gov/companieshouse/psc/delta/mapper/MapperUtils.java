package uk.gov.companieshouse.psc.delta.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Base64;
import java.util.Locale;

import org.apache.commons.codec.digest.DigestUtils;
import org.mapstruct.Mapper;

import uk.gov.companieshouse.psc.delta.exception.NonRetryableErrorException;

public class MapperUtils {

    private MapperUtils(){
        //utility class
    }

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
        String encodedString = Base64.getUrlEncoder().withoutPadding().encodeToString(
                DigestUtils.sha1(unencodedString + salt));
        return encodedString;
    }
}
