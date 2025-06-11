package ru.practicum.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Configuration
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String server;

    @Value("${kafka.key-serializer}")
    private String keySerializer;

    @Value("${kafka.value-serializer}")
    private String valueSerializer;

    @Value("${kafka.key-deserializer}")
    private String keyDeserializer;

    @Value("${kafka.value-deserializer-similarity}")
    private String eventDeserializer;

    @Value("${kafka.value-deserializer-actions}")
    private String userActionDeserializer;

    @Value("${kafka.consumer-analyzer-actions-group-id}")
    private String actionsConsumerGroupId;

    @Value("${kafka.consumer-analyzer-similarity-group-id}")
    private String similarityConsumerGroupId;

    @Value("${kafka.auto-offset-reset}")
    private String autoOffsetReset;

    @Value("${kafka.schema-registry-url:}")
    private String schemaRegistryUrl;

    private Properties buildConsumerProperties(String valueDeserializer, String groupId) {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> similarityConsumer() {
        return new KafkaConsumer<>(buildConsumerProperties(eventDeserializer, similarityConsumerGroupId));
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> actionsConsumer() {
        return new KafkaConsumer<>(buildConsumerProperties(userActionDeserializer, actionsConsumerGroupId));
    }

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        if (StringUtils.hasText(schemaRegistryUrl)) {
            props.put("schema.registry.url", schemaRegistryUrl);
        }

        return new KafkaProducer<>(props);
    }
}
