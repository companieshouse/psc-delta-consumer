package uk.gov.companieshouse.psc.delta.utils;

import java.io.IOException;
import java.io.InputStreamReader;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.util.FileCopyUtils;

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

    private Message<ChsDelta> buildMessage (String data, boolean isDelete) {
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
            resource = "corporate-entity-psc-delta-example";
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
            resource = "corporate-entity-psc-delta-example";
        }
        InputStreamReader exampleJsonPayload = new InputStreamReader(
                ClassLoader.getSystemClassLoader().getResourceAsStream(resource));
        String data = FileCopyUtils.copyToString(exampleJsonPayload);

        return buildMessage(data, isDelete);
    }  
}
