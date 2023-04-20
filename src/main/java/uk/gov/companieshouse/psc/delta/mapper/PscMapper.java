package uk.gov.companieshouse.psc.delta.mapper;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.GenerateEtagUtil;

import uk.gov.companieshouse.api.delta.NameElements;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.SensitiveData;

@Mapper(componentModel = "spring")
public interface PscMapper {

    @Mapping(target = "externalData.internalId", source = "internalId")
    @Mapping(target = "externalData.notificationId", source = "internalId")
    @Mapping(target = "externalData.companyNumber", source = "companyNumber")
    @Mapping(target = "externalData.id", source = "internalId")
    @Mapping(target = "externalData.data.etag", ignore = true)
    @Mapping(target = "externalData.data.name", ignore = true)
    @Mapping(target = "externalData.data.links", ignore = true)
    @Mapping(target = "externalData.data.kind", source = "kind", ignore = true)
    @Mapping(target = "externalData.data.ceasedOn", source = "ceasedOn", ignore = true)

    @Mapping(target = "externalData.pscId", source = "pscId", ignore = true)
    @Mapping(target = "externalData.data.notifiedOn", source = "notificationDate",
            dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.notificationDate", source = "notificationDate",
            dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.serviceAddress", source = "address")
    @Mapping(target = "externalData.data.nameElements", source = "nameElements")
    @Mapping(target = "externalData.data.nationality", source = "nationality")
    @Mapping(target = "externalData.data.countryOfResidence", source = "countryOfResidence")
    @Mapping(target = "externalData.data.naturesOfControl", source = "naturesOfControl")
    @Mapping(target = "externalData.data.serviceAddressSameAsRegisteredOfficeAddress",
            source = "serviceAddressSameAsRegisteredOffice", ignore = true)
    @Mapping(target = "externalData.sensitiveData.residentialAddressSameAsServiceAddress",
            source = "residentialAddressSameAsServiceAddress", ignore = true)
    @Mapping(target = "externalData.sensitiveData.dateOfBirth", ignore = true)
    @Mapping(target = "externalData.sensitiveData.usualResidentialAddress",
            source = "usualResidentialAddress")
    FullRecordCompanyPSCApi mapPscData(Psc psc);

    @AfterMapping
    default void mapEtag(@MappingTarget Data target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
    }

    /**
     * Manually map Name.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapName(@MappingTarget Data target, Psc source) {
        NameElements nameElements = source.getNameElements();

        if (nameElements != null) {
            target.setName(Stream.of(nameElements.getTitle(), nameElements.getForename(),
                            nameElements.getMiddleName(), nameElements.getSurname())
                    .filter(Objects::nonNull).collect(Collectors.joining(" ")));
        }
    }

    /**
     * Manually map links.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapLinks(@MappingTarget Data target, Psc source) {
        String encodedId = MapperUtils.encode(source.getInternalId());
        ItemLinkTypes links = new ItemLinkTypes();
        links.setSelf(String
                .format("/company/%s/persons-with-significant-control/%s/%s",
                        source.getCompanyNumber(),
                        source.getKind(),
                        encodedId));

        if (source.getPscStatementId() != null) {
            String encodedStatementId = MapperUtils.encode(source.getPscStatementId());
            links.setStatements(String
                    .format("/company/%s/persons-with-significant-control-statements/%s",
                            source.getCompanyNumber(),
                            encodedStatementId)); ;
        }
        target.setLinks(Collections.singletonList(links));
    }

    /**
     * Manually map kind.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapKind(@MappingTarget Data target, Psc source) {
        switch (source.getKind()) {
            case INDIVIDUAL:
                target.setKind("individual-person-with-significant-control");
                break;
            case CORPORATE_ENTITY:
                target.setKind("corporate-entity-person-with-significant-control");
                break;
            case LEGAL_PERSON:
                target.setKind("legal-person-person-with-significant-control");
                break;
            case SUPER_SECURE:
                target.setKind("super-secure-person-with-significant-control");
                break;
            case INDIVIDUAL_BENEFICIAL_OWNER:
                target.setKind("individual-beneficial-owner");
                break;
            case CORPORATE_BENEFICIAL_OWNER:
                target.setKind("corporate-entity-beneficial-owner");
                break;
            case LEGAL_PERSON_BENEFICIAL_OWNER:
                target.setKind("legal-person-beneficial-owner");
                break;
            case SUPER_SECURE_BENEFICIAL_OWNER:
                target.setKind("super-secure-beneficial-owner");
                break;
            default:
        }
    }

    /**
     * Manually map CeasedOn.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapCeasedOn(@MappingTarget Data target, Psc source) {
        if (source.getCeasedOn() != null) {
            target.setCeasedOn(MapperUtils.parseLocalDate(source.getCeasedOn()));
        }
    }

    /** encode psc_id. */
    @AfterMapping
    default void mapEncodedPscId(@MappingTarget ExternalData target, Psc source) {
        if (source.getPscId() != null) {
            target.setPscId(MapperUtils.encode(source.getPscId()));
        }
    }

    /**
     * Manually map ServiceAddressSameAsRegisteredOffice.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapRegisteredOfficeEnum(@MappingTarget Data target, Psc source) {
        Psc.ServiceAddressSameAsRegisteredOfficeEnum registeredOfficeEnum =
                source.getServiceAddressSameAsRegisteredOffice();

        if (registeredOfficeEnum != null) {
            if (registeredOfficeEnum.toString().equals("Y")) {
                target.setServiceAddressSameAsRegisteredOfficeAddress(Boolean.TRUE);
            } else if (registeredOfficeEnum.toString().equals("N")) {
                target.setServiceAddressSameAsRegisteredOfficeAddress(Boolean.FALSE);
            }
        }
    }

    /**
     * Manually map ResidentialAddressSameAsServiceAddress.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapServiceAddressEnum(@MappingTarget SensitiveData target, Psc source) {
        Psc.ResidentialAddressSameAsServiceAddressEnum serviceAddressEnum =
                source.getResidentialAddressSameAsServiceAddress();

        if (serviceAddressEnum != null) {
            if (serviceAddressEnum.toString().equals("Y")) {
                target.setResidentialAddressSameAsServiceAddress(Boolean.TRUE);
            } else {
                target.setResidentialAddressSameAsServiceAddress(Boolean.FALSE);
            }
        }
    }

    /**
     * Manually map Date of Birth.
     * @param target SensitiveData object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapDateOfBirth(@MappingTarget SensitiveData target, Psc source) {

        if (source.getDateOfBirth() != null) {
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
    }

}
