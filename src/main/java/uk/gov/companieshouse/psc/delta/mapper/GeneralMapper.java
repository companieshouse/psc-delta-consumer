package uk.gov.companieshouse.psc.delta.mapper;

import java.util.Collections;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.MappingTarget;
import uk.gov.companieshouse.GenerateEtagUtil;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.model.psc.PscLinks;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;



@Mapper(componentModel = "spring")
public interface GeneralMapper {

    @Mapping(target = "externalData.internalId", source = "internalId")
    @Mapping(target = "externalData.notificationId", source = "internalId")
    @Mapping(target = "externalData.companyNumber", source = "companyNumber")
    @Mapping(target = "externalData.id", source = "internalId")
    @Mapping(target = "externalData.data.etag", ignore = true)
    @Mapping(target = "externalData.data.links", ignore = true)
    @Mapping(target = "externalData.data.kind", source = "kind", ignore = true)
    @Mapping(target = "externalData.data.ceasedOn", source = "ceasedOn", dateFormat = "yyyyMMdd")
    FullRecordCompanyPSCApi mapPscData(Psc psc);

    @AfterMapping
    default void mapEtag(@MappingTarget Data target) {
        target.setEtag(GenerateEtagUtil.generateEtag());
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

}
