package ru.practicum.collector.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.util.Properties;


@RequiredArgsConstructor
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String servers;

    @Value("${spring.kafka.key-serializer}")
    private String keySerializer;

    @Value("${spring.kafka.value-serializer}")
    private String valueSerializer;

    @Bean
    public KafkaProducer<String, UserActionAvro> kafkaProducer() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, servers);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySerializer);
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valueSerializer);

        return new KafkaProducer<>(properties);
    }

}
