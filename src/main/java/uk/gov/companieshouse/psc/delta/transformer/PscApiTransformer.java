package uk.gov.companieshouse.psc.delta.transformer;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.InternalData;
import uk.gov.companieshouse.psc.delta.mapper.PscMapper;

@Component
public class PscApiTransformer {

    private final PscMapper pscMapper;

    /**
     * Constructor for the transformer.
     * @param pscMapper returns the FullRecordCompanyPSCApi
     *                      object for Super Secure PSCs and BOs.
     */
    @Autowired
    public PscApiTransformer(PscMapper pscMapper) {
        this.pscMapper = pscMapper;
    }

    /**
     * Maps the psc delta to an api object.
     * @param pscDelta the CHIPS delta
     * @return the FullRecordCompanyPSCApi object
     */
    public FullRecordCompanyPSCApi transform(PscDelta pscDelta) {

        Psc psc = pscDelta.getPscs().get(0);

        FullRecordCompanyPSCApi fullRecordCompanyPscApi = pscMapper.mapPscData(psc);

        return parseDeltaAt(fullRecordCompanyPscApi, pscDelta);
    }

    /**
     * Maps the delta at which is not passed into the mapper.
     * @param apiObject the FullRecordCompanyPSCApi object from the mapper.
     * @param pscDelta the delta from CHIPS containing the delta_at
     * @return the original api object with the delta_at parsed.
     */
    private FullRecordCompanyPSCApi parseDeltaAt(
            FullRecordCompanyPSCApi apiObject,
            PscDelta pscDelta) {
        InternalData internalData = new
                InternalData();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSSSSS")
                .withZone(ZoneId.of("Z"));
        ZonedDateTime datetime = ZonedDateTime.parse(pscDelta.getDeltaAt(), formatter);
        internalData.setDeltaAt(datetime.toOffsetDateTime());

        apiObject.setInternalData(internalData);
        return apiObject;
    }
}
