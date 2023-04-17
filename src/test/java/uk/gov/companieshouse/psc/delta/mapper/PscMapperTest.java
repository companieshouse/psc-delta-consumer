package uk.gov.companieshouse.psc.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        GeneralMapperImpl.class,
        IndividualMapperImpl.class,
        CorpAndLegalMapperImpl.class})
public class PscMapperTest {

    private ObjectMapper mapper;
    private PscDelta deltaObject;
    private Psc psc;
    private Address serviceAddress;
    private UsualResidentialAddress usualResidentialAddress;

    @Autowired
    GeneralMapper generalMapper;

    @Autowired
    IndividualMapper individualMapper;

    @Autowired
    CorpAndLegalMapper corpAndLegalMapper;

    @BeforeEach
    public void setUp() throws Exception {
        mapper = new ObjectMapper();

        String path = "psc-statement-delta-example.json";
        String input = FileCopyUtils.copyToString(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(path)));

        deltaObject = mapper.readValue(input, PscDelta.class);
        psc = deltaObject.getPscs().get(0);

        serviceAddress = createServiceAddress();
    }

    @Test
    public void shouldMapIndividualPscToPsc() throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = individualMapper.mapPscData(psc);
        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);
        FullRecordCompanyPSCApi expectedResult = new FullRecordCompanyPSCApi();

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/08694860/persons-with-significant-control/individual/3VZoaE42kS3MxZqoyPhtIVQ99yc");
        linkTypes.setStatements("/company/08694860/persons-with-significant-control-statements/BjncsPOPYNVsAbTV7D8UOrvuxSc");
        links.add(linkTypes);

        expectedResult.getExternalData().setId("5");
        expectedResult.getExternalData().setPscId("vuIAhYYbRDhqzx9b3e_jd6Uhres");
        expectedResult.getExternalData().setInternalId("5");
        expectedResult.getExternalData().setNotificationId("5");
        expectedResult.getExternalData().setCompanyNumber("00623672");

        //links set in transformer
        expectedResult.getExternalData().getData().setNotifiedOn(LocalDate.of(2016, 1, 1));
        expectedResult.getExternalData().getData().setServiceAddress(serviceAddress);
        expectedResult.getExternalData().getData().setName("Mr John Dave Smith");
        expectedResult.getExternalData().getData().setNameElements();
        expectedResult.getExternalData().getData().setNaturesOfControl(Collections.singletonList("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON"));
        expectedResult.getExternalData().getData().setIsSanctioned();
        expectedResult.getExternalData().getData().setServiceAddressSameAsRegisteredOfficeAddress(Boolean.TRUE);
        expectedResult.getExternalData().getData().setResidentialAddressIsSameAsServiceAddress(Boolean.TRUE);
        expectedResult.getExternalData().getData().setCountryOfResidence("Wales");
        expectedResult.getExternalData().getData().setLinks(links);
        expectedResult.getExternalData().getData().setNationality("Welsh");
        expectedResult.getExternalData().getData().setSurname("Smith");
        expectedResult.getExternalData().getData().setForename("John");
        expectedResult.getExternalData().getData().setTitle("Mr");
        expectedResult.getExternalData().getData().setCompanyNumber("00623672");
        expectedResult.getExternalData().getData().setNotificationDate(LocalDate.of(2016, 1, 1));

        expectedResult.getExternalData().getSensitiveData().setUsualResidentialAddress(serviceAddress);
        expectedResult.getExternalData().getSensitiveData().setResidentialAddressSameAsServiceAddress(Boolean.TRUE);
        expectedResult.getExternalData().getSensitiveData().setDateOfBirth();

        //deltaat set in transformer

        System.out.println(fullRecordCompanyPSCApi);
        assertEquals(fullRecordCompanyPSCApi.getExternalData().getData().getCeasedOn(), LocalDate.of(2018, 2, 1));
        assertEquals(fullRecordCompanyPSCApi, expectedResult);

    }

    public Address createServiceAddress() {
        Address serviceAddress = new Address();

        serviceAddress.setPremises("3");
        serviceAddress.setAddressLine1("Clos Rhiannon");
        serviceAddress.setAddressLine2("Thornhill");
        serviceAddress.setLocality("Cardiff");
        serviceAddress.setRegion("Here");
        serviceAddress.setCountry("Wales");
        serviceAddress.setPostalCode("CF14 9HQ");
        serviceAddress.setPoBox("PoBox");
        serviceAddress.setCareOf("CareOf");

        return serviceAddress;
    }
}
