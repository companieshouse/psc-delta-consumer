package uk.gov.companieshouse.pscstatement.delta.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.delta.PscStatement;
import uk.gov.companieshouse.api.psc.CompanyPscStatement;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.api.psc.Statement;
import uk.gov.companieshouse.psc.delta.mapper.CorpAndLegalMapper;
import uk.gov.companieshouse.psc.delta.mapper.GeneralMapper;
import uk.gov.companieshouse.psc.delta.mapper.IndividualMapper;
import uk.gov.companieshouse.psc.delta.transformer.PscApiTransformer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class PscTransformerTest {

    @Mock
    private GeneralMapper generalMapper;
    @Mock
    private IndividualMapper individualMapper;
    @Mock
    private CorpAndLegalMapper corpAndLegalMapper;

    private PscApiTransformer transformer;
    private Psc psc;

    @BeforeEach
    public void setUp() {
        transformer = new PscApiTransformer(generalMapper, individualMapper, corpAndLegalMapper);
    }

    @Test
    public void transformIndividualPscSuccessfully() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);
        input.setDeltaAt("20211008152823383176");

        Psc psc = input.getPscs().get(0);
        psc.setKind(Psc.KindEnum.valueOf("individual"));
        FullRecordCompanyPSCApi mock = mock(FullRecordCompanyPSCApi.class);

        when(individualMapper.mapPscData(psc)).thenReturn(mock);

        FullRecordCompanyPSCApi actual = transformer.transform(input);
        assertThat(actual).isEqualTo(mock);
        assertEquals("individual-person-with-significant-control", actual.getExternalData().getData().getKind());
    }

    @Test
    public void transformCorpPscSuccessfully() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);
        input.setDeltaAt("20211008152823383176");

        Psc psc = input.getPscs().get(0);
        psc.setKind(Psc.KindEnum.valueOf("corporate-entity"));
        FullRecordCompanyPSCApi mock = mock(FullRecordCompanyPSCApi.class);

        when(individualMapper.mapPscData(psc)).thenReturn(mock);

        FullRecordCompanyPSCApi actual = transformer.transform(input);
        assertThat(actual).isEqualTo(mock);
        assertEquals("corporate-entity-person-with-significant-control", actual.getExternalData().getData().getKind());
    }

    @Test
    public void transformSuperSecureBOPscSuccessfully() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);
        input.setDeltaAt("20211008152823383176");

        Psc psc = input.getPscs().get(0);
        psc.setKind(Psc.KindEnum.valueOf("super-secure-beneficial-owner"));
        FullRecordCompanyPSCApi mock = mock(FullRecordCompanyPSCApi.class);

        when(individualMapper.mapPscData(psc)).thenReturn(mock);

        FullRecordCompanyPSCApi actual = transformer.transform(input);
        assertThat(actual).isEqualTo(mock);
        assertEquals("super-secure-beneficial-owner", actual.getExternalData().getData().getKind());
    }

    @Test
    void errorDuringTransformationThrowsNullPointer() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);

        Psc psc = input.getPscs().get(0);

        when(individualMapper.mapPscData(psc))
                .thenThrow(NullPointerException.class);
        assertThrows(NullPointerException.class, () -> transformer.transform(input));
    }
}
