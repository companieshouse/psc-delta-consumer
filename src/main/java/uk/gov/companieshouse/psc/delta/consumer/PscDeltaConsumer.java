package uk.gov.companieshouse.psc.delta.consumer;

import consumer.exception.NonRetryableErrorException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.SameIntervalTopicReuseStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.psc.delta.processor.PscDeltaProcessor;

@Component
public class PscDeltaConsumer {

    private final PscDeltaProcessor pscDeltaProcessor;

    public PscDeltaConsumer(PscDeltaProcessor pscDeltaProcessor) {
        this.pscDeltaProcessor = pscDeltaProcessor;
    }

    @RetryableTopic(attempts = "${pscs.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${pscs.delta.backoff-delay}"),
            sameIntervalTopicReuseStrategy = SameIntervalTopicReuseStrategy.SINGLE_TOPIC,
            dltTopicSuffix = "-error",
            retryTopicSuffix = "-retry",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${pscs.delta.topic}",
            groupId = "${pscs.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage) {
        if (chsDeltaMessage.getPayload().getIsDelete()) {
            pscDeltaProcessor.processDelete(chsDeltaMessage);
        } else {
            pscDeltaProcessor.processDelta(chsDeltaMessage);
        }
    }
}
