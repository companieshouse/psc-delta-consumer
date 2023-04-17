package uk.gov.companieshouse.psc.delta.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.api.delta.NameElements;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;

@Mapper(componentModel = "spring", uses = {GeneralMapper.class})
public interface CorpAndLegalMapper {

    @Mapping(target = "externalData.pscId", source = "pscId", ignore = true)
    @Mapping(target = "externalData.data.notifiedOn ",
            source = "notificationDate", dateFormat = "yyyyMMdd")
    @Mapping(target = "externalData.data.serviceAddress", source = "address")
    @Mapping(target = "externalData.data.nameElements", source = "nameElements")
    @Mapping(target = "externalData.data.nationality", source = "nationality")
    @Mapping(target = "externalData.data.countryOfResidence", source = "countryOfResidence")
    @Mapping(target = "externalData.data.naturesOfControl", source = "naturesOfControl")
    @Mapping(target = "externalData.data.name", ignore = true)
    FullRecordCompanyPSCApi mapPscData(Psc psc);

    /**
     * Manually map Name.
     * @param target Data object within FullRecordCompanyPSCApi object to map to
     * @param source Psc delta object that will be mapped from
     */
    @AfterMapping
    default void mapName(@MappingTarget Data target, Psc source) {
        NameElements nameElements = source.getNameElements();

        String title = nameElements.getTitle();
        String forename = nameElements.getForename();
        String surname = nameElements.getSurname();
        String middleName = nameElements.getMiddleName();

        if (nameElements.getTitle() != null && nameElements.getMiddleName() != null) {
            target.setName(title + " " + forename + " " + middleName + " " + surname);
            target.setTitle(title);
        } else if (nameElements.getTitle() == null && nameElements.getMiddleName() != null) {
            target.setName(forename + " " + middleName + " " + surname);
        } else if (nameElements.getTitle() != null && nameElements.getMiddleName() == null) {
            target.setName(title + " " + forename + " " + surname);
            target.setTitle(title);
        } else {
            target.setName(forename + " " + surname);
        }
        target.setForename(forename);
        target.setSurname(surname);

    }
}
