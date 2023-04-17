package uk.gov.companieshouse.psc.delta.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.api.delta.NameElements;
import uk.gov.companieshouse.api.delta.Psc.ResidentialAddressSameAsServiceAddressEnum;
import uk.gov.companieshouse.api.delta.Psc.ServiceAddressSameAsRegisteredOfficeEnum;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.SensitiveData;

@Mapper(componentModel = "spring", uses = {GeneralMapper.class})
public interface IndividualMapper {
    @Mapping(target = "externalData.pscId", source = "pscId", ignore = true)
    @Mapping(target = "externalData.data.notifiedOn", source = "notificationDate",
            dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.serviceAddress", source = "address")
    @Mapping(target = "externalData.data.nameElements", source = "nameElements")
    @Mapping(target = "externalData.data.nationality", source = "nationality")
    @Mapping(target = "externalData.data.countryOfResidence", source = "countryOfResidence")
    @Mapping(target = "externalData.data.naturesOfControl", source = "naturesOfControl")
    @Mapping(target = "externalData.data.serviceAddressSameAsRegisteredOfficeAddress",
            source = "serviceAddressSameAsRegisteredOffice", ignore = true)
    @Mapping(target = "externalData.data.residentialAddressIsSameAsServiceAddress",
            source = "residentialAddressSameAsServiceAddress", ignore = true)
    @Mapping(target = "externalData.sensitiveData.dateOfBirth", ignore = true)
    @Mapping(target = "externalData.sensitiveData.usualResidentialAddress",
            source = "usualResidentialAddress")
    FullRecordCompanyPSCApi mapPscData(Psc psc);

    /** encode psc_id. */
    @AfterMapping
    default void mapEncodedPscId(@MappingTarget ExternalData target, Psc source) {
        target.setPscId(MapperUtils.encode(source.getPscId()));
    }

    /**
     * Manually map Date of Birth.
     * @param target SensitiveData object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapDateOfBirth(@MappingTarget SensitiveData target, Psc source) {

        DateOfBirth dateOfBirth = new DateOfBirth();
        String year = String.valueOf((MapperUtils
                                      .parseLocalDate(source.getDateOfBirth()))
                                      .getYear());
        String month = String.valueOf((MapperUtils
                                       .parseLocalDate(source.getDateOfBirth()))
                                       .getMonthValue());
        String day = String.valueOf((MapperUtils
                                     .parseLocalDate(source.getDateOfBirth()))
                                     .getDayOfMonth());

        dateOfBirth.setYear(Integer.parseInt(year));
        dateOfBirth.setMonth(Integer.parseInt(month));
        dateOfBirth.setDay(Integer.parseInt(day));

        target.setDateOfBirth(dateOfBirth);
    }

    /**
     * Manually map ServiceAddressSameAsRegisteredOffice.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapRegisteredOfficeEnum(@MappingTarget Data target, Psc source) {
        ServiceAddressSameAsRegisteredOfficeEnum registeredOfficeEnum =
                source.getServiceAddressSameAsRegisteredOffice();

        if (registeredOfficeEnum.toString().equals("Y")) {
            target.setServiceAddressSameAsRegisteredOfficeAddress(Boolean.TRUE);
        } else {
            target.setServiceAddressSameAsRegisteredOfficeAddress(Boolean.FALSE);
        }
    }

    /**
     * Manually map ResidentialAddressSameAsServiceAddress.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapServiceAddressEnum(@MappingTarget Data target, Psc source) {
        ResidentialAddressSameAsServiceAddressEnum serviceAddressEnum =
                source.getResidentialAddressSameAsServiceAddress();

        if (serviceAddressEnum.toString().equals("Y")) {
            target.setResidentialAddressIsSameAsServiceAddress(Boolean.TRUE);
        } else {
            target.setResidentialAddressIsSameAsServiceAddress(Boolean.FALSE);
        }
    }
}
