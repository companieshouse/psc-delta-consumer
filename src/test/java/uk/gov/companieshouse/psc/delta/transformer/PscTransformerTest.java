package uk.gov.companieshouse.psc.delta.transformer;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.psc.delta.mapper.PscMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class PscTransformerTest {

    @Mock
    private PscMapper pscMapper;
    private PscApiTransformer transformer;

    @BeforeEach
    public void setUp() {
        transformer = new PscApiTransformer(pscMapper);
    }

    @Test
    public void transformPscSuccessfully() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);
        input.setDeltaAt("20211008152823383176");

        Psc psc = input.getPscs().get(0);
        FullRecordCompanyPSCApi mock = mock(FullRecordCompanyPSCApi.class);

        when(pscMapper.mapPscData(psc)).thenReturn(mock);

        FullRecordCompanyPSCApi actual = transformer.transform(input);
        assertThat(actual).isEqualTo(mock);
    }

    @Test
    void errorDuringTransformationThrowsNullPointer() {
        PscDelta input = new PscDelta();
        List<Psc> pscList = new ArrayList<>();
        pscList.add(new Psc());
        input.setPscs(pscList);

        Psc psc = input.getPscs().get(0);

        when(pscMapper.mapPscData(psc))
                .thenThrow(NullPointerException.class);
        assertThrows(NullPointerException.class, () -> transformer.transform(input));
    }
}
