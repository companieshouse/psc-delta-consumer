package uk.gov.companieshouse.psc.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.companieshouse.api.delta.Psc.KindEnum.SUPER_SECURE;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Psc;
import consumer.exception.NonRetryableErrorException;
import uk.gov.companieshouse.api.delta.Psc.NaturesOfControlEnum;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.*;

@SpringJUnitConfig(classes = {
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
        assertEquals("Test Company Ltd", externalData.getCompanyName());
        assertEquals("active", externalData.getCompanyStatus());
        assertEquals("previous-psc-3", externalData.getPreviousPscId());

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
        assertEquals("Test Company Ltd", externalData.getCompanyName());
        assertEquals("active", externalData.getCompanyStatus());

        // verify the identification object mapped into the Data object matches the expected identification
        assertEquals(createCorporateIdentification(), data.getIdentification());

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
        Address createServiceAddress = new Address();

        createServiceAddress.setPremises("3");
        createServiceAddress.setAddressLine1("Clos Rhiannon");
        createServiceAddress.setAddressLine2("Thornhill");
        createServiceAddress.setLocality("Cardiff");
        createServiceAddress.setRegion("Here");
        createServiceAddress.setCountry("Wales");
        createServiceAddress.setPostalCode("CF14 9HQ");
        createServiceAddress.setPoBox("PoBox");
        createServiceAddress.setCareOf("CareOf");

        return createServiceAddress;
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
        NameElements createNameElements = new NameElements();

        createNameElements.setTitle("Mr");
        createNameElements.setForename("John");
        createNameElements.setMiddleName("Dave");
        createNameElements.setSurname("Smith");

        return createNameElements;
    }

    DateOfBirth createDateOfBirth() {
        DateOfBirth createDateOfBirth = new DateOfBirth();

        createDateOfBirth.setDay(12);
        createDateOfBirth.setMonth(7);
        createDateOfBirth.setYear(1994);

        return createDateOfBirth;
    }

    Psc createPscObject(String type) throws Exception {
        mapper = new JsonMapper();

        String testFilePath = String.format("%s-delta-example.json", type);

        String input = FileCopyUtils.copyToString(new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(testFilePath)));

        pscDeltaObject = mapper.readValue(input, PscDelta.class);
        pscObject = pscDeltaObject.getPscs().get(0);

        return pscObject;
    }

    Identification createCorporateIdentification() {

        Identification corporateIdentification = new Identification();

        corporateIdentification.setCountryRegistered("Wales");
        corporateIdentification.setLegalAuthority("Authority");
        corporateIdentification.setLegalForm("Form");
        corporateIdentification.setPlaceRegistered("Cardiff");
        corporateIdentification.setRegistrationNumber("16102009");

        return corporateIdentification;
    }

    Identification createLegalIdentification() {

        Identification legalIdentification = new Identification();

        legalIdentification.setLegalAuthority("Authority");
        legalIdentification.setLegalForm("Form");

        return legalIdentification;
    }

    @Test
    void shouldMapNaturesOfControl() {
        Psc source = new Psc();
        source.setNaturesOfControl(List.of(NaturesOfControlEnum.OWNERSHIPOFSHARES_25_TO50_PERCENT_AS_PERSON));
        source.setCompanyNumber("00623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of("ownership-of-shares-25-to-50-percent");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    void shouldMapNaturesOfControlLlp() {
        Psc source = new Psc();
        source.setNaturesOfControl(List.of(NaturesOfControlEnum.RIGHTTOSHARESURPLUSASSETS_25_TO50_PERCENT_AS_FIRM));
        source.setCompanyNumber("OC623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of(
                "right-to-share-surplus-assets-25-to-50-percent-as-firm-limited-liability-partnership");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    void shouldMapNaturesOfControlRoe() {
        Psc source = new Psc();
        source.setNaturesOfControl((List.of(NaturesOfControlEnum.OE_OWNERSHIPOFSHARES_MORETHAN25_PERCENT_AS_FIRM)));
        source.setCompanyNumber("OE623672");
        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of("ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity");

        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    void shouldUseFallbackWhenNameLookupIsMissing() {
        Psc source = new Psc();
        // use NoC value where the enum name differs from the wire value used in the map
        source.setNaturesOfControl((List.of(NaturesOfControlEnum.OE_OWNERSHIPOFSHARES_MORETHAN25_PERCENT_AS_FIRM)));
        source.setCompanyNumber("OE623672");

        // the primary lookup by name should be null and the fallback (toString) should be present
        var nature = source.getNaturesOfControl().get(0);
        var naturesMap = MapperUtils.getNaturesOfControlMap(source.getCompanyNumber());

        assertNull(naturesMap.get(nature.name()));
        // The enum toString() should match the map key and return the mapped value
        assertEquals("ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity",
                naturesMap.get(nature.toString()));

        Data target = new Data();
        pscMapper.mapNaturesOfControl(target, source);

        List<String> expectedValue = List.of("ownership-of-shares-more-than-25-percent-as-firm-registered-overseas-entity");
        assertEquals(expectedValue, target.getNaturesOfControl());
    }

    @Test
    void shouldMapIdentityVerificationDetails() throws Exception {
        pscObject = createPscObject("identity-verification-details-psc");
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        IdentityVerificationDetails ivd = data.getIdentityVerificationDetails();

        assertEquals(LocalDate.of(2018, 1, 31), ivd.getAppointmentVerificationEndOn());
        assertEquals(LocalDate.of(2018, 1, 2), ivd.getAppointmentVerificationStatementDate());
        assertEquals(LocalDate.of(2018, 1, 21), ivd.getAppointmentVerificationStatementDueOn());
        assertEquals(LocalDate.of(2018, 1, 1), ivd.getAppointmentVerificationStartOn());
        assertEquals(LocalDate.of(2018, 1, 12), ivd.getIdentityVerifiedOn());
        assertEquals("Corporate Service Provider Ltd", ivd.getAuthorisedCorporateServiceProviderName());
        assertEquals("Preferred Name", ivd.getPreferredName());
        assertEquals(1, ivd.getAntiMoneyLaunderingSupervisoryBodies().size());
        assertEquals("Money Laundering Supervisory Body", ivd.getAntiMoneyLaunderingSupervisoryBodies().getFirst());
    }

    @Test
    void shouldMapIdentityVerificationDetailsWithoutAntiMoneyLaunderingSupervisoryBodies() throws Exception {
        pscObject = createPscObject("identity-verification-details-psc");
        pscObject.getIdentityVerificationDetails().setAntiMoneyLaunderingSupervisoryBodies(null);
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        IdentityVerificationDetails ivd = data.getIdentityVerificationDetails();

        assertEquals(LocalDate.of(2018, 1, 31), ivd.getAppointmentVerificationEndOn());
        assertEquals(LocalDate.of(2018, 1, 2), ivd.getAppointmentVerificationStatementDate());
        assertEquals(LocalDate.of(2018, 1, 21), ivd.getAppointmentVerificationStatementDueOn());
        assertEquals(LocalDate.of(2018, 1, 1), ivd.getAppointmentVerificationStartOn());
        assertEquals(LocalDate.of(2018, 1, 12), ivd.getIdentityVerifiedOn());
        assertEquals("Corporate Service Provider Ltd", ivd.getAuthorisedCorporateServiceProviderName());
        assertEquals("Preferred Name", ivd.getPreferredName());
        assertNull(ivd.getAntiMoneyLaunderingSupervisoryBodies());
    }


    @Test
    void shouldMapIdentityVerificationDetailsForSuperSecurePscs() throws Exception {
        pscObject = createPscObject("identity-verification-details-psc");
        pscObject.setKind(SUPER_SECURE);
        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = pscMapper.mapPscData(pscObject);

        ExternalData externalData = fullRecordCompanyPSCApi.getExternalData();
        Data data = externalData.getData();
        IdentityVerificationDetails ivd = data.getIdentityVerificationDetails();

        assertEquals(LocalDate.of(2018, 1, 31), ivd.getAppointmentVerificationEndOn());
        assertEquals(LocalDate.of(2018, 1, 1), ivd.getAppointmentVerificationStartOn());

        assertNull(ivd.getAppointmentVerificationStatementDate());
        assertNull(ivd.getAppointmentVerificationStatementDueOn());

        assertNull(ivd.getIdentityVerifiedOn());
        assertNull(ivd.getAuthorisedCorporateServiceProviderName());
        assertNull(ivd.getPreferredName());
        assertNull(ivd.getAntiMoneyLaunderingSupervisoryBodies());
    }

    @Test
    void shouldThrowWhenCompanyStatusIsUnknown() {
        Psc source = new Psc();
        source.setStatus("UNKNOWN_STATUS");
        ExternalData target = new ExternalData();

        assertThrows(NonRetryableErrorException.class, () -> pscMapper.mapCompanyStatus(target, source));
    }

    @Test
    void shouldThrowWhenCompanyStatusIsNull() {
        Psc source = new Psc();
        // status left as null
        ExternalData target = new ExternalData();

        assertThrows(NonRetryableErrorException.class, () -> pscMapper.mapCompanyStatus(target, source));
    }
}
