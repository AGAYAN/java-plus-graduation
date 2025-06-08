package ru.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
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
    private String bootstrapServers;

    @Value("${kafka.key-deserializer}")
    private String keyDeserializer;

    @Value("${kafka.value-deserializer-similarity}")
    private String valueDeserializerSimilarity;

    @Value("${kafka.value-deserializer-actions}")
    private String valueDeserializerActions;

    @Value("${kafka.consumer-analyzer-similarity-group-id}")
    private String consumerAnalyzerSimilarityGroupId;

    @Value("${kafka.consumer-analyzer-actions-group-id}")
    private String consumerAnalyzerActionsGroupId;

    @Value("${kafka.auto-offset-reset}")
    private String autoOffsetReset;


    private Properties propertiesConsumer() {

        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerSimilarity);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerAnalyzerSimilarityGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);

        return props;
    }

    @Bean
    public KafkaConsumer<String, EventSimilarityAvro> baseConsumer() {
        return new KafkaConsumer<>(propertiesConsumer());
    }

    private Properties propertiesActionsConsumer() {
        Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeserializer);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializerActions);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerAnalyzerActionsGroupId);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        return props;
    }

    @Bean
    public KafkaConsumer<String, UserActionAvro> actionsConsumer() {
        return new KafkaConsumer<>(propertiesActionsConsumer());
    }
}
