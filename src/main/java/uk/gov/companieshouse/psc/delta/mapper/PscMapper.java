package uk.gov.companieshouse.psc.delta.mapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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
import uk.gov.companieshouse.api.delta.PscAddress;
import uk.gov.companieshouse.api.psc.Address;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.SensitiveData;

@Mapper(componentModel = "spring")
public interface PscMapper {

    @Mapping(target = "externalData.internalId", source = "internalId")
    @Mapping(target = "externalData.notificationId", source = "internalId", ignore = true)
    @Mapping(target = "externalData.companyNumber", source = "companyNumber")
    @Mapping(target = "externalData.id", source = "internalId", ignore = true)
    @Mapping(target = "externalData.data.etag", ignore = true)
    @Mapping(target = "externalData.data.name", ignore = true)
    @Mapping(target = "externalData.data.links", ignore = true)
    @Mapping(target = "externalData.data.kind", source = "kind", ignore = true)
    @Mapping(target = "externalData.data.isSanctioned", ignore = true)
    @Mapping(target = "externalData.data.ceasedOn", source = "ceasedOn", ignore = true)
    @Mapping(target = "externalData.pscId", source = "pscId", ignore = true)
    @Mapping(target = "externalData.data.notifiedOn", source = "notificationDate",
            dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.notificationDate", source = "notificationDate",
            dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.serviceAddress", source = "address")
    @Mapping(target = "externalData.data.principalOfficeAddress", source = "principalOfficeAddress")
    @Mapping(target = "externalData.data.nameElements", source = "nameElements")
    @Mapping(target = "externalData.data.nationality", source = "nationality")
    @Mapping(target = "externalData.data.countryOfResidence", source = "countryOfResidence")
    @Mapping(target = "externalData.data.naturesOfControl", source = "naturesOfControl")
    @Mapping(target = "externalData.data.serviceAddressSameAsRegisteredOfficeAddress",
            source = "serviceAddressSameAsRegisteredAddress", ignore = true)
    @Mapping(target = "externalData.sensitiveData.residentialAddressSameAsServiceAddress",
            source = "residentialAddressSameAsServiceAddress", ignore = true)
    @Mapping(target = "externalData.sensitiveData.dateOfBirth", ignore = true)
    @Mapping(target = "externalData.sensitiveData.usualResidentialAddress",
            source = "usualResidentialAddress")
    @Mapping(target = "externalData.data.identification", ignore = true)
    
    FullRecordCompanyPSCApi mapPscData(Psc psc);

    /** encode internal_id and map to id and notification_id. */
    @AfterMapping
    default void mapEncodedInternalId(@MappingTarget ExternalData target, Psc source) {
        target.setId(MapperUtils.encode(source.getInternalId()));
        target.setNotificationId(MapperUtils.encode(source.getInternalId()));
    }

    @AfterMapping
    default void mapEtag(@MappingTarget Data target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
    }

    /**
    * Invoked at the end of the auto-generated mapping methods.
    *
    * @param target     the target PSC
    * @param source     the source PSC
    */
    @AfterMapping
    default void mapAddressPremiseToPremises(@MappingTarget Data target, Psc source) {
        Address serviceAddress = target.getServiceAddress();
        Address principalOfficeAddress = target.getPrincipalOfficeAddress();
        if (source.getAddress() != null) {
            serviceAddress.setPremises(source.getAddress().getPremise());
        }
        if (source.getPrincipalOfficeAddress() != null) {
            principalOfficeAddress.setPremises(source.getPrincipalOfficeAddress().getPremise());
        }
    }

    /**
     * Invoked at the end of the auto-generated mapping methods.
     *
     * @param target     the target PSC
     * @param source     the source PSC
     */
    @AfterMapping
    default void mapCareOf(@MappingTarget ExternalData target, Psc source) {
        Data data = target.getData();
        Address address = data.getServiceAddress();
        PscAddress sourceAddress = source.getAddress();
        if (source.getAddress() != null) {
            address.setCareOf(sourceAddress.getCareOfName());
            data.setServiceAddress(address);
            target.setData(data);
        }
    }

    /**
     * Manually map Description for Super Secure.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapSuperSecureDescription(@MappingTarget Data target, Psc source) {
        if (source.getKind() == Psc.KindEnum.SUPER_SECURE) {
            target.setDescription("super-secure-persons-with-significant-control");
        } else if (source.getKind() == Psc.KindEnum.SUPER_SECURE_BENEFICIAL_OWNER) {
            target.setDescription("super-secure-beneficial-owner");
        }
    }

    /**
     * Manually map Name.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapName(@MappingTarget Data target, Psc source) {
        Psc.KindEnum kind = source.getKind();
        if (kind.equals(Psc.KindEnum.INDIVIDUAL)
                || kind.equals(Psc.KindEnum.INDIVIDUAL_BENEFICIAL_OWNER)) {
            NameElements nameElements = source.getNameElements();
            if (nameElements != null) {
                target.setName(Stream.of(
                                nameElements.getTitle(),
                                nameElements.getForename(),
                                nameElements.getMiddleName(),
                                nameElements.getSurname())
                        .filter(Objects::nonNull).collect(Collectors.joining(" ")));
            }
        } else {
            target.setName(source.getName());
        }
    }

    /**
     * Manually map IsSanctioned.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapIsSanctioned(@MappingTarget Data target, Psc source) {
        if (source.getSanctionInd() == Psc.SanctionIndEnum._0) {
            target.setIsSanctioned(false);
        } else if (source.getSanctionInd() == Psc.SanctionIndEnum._1) {
            target.setIsSanctioned(true);
        }
        // else null
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
                            encodedStatementId));
        }
        target.setLinks(Collections.singletonList(links));
    }

    /**
     * Manually map kind.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapKindAndIdentification(@MappingTarget Data target, Psc source) {

        Identification identification = new Identification();
        target.setIdentification(identification);
        switch (source.getKind()) {
            case INDIVIDUAL:
                target.setKind("individual-person-with-significant-control");
                break;
            case CORPORATE_ENTITY:
                target.setKind("corporate-entity-person-with-significant-control");
                identification.setLegalAuthority(source.getLegalAuthority());
                identification.setLegalForm(source.getLegalForm());
                identification.setCountryRegistered(source.getCountryRegistered());
                identification.setPlaceRegistered(source.getPlaceRegistered());
                identification.setRegistrationNumber(source.getRegistrationNumber());
                break;
            case LEGAL_PERSON:
                target.setKind("legal-person-person-with-significant-control");
                identification.setLegalAuthority(source.getLegalAuthority());
                identification.setLegalForm(source.getLegalForm());
                break;
            case SUPER_SECURE:
                target.setKind("super-secure-person-with-significant-control");
                break;
            case INDIVIDUAL_BENEFICIAL_OWNER:
                target.setKind("individual-beneficial-owner");
                break;
            case CORPORATE_ENTITY_BENEFICIAL_OWNER:
                target.setKind("corporate-entity-beneficial-owner");
                identification.setLegalAuthority(source.getLegalAuthority());
                identification.setLegalForm(source.getLegalForm());
                identification.setCountryRegistered(source.getCountryRegistered());
                identification.setPlaceRegistered(source.getPlaceRegistered());
                identification.setRegistrationNumber(source.getRegistrationNumber());
                break;
            case LEGAL_PERSON_BENEFICIAL_OWNER:
                target.setKind("legal-person-beneficial-owner");
                identification.setLegalAuthority(source.getLegalAuthority());
                identification.setLegalForm(source.getLegalForm());
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
        target.setPscId(MapperUtils.encode(source.getPscId()));
    }

    /**
     * Manually map ServiceAddressSameAsRegisteredAddress.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapServiceAddressSameAsRegisteredAddress(@MappingTarget Data target, Psc source) {
        Psc.ServiceAddressSameAsRegisteredAddressEnum registeredAddressEnum =
                source.getServiceAddressSameAsRegisteredAddress();

        if (registeredAddressEnum != null) {
            target.setServiceAddressSameAsRegisteredOfficeAddress(
                    registeredAddressEnum.toString().equals("Y"));
        }
    }

    /**
     * Manually map ResidentialAddressSameAsServiceAddress.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapResidentialAddressSameAsServiceAddress(
            @MappingTarget SensitiveData target, Psc source) {
        Psc.ResidentialAddressSameAsServiceAddressEnum serviceAddressEnum =
                source.getResidentialAddressSameAsServiceAddress();

        if (serviceAddressEnum != null) {
            target.setResidentialAddressSameAsServiceAddress(
                    serviceAddressEnum.toString().equals("Y"));
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

    /**
     * Manually map NaturesOfControl.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from.
     */

    @AfterMapping
    default void mapNaturesOfControl(@MappingTarget Data target, Psc source) {
        if (source.getNaturesOfControl() != null && !source.getNaturesOfControl().isEmpty()) {
            HashMap<String,String> naturesOfControlMap =
                    MapperUtils.getNaturesOfControlMap(source.getCompanyNumber());
            List<String> mappedNaturesOfControl = new ArrayList<>();
            for (Psc.NaturesOfControlEnum nature : source.getNaturesOfControl()) {
                String natureKey = nature.name();
                String mappedValue = naturesOfControlMap.get(natureKey);
                mappedNaturesOfControl.add(mappedValue);
            }
            target.setNaturesOfControl(mappedNaturesOfControl);
        }
    }



}
