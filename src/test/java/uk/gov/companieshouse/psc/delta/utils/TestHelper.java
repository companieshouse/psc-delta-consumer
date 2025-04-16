package uk.gov.companieshouse.psc.delta.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;
import uk.gov.companieshouse.api.delta.Psc;
import uk.gov.companieshouse.api.delta.PscDelta;
import uk.gov.companieshouse.api.psc.Data;
import uk.gov.companieshouse.api.psc.ExternalData;
import uk.gov.companieshouse.api.psc.FullRecordCompanyPSCApi;
import uk.gov.companieshouse.delta.ChsDelta;

public class TestHelper {

    private ChsDelta buildDelta(String data, boolean isDelete) {
        return ChsDelta.newBuilder()
                .setData(data)
                .setContextId("contextId")
                .setAttempt(0)
                .setIsDelete(isDelete)
                .build();
    }

    private Message<ChsDelta> buildMessage(String data, boolean isDelete) {
        return MessageBuilder
                .withPayload(buildDelta(data, isDelete))
                .setHeader(KafkaHeaders.RECEIVED_TOPIC, "test")
                .setHeader("PSC_DELTA_RETRY_COUNT", 1)
                .build();
    }

    public Message<ChsDelta> createInvalidChsDeltaMessage() {
        return buildMessage("This is some invalid data", false);
    }

    public ChsDelta createChsDelta(boolean isDelete) throws IOException {
        String resource;
        if (isDelete) {
            resource = "psc-delete-delta-example.json";
        } else {
            resource = "super-secure-psc-delta-example.json";
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildDelta(data, isDelete);
    }

    public Message<ChsDelta> createChsDeltaMessage(boolean isDelete) throws IOException {
        String resource;
        if (isDelete) {
            resource = "psc-delete-delta-example.json";
        } else {
            resource = "super-secure-psc-delta-example.json";
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data, isDelete);
    }

    public PscDelta createPscDelta() {
        Psc psc = new Psc();

        psc.setCompanyNumber("00623672");
        psc.setPscId("3");
        psc.setInternalId("5");
        psc.setKind(Psc.KindEnum.SUPER_SECURE);
        psc.setCeasedOn("20180201");

        return new PscDelta().addPscsItem(psc);
    }

    public FullRecordCompanyPSCApi createFullRecordCompanyPSCApi() {
        Data data = new Data();
        data.setCeasedOn(LocalDate.of(2018, 2, 1));
        data.setKind("super-secure-person-with-significant-control");

        ExternalData externalData = new ExternalData();
        externalData.setId("5");
        externalData.setInternalId("5");
        externalData.setNotificationId("5");
        externalData.setCompanyNumber("00623672");
        externalData.setData(data);

        FullRecordCompanyPSCApi fullRecordCompanyPSCApi = new FullRecordCompanyPSCApi();
        fullRecordCompanyPSCApi.setExternalData(externalData);

        return fullRecordCompanyPSCApi;
    }
}
