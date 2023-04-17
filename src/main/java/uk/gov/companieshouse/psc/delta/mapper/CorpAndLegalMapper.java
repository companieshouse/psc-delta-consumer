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
    FullRecordCompanyPSCApi mapPscData(Psc psc);
}
