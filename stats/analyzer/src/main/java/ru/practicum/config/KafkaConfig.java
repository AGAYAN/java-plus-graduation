package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;

@Getter
@Setter
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

    private Properties propertiesConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, eventDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, similarityConsumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> baseConsumer() {
        return new KafkaConsumer<>(propertiesConsumer());
    }

    private Properties propertiesActionsConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, userActionDeserializer);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, actionsConsumerGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> actionsConsumer() {
        return new KafkaConsumer<>(propertiesActionsConsumer());
    }

    @Bean
    public KafkaProducer<String, Object> kafkaProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, server);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);
        return new KafkaProducer<>(props);
    }
}
