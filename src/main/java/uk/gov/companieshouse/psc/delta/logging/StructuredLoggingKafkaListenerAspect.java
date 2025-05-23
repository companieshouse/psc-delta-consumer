package uk.gov.companieshouse.psc.delta.logging;

import static org.springframework.kafka.retrytopic.RetryTopicHeaders.DEFAULT_HEADER_ATTEMPTS;
import static org.springframework.kafka.support.KafkaHeaders.OFFSET;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_PARTITION;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;
import static uk.gov.companieshouse.psc.delta.PscDeltaConsumerApplication.NAMESPACE;

import consumer.exception.NonRetryableErrorException;
import consumer.exception.RetryableErrorException;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.delta.ChsDelta;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

@Component
@Aspect
class StructuredLoggingKafkaListenerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(NAMESPACE);
    private static final String LOG_MESSAGE_RECEIVED = "Processing delta";
    private static final String LOG_MESSAGE_DELETE_RECEIVED = "Processing DELETE delta";
    private static final String LOG_MESSAGE_PROCESSED = "Processed delta";
    private static final String LOG_MESSAGE_DELETE_PROCESSED = "Processed DELETE delta";
    private static final String EXCEPTION_MESSAGE = "%s exception thrown";

    private final int maxAttempts;

    StructuredLoggingKafkaListenerAspect(@Value("${pscs.delta.retry-attempts}") int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Around("@annotation(org.springframework.kafka.annotation.KafkaListener)")
    public Object manageStructuredLogging(ProceedingJoinPoint joinPoint) throws Throwable {
        int retryCount = 0;
        try {
            Message<?> message = (Message<?>) joinPoint.getArgs()[0];
            MessageHeaders headers = message.getHeaders();
            retryCount = Optional.ofNullable(headers.get(DEFAULT_HEADER_ATTEMPTS))
                    .map(attempts -> ByteBuffer.wrap((byte[]) attempts).getInt())
                    .orElse(1) - 1;

            ChsDelta chsDelta = extractChsDelta(message.getPayload());

            DataMapHolder.initialise(Optional.ofNullable(chsDelta.getContextId())
                    .orElse(UUID.randomUUID().toString()));

            DataMapHolder.get()
                    .retryCount(retryCount)
                    .topic((String) headers.get(RECEIVED_TOPIC))
                    .partition((Integer) headers.get(
                            RECEIVED_PARTITION))
                    .offset((Long) headers.get(OFFSET));

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_RECEIVED : LOG_MESSAGE_RECEIVED,
                    DataMapHolder.getLogMap());

            Object result = joinPoint.proceed();

            LOGGER.info(chsDelta.getIsDelete() ? LOG_MESSAGE_DELETE_PROCESSED : LOG_MESSAGE_PROCESSED,
                    DataMapHolder.getLogMap());

            return result;
        } catch (RetryableErrorException ex) {
            // maxAttempts includes first attempt which is not a retry
            if (retryCount >= maxAttempts - 1) {
                LOGGER.error("Max retry attempts reached", ex, DataMapHolder.getLogMap());
            } else {
                LOGGER.info(String.format(EXCEPTION_MESSAGE, ex.getClass().getSimpleName()), DataMapHolder.getLogMap());
            }
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Exception thrown", ex, DataMapHolder.getLogMap());
            throw ex;
        } finally {
            DataMapHolder.clear();
        }
    }

    private ChsDelta extractChsDelta(Object payload) {
        if (payload instanceof ChsDelta chsDelta) {
            return Optional.of(chsDelta)
                    .orElseGet(() -> {
                        LOGGER.error("Null payload", DataMapHolder.getLogMap());
                        throw new NonRetryableErrorException("Null payload");
                    });
        }
        final String errorMessage = "Invalid payload type, payload: [%s]".formatted(payload);
        LOGGER.error(errorMessage, DataMapHolder.getLogMap());
        throw new NonRetryableErrorException(errorMessage);
    }
}
