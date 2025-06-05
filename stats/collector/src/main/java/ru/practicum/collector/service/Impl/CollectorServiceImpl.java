package ru.practicum.collector.service.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.practicum.collector.service.CollectorService;
import ru.practicum.ewm.stats.avro.UserActionAvro;

@Service
@Slf4j
@RequiredArgsConstructor
public class CollectorServiceImpl implements CollectorService {

    private final KafkaProducer<String, UserActionAvro> kafkaProducer;

    @Value("${spring.kafka.topic.user-actions}")
    private String userActionsTopic;

    @Override
    public void sendAction(UserActionAvro actionAvro) {
        ProducerRecord<String, UserActionAvro> record =
                new ProducerRecord<>(userActionsTopic, actionAvro);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send message to Kafka topic {}: {}",
                        userActionsTopic, exception.getMessage());
            } else {
                log.debug("Message successfully sent to partition {} with offset {}",
                        metadata.partition(), metadata.offset());
            }
        });

    }
}
