package uk.gov.companieshouse.psc.delta.consumer;

import consumer.exception.NonRetryableErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.retrytopic.FixedDelayStrategy;
import org.springframework.messaging.Message;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.psc.delta.logging.DataMapHolder;
import uk.gov.companieshouse.psc.delta.processor.PscDeltaProcessor;

@Component
public class PscDeltaConsumer {

    private final Logger logger;
    public final KafkaTemplate<String, Object> kafkaTemplate;

    private final PscDeltaProcessor pscDeltaProcessor;

    /**
     * Default constructor.
     */
    @Autowired
    public PscDeltaConsumer(Logger logger, KafkaTemplate<String, Object> kafkaTemplate,
                            PscDeltaProcessor pscDeltaProcessor) {
        this.logger = logger;
        this.kafkaTemplate = kafkaTemplate;
        this.pscDeltaProcessor = pscDeltaProcessor;
    }

    /**
     * Receives Main topic messages.
     */
    @RetryableTopic(attempts = "${pscs.delta.retry-attempts}",
            backoff = @Backoff(delayExpression = "${pscs.delta.backoff-delay}"),
            fixedDelayTopicStrategy = FixedDelayStrategy.SINGLE_TOPIC,
            dltTopicSuffix = "-error",
            retryTopicSuffix = "-retry",
            dltStrategy = DltStrategy.FAIL_ON_ERROR,
            autoCreateTopics = "false",
            exclude = NonRetryableErrorException.class)
    @KafkaListener(topics = "${pscs.delta.topic}",
            groupId = "${pscs.delta.group-id}",
            containerFactory = "listenerContainerFactory")
    public void receiveMainMessages(Message<ChsDelta> chsDeltaMessage) {
        logger.infoContext(chsDeltaMessage.getPayload().getContextId(),
                "Starting processing a psc delta", DataMapHolder.getLogMap());
        if (chsDeltaMessage.getPayload().getIsDelete()) {
            pscDeltaProcessor.processDelete(chsDeltaMessage);
        } else {
            pscDeltaProcessor.processDelta(chsDeltaMessage);
        }

    }
}
