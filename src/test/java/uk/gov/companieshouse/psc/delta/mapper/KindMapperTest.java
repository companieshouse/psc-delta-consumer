package uk.gov.companieshouse.psc.delta.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.delta.PscDeleteDelta.KindEnum;

@ExtendWith(MockitoExtension.class)
class KindMapperTest {

    @InjectMocks
    private KindMapper kindMapper;

    @ParameterizedTest
    @MethodSource("scenarios")
    void shouldCorrectlyMapKindEnum (KindEnum kindEnum, String expectedKind) {

        // when
        String actualKind = kindMapper.mapKindForDelete(kindEnum);

        // then
        assertEquals(expectedKind,  actualKind);
    }

    private static Stream<Arguments> scenarios() {
        return Stream.of(
                Arguments.of(KindEnum.INDIVIDUAL, "individual-person-with-significant-control"),
                Arguments.of(KindEnum.CORPORATE_ENTITY, "corporate-entity-person-with-significant-control"),
                Arguments.of(KindEnum.LEGAL_PERSON, "legal-person-person-with-significant-control"),
                Arguments.of(KindEnum.SUPER_SECURE, "super-secure-person-with-significant-control"),
                Arguments.of(KindEnum.INDIVIDUAL_BENEFICIAL_OWNER, "individual-beneficial-owner"),
                Arguments.of(KindEnum.CORPORATE_ENTITY_BENEFICIAL_OWNER, "corporate-entity-beneficial-owner"),
                Arguments.of(KindEnum.LEGAL_PERSON_BENEFICIAL_OWNER, "legal-person-beneficial-owner"),
                Arguments.of(KindEnum.SUPER_SECURE_BENEFICIAL_OWNER, "super-secure-beneficial-owner")
        );
    }
}
