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
import uk.gov.companieshouse.psc.delta.mapper.CorpAndLegalMapper;
import uk.gov.companieshouse.psc.delta.mapper.GeneralMapper;
import uk.gov.companieshouse.psc.delta.mapper.IndividualMapper;

@Component
public class PscApiTransformer {

    private final GeneralMapper generalMapper;
    private final IndividualMapper individualMapper;
    private final CorpAndLegalMapper corpAndLegalMapper;

    /**
     * Constructor for the transformer.
     * @param generalMapper returns the FullRecordCompanyPSCApi
     *                      object for Super Secure PSCs and BOs.
     * @param individualMapper returns the FullRecordCompanyPSCApi
     *                         object for Individual PSCs and BOs.
     * @param corpAndLegalMapper returns the FullRecordCompanyPSCApi
     *                           object for Corp PSCs and BOs and Legal PSCs and BOs.
     */
    @Autowired
    public PscApiTransformer(GeneralMapper generalMapper, IndividualMapper individualMapper,
                             CorpAndLegalMapper corpAndLegalMapper) {
        this.generalMapper = generalMapper;
        this.individualMapper = individualMapper;
        this.corpAndLegalMapper = corpAndLegalMapper;
    }

    /**
     * Maps the psc delta to an api object.
     * @param pscDelta the CHIPS delta
     * @return the FullRecordCompanyPSCApi object
     */
    public FullRecordCompanyPSCApi transform(PscDelta pscDelta) {

        FullRecordCompanyPSCApi fullRecordCompanyPscApi = new FullRecordCompanyPSCApi();
        Psc psc = pscDelta.getPscs().get(0);

        switch (psc.getKind()) {
            case INDIVIDUAL:
                fullRecordCompanyPscApi = individualMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("individual-person-with-significant-control");
                break;
            case CORPORATE_ENTITY:
                fullRecordCompanyPscApi = corpAndLegalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("corporate-entity-person-with-significant-control");
                break;
            case LEGAL_PERSON:
                fullRecordCompanyPscApi = corpAndLegalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("legal-person-person-with-significant-control");
                break;
            case SUPER_SECURE:
                fullRecordCompanyPscApi = generalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("super-secure-person-with-significant-control");
                break;
            case INDIVIDUAL_BENEFICIAL_OWNER:
                fullRecordCompanyPscApi = individualMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("individual-beneficial-owner");
                break;
            case CORPORATE_BENEFICIAL_OWNER:
                fullRecordCompanyPscApi = corpAndLegalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("corporate-entity-beneficial-owner");
                break;
            case LEGAL_PERSON_BENEFICIAL_OWNER:
                fullRecordCompanyPscApi = corpAndLegalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("legal-person-beneficial-owner");
                break;
            case SUPER_SECURE_BENEFICIAL_OWNER:
                fullRecordCompanyPscApi = generalMapper.mapPscData(psc);
                fullRecordCompanyPscApi.getExternalData()
                                       .getData()
                                       .setKind("super-secure-beneficial-owner");
                break;
            default:

        }

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
