package uk.gov.companieshouse.psc.delta.config;

import consumer.deserialization.AvroDeserializer;
import consumer.exception.TopicErrorInterceptor;
import consumer.serialization.AvroSerializer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;
import uk.gov.companieshouse.delta.ChsDelta;

@TestConfiguration
public class KafkaTestContainerConfig {

    /**
     * Singleton Kafka container shared across all tests.
     * Using static ensures the container survives Spring context reloads.
     * withReuse(true) allows reusing the container across test runs when
     * testcontainers.reuse.enable=true is set in ~/.testcontainers.properties
     *
     * NB: Create or update your local ~.testcontainers.properties file to include:
     * testcontainers.reuse.enable=true
     * to enable container reuse and speed up your test runs in local development dramatically.
     */
    private static final ConfluentKafkaContainer KAFKA_CONTAINER;

    static {
        KAFKA_CONTAINER = new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
                .withReuse(true);
        KAFKA_CONTAINER.start();
    }

    private final AvroDeserializer<ChsDelta> deserializer;
    private final AvroSerializer serializer;

    public KafkaTestContainerConfig(final AvroSerializer serializer, final AvroDeserializer<ChsDelta> deserializer) {
        this.serializer = serializer;
        this.deserializer = deserializer;
    }

    @Bean
    public ConfluentKafkaContainer kafkaContainer() {
        return KAFKA_CONTAINER;
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, ChsDelta> listenerContainerFactory() {
        final var factory = new ConcurrentKafkaListenerContainerFactory<String, ChsDelta>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, ChsDelta> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(kafkaContainer()),
                new StringDeserializer(),
                new ErrorHandlingDeserializer<>(deserializer));
    }

    @Bean
    public Map<String, Object> consumerConfigs(final ConfluentKafkaContainer kafkaContainer) {
        final var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        props.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        props.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, AvroDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        return props;
    }

    @Bean
    public ProducerFactory<String, Object> producerFactory(final ConfluentKafkaContainer kafkaContainer) {
        final var props = new HashMap<String, Object>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroDeserializer.class);
        props.put(ProducerConfig.INTERCEPTOR_CLASSES_CONFIG, TopicErrorInterceptor.class.getName());

        return new DefaultKafkaProducerFactory<>(
                props, new StringSerializer(), serializer);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory(kafkaContainer()));
    }

    @Bean
    public KafkaConsumer<String, Object> mainTopicConsumer() {
        return createTopicConsumer("psc-delta-main-consumer", "psc-delta");
    }

    @Bean
    public KafkaConsumer<String, Object> invalidTopicConsumer() {
        return createTopicConsumer("psc-delta-invalid-consumer", "psc-delta-invalid");
    }

    @Bean
    public KafkaConsumer<String, Object> retryTopicConsumer() {
        return createTopicConsumer("psc-delta-retry-consumer", "psc-delta-retry");
    }

    @Bean
    public KafkaConsumer<String, Object> errorTopicConsumer() {
        return createTopicConsumer("psc-delta-error-consumer", "psc-delta-error");
    }

    private KafkaConsumer<String, Object> createTopicConsumer(final String groupId, final String... topics) {
        final var props = new HashMap<String, Object>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer().getBootstrapServers());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
        final var consumer = new KafkaConsumer<String, Object>(props);
        consumer.subscribe(List.of(topics));
        return consumer;
    }

}
