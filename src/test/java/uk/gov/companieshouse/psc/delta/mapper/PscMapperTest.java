package uk.gov.companieshouse.psc.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.Psc.NaturesOfControlEnum;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.Address;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.DateOfBirth;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Identification;
import uk.gov.companieshouse.api.psc.ItemLinkTypes;
import uk.gov.companieshouse.api.psc.NameElements;
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
        String internalId = externalData.getInternalId();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/individual/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatement("/company/00623672/persons-with-significant-control-statements/UKWLhOXMpdjzt-Maq7hbxAyPyQs");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("UKWLhOXMpdjzt-Maq7hbxAyPyQs", externalData.getPscStatementId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("individual-person-with-significant-control", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("Mr John Dave Smith", data.getName());
        assertEquals(nameElements, data.getNameElements());
        assertEquals(Collections.singletonList("ownership-of-shares-25-to-50-percent"), data.getNaturesOfControl());

        assertEquals(Boolean.TRUE, data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertEquals(usualResidentialAddress, sensitiveData.getUsualResidentialAddress());
        assertEquals(Boolean.TRUE, sensitiveData.getResidentialAddressSameAsServiceAddress());
        assertEquals(dateOfBirth, sensitiveData.getDateOfBirth());
        assertEquals(internalId, sensitiveData.getInternalId().toString());
    }

    @Test
    void shouldMapCorpPscToPsc() throws Exception {
        pscObject = createPscObject("corporate-entity-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        String internalId = externalData.getInternalId();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();
        identification = createCorporateIdentification();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/corporate-entity/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatement("/company/00623672/persons-with-significant-control-statements/UKWLhOXMpdjzt-Maq7hbxAyPyQs");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("UKWLhOXMpdjzt-Maq7hbxAyPyQs", externalData.getPscStatementId());
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
        assertEquals(Collections.singletonList("ownership-of-shares-25-to-50-percent"), data.getNaturesOfControl());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertNull(data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertNull(sensitiveData.getUsualResidentialAddress());
        assertNull(sensitiveData.getResidentialAddressSameAsServiceAddress());
        assertNull(sensitiveData.getDateOfBirth());

        assertEquals(internalId, sensitiveData.getInternalId().toString());
    }

    @Test
    void shouldMapLegalPscToPsc() throws Exception {
        pscObject = createPscObject("legal-person-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        String internalId = externalData.getInternalId();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();
        identification = createLegalIdentification();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/legal-person/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatement("/company/00623672/persons-with-significant-control-statements/UKWLhOXMpdjzt-Maq7hbxAyPyQs");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("UKWLhOXMpdjzt-Maq7hbxAyPyQs", externalData.getPscStatementId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals("Form", identification.getLegalForm());
        assertEquals("Authority", identification.getLegalAuthority());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("legal-person-person-with-significant-control", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("His Majesty King John Smith", data.getName());
        assertEquals(Collections.singletonList("ownership-of-shares-25-to-50-percent"), data.getNaturesOfControl());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertNull(data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertNull(sensitiveData.getUsualResidentialAddress());
        assertNull(sensitiveData.getResidentialAddressSameAsServiceAddress());
        assertNull(sensitiveData.getDateOfBirth());

        assertEquals(internalId, sensitiveData.getInternalId().toString());
    }

    @Test
    void shouldMapSuperSecurePscToPsc() throws Exception {
        pscObject = createPscObject("super-secure-psc");
        pscObject.setPscId(null);

        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);
        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        String internalId = externalData.getInternalId();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf("/company/00623672/persons-with-significant-control/super-secure/lXgouUAR16hSIwxdJSpbr_dhyT8");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertNull(externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertNull(externalData.getPscStatementId());
        assertEquals("00623672", externalData.getCompanyNumber());
        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("super-secure-person-with-significant-control", data.getKind());
        assertEquals("super-secure-persons-with-significant-control", data.getDescription());
        assertEquals(links, data.getLinks());

        assertNull(data.getServiceAddress());
        assertNull(sensitiveData.getUsualResidentialAddress());
        assertNull(sensitiveData.getDateOfBirth());
        assertNull(data.getLinks().get(0).getStatement());

        assertEquals(internalId, sensitiveData.getInternalId().toString());
    }

    @Test
    void shouldMapIndividualBOToBO() throws Exception {
        pscObject = createPscObject("individual-beneficial-owner");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        String internalId = externalData.getInternalId();
        Data data = externalData.getData();
        SensitiveData sensitiveData = externalData.getSensitiveData();

        fullRecordCompanyPSCApi.getExternalData().getData().setEtag(null);

        List<ItemLinkTypes> links = new ArrayList<>();
        ItemLinkTypes linkTypes = new ItemLinkTypes();
        linkTypes.setSelf(
                "/company/00623672/persons-with-significant-control/individual-beneficial-owner/lXgouUAR16hSIwxdJSpbr_dhyT8");
        linkTypes.setStatement("/company/00623672/persons-with-significant-control-statements/UKWLhOXMpdjzt-Maq7hbxAyPyQs");
        links.add(linkTypes);

        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getId());
        assertEquals("AoRE4bhxdSdXur_NLdfh4JF81Y4", externalData.getPscId());
        assertEquals("5", externalData.getInternalId());
        assertEquals("lXgouUAR16hSIwxdJSpbr_dhyT8", externalData.getNotificationId());
        assertEquals("UKWLhOXMpdjzt-Maq7hbxAyPyQs", externalData.getPscStatementId());
        assertEquals("00623672", externalData.getCompanyNumber());

        assertEquals(LocalDate.of(2018, 2, 1), data.getCeasedOn());
        assertEquals("individual-beneficial-owner", data.getKind());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotifiedOn());
        assertEquals(serviceAddress, data.getServiceAddress());
        assertEquals("Mr John Dave Smith", data.getName());
        assertEquals(nameElements, data.getNameElements());
        assertEquals(Collections.singletonList("ownership-of-shares-25-to-50-percent"), data.getNaturesOfControl());

        assertEquals(Boolean.TRUE, data.getServiceAddressSameAsRegisteredOfficeAddress());
        assertEquals("Wales", data.getCountryOfResidence());
        assertEquals(links, data.getLinks());
        assertEquals("Welsh", data.getNationality());
        assertEquals(LocalDate.of(2016, 1, 1), data.getNotificationDate());

        assertEquals(usualResidentialAddress, sensitiveData.getUsualResidentialAddress());
        assertEquals(Boolean.TRUE, sensitiveData.getResidentialAddressSameAsServiceAddress());
        assertEquals(dateOfBirth, sensitiveData.getDateOfBirth());
        assertEquals(internalId, sensitiveData.getInternalId().toString());
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


    @Test
    public void shouldMapNaturesOfControl() {
        Psc source = new Psc();
        source.setNaturesOfControl(List.of(NaturesOfControlEnum.OWNERSHIPOFSHARES_25TO50PERCENT_AS_PERSON));
        source.setCompanyNumber("00623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of("ownership-of-shares-25-to-50-percent");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    public void shouldMapNaturesOfControlLlp() {
        Psc source = new Psc();
        source.setNaturesOfControl(List.of(NaturesOfControlEnum.RIGHTTOSHARESURPLUSASSETS_25TO50PERCENT_AS_FIRM));
        source.setCompanyNumber("OC623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of(
                "right-to-share-surplus-assets-25-to-50-percent-as-firm-limited-liability-partnership");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    public void shouldMapNaturesOfControlRoe() {
        Psc source = new Psc();
        source.setNaturesOfControl((List.of(NaturesOfControlEnum.OE_OWNERSHIPOFSHARES_MORETHAN25PERCENT_AS_FIRM)));
        source.setCompanyNumber("OE623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of("ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

}
