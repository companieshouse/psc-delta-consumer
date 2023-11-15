package uk.gov.companieshouse.psc.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.InputStreamReader;
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

import uk.gov.companieshouse.api.psc.NameElements;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.Address;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.SensitiveData;
import uk.gov.companieshouse.api.psc.UsualResidentialAddress;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        PscMapperImpl.class})
class PscMapperTest {

    private ObjectMapper mapper;
    private PscDelta pscDeltaObject;
    private Psc pscObject;
    private Address serviceAddress;
    private UsualResidentialAddress usualResidentialAddress;
    private NameElements nameElements;
    private DateOfBirth dateOfBirth;
    private Identification identification;

    @Autowired
    PscMapper pscMapper;

    @BeforeEach
    void setUp() {
        serviceAddress = createServiceAddress();
        nameElements = createNameElements();
        usualResidentialAddress = createURA();
        dateOfBirth = createDateOfBirth();
    }

    @Test
    void shouldMapIndividualPscToPsc() throws Exception {

        pscObject = createPscObject("individual-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        SensitiveData sensitivedata = externalData.getSensitiveData();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/individual/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatements("/company/00623672/persons-with-significant-control-statements/w9h9A8B-F2rLh_r57J6zpKsyHrM");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("individual-person-with-significant-control", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("Mr John Dave Smith", data.getName());
        assertEquals(nameElements, data.getNameElements());
        assertEquals(Collections.singletonList("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON"), data.getNaturesOfControl());

        assertEquals(Boolean.TRUE, data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertEquals(usualResidentialAddress, sensitivedata.getUsualResidentialAddress());
        assertEquals(Boolean.TRUE, sensitivedata.getResidentialAddressSameAsServiceAddress());
        assertEquals(dateOfBirth, sensitivedata.getDateOfBirth());
    }

    @Test
    void shouldMapCorpPscToPsc() throws Exception {

        pscObject = createPscObject("corporate-entity-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        SensitiveData sensitivedata = externalData.getSensitiveData();
        identification = createCorporateIdentification();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/corporate-entity/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatements("/company/00623672/persons-with-significant-control-statements/w9h9A8B-F2rLh_r57J6zpKsyHrM");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals("Form", identification.getLegalForm());
        assertEquals("Authority", identification.getLegalAuthority());
        assertEquals("Wales", identification.getCountryRegistered());
        assertEquals("Cardiff", identification.getPlaceRegistered());
        assertEquals("16102009", identification.getRegistrationNumber());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("corporate-entity-person-with-significant-control", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("John Smith Limited", data.getName());
        assertEquals(Collections.singletonList("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON"), data.getNaturesOfControl());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertNull(data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertNull(sensitivedata.getUsualResidentialAddress());
        assertNull(sensitivedata.getResidentialAddressSameAsServiceAddress());
        assertNull(sensitivedata.getDateOfBirth());
    }

    @Test
    void shouldMapLegalPscToPsc() throws Exception {

        pscObject = createPscObject("legal-person-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        SensitiveData sensitivedata = externalData.getSensitiveData();
        identification = createLegalIdentification();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/legal-person/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatements("/company/00623672/persons-with-significant-control-statements/w9h9A8B-F2rLh_r57J6zpKsyHrM");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals("Form", identification.getLegalForm());
        assertEquals("Authority", identification.getLegalAuthority());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("legal-person-person-with-significant-control", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("His Majesty King John Smith", data.getName());
        assertEquals(Collections.singletonList("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON"), data.getNaturesOfControl());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertNull(data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertNull(sensitivedata.getUsualResidentialAddress());
        assertNull(sensitivedata.getResidentialAddressSameAsServiceAddress());
        assertNull(sensitivedata.getDateOfBirth());
    }

    @Test
    void shouldMapSuperSecurePscToPsc() throws Exception {

        pscObject = createPscObject("super-secure-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);
        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/super-secure/lXgouUAR16hSIwxdJSpbr_dhyT8");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());
        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("super-secure-person-with-significant-control", data.getKind());
        assertEquals(links, data.getLinks());

        assertNull(data.getServiceAddress());
        assertNull(sensitiveData.getUsualResidentialAddress());
        assertNull(sensitiveData.getDateOfBirth());
        assertNull(data.getLinks().get(0).getStatements());

    }

    @Test
    void shouldMapIndividualBOToBO() throws Exception {

        pscObject = createPscObject("individual-beneficial-owner");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        SensitiveData sensitivedata = externalData.getSensitiveData();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/individual-beneficial-owner/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatements("/company/00623672/persons-with-significant-control-statements/w9h9A8B-F2rLh_r57J6zpKsyHrM");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("individual-beneficial-owner", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("Mr John Dave Smith", data.getName());
        assertEquals(nameElements, data.getNameElements());
        assertEquals(Collections.singletonList("OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON"), data.getNaturesOfControl());

        assertEquals(Boolean.TRUE, data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertEquals(usualResidentialAddress, sensitivedata.getUsualResidentialAddress());
        assertEquals(Boolean.TRUE, sensitivedata.getResidentialAddressSameAsServiceAddress());
        assertEquals(dateOfBirth, sensitivedata.getDateOfBirth());
    }

    Address createServiceAddress() {
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

    UsualResidentialAddress createURA() {
        UsualResidentialAddress ura = new UsualResidentialAddress();

        ura.setPremise("3");
        ura.setAddressLine1("Clos Rhiannon");
        ura.setAddressLine2("Thornhill");
        ura.setLocality("Cardiff");
        ura.setRegion("Here");
        ura.setCountry("Wales");
        ura.setPostalCode("CF14 9HQ");
        ura.setPoBox("PoBox");
        ura.setCareOf("CareOf");

        return ura;
    }

    NameElements createNameElements() {
        NameElements nameElements = new NameElements();

        nameElements.setTitle("Mr");
        nameElements.setForename("John");
        nameElements.setMiddleName("Dave");
        nameElements.setSurname("Smith");

        return nameElements;
    }

    DateOfBirth createDateOfBirth() {
        DateOfBirth dateOfBirth = new DateOfBirth();

        dateOfBirth.setDay(12);
        dateOfBirth.setMonth(7);
        dateOfBirth.setYear(1994);

        return dateOfBirth;
    }

    Psc createPscObject(String type) throws Exception {
        mapper = new ObjectMapper();

        String testFilePath = String.format("%s-delta-example.json", type);

        String input = FileCopyUtils.copyToString(new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(testFilePath)));

        pscDeltaObject = mapper.readValue(input, PscDelta.class);
        pscObject = pscDeltaObject.getPscs().get(0);

        return pscObject;
    }

    Identification createCorporateIdentification() {

        Identification identification = new Identification();

        identification.setCountryRegistered("Wales");
        identification.setLegalAuthority("Authority");
        identification.setLegalForm("Form");
        identification.setPlaceRegistered("Cardiff");
        identification.setRegistrationNumber("16102009");

        return identification;
    }

    Identification createLegalIdentification() {

        Identification identification = new Identification();

        identification.setLegalAuthority("Authority");
        identification.setLegalForm("Form");

        return identification;
    }
}
