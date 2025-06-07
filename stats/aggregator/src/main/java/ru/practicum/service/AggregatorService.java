package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorService {

    private final KafkaProducer<String, Object> kafkaProducer;
    private final KafkaConsumer<String, UserActionAvro> kafkaConsumer;

    @Value("${kafka.topics.actions}")
    private String actionTopic;

    @Value("${kafka.topics.similarity}")
    private String similarityTopic;

    private final Map<Long, Map<Long, Double>> userEventWeights = new ConcurrentHashMap<>();

    private final Map<EventPair, Double> eventPairSimilarities = new ConcurrentHashMap<>();

    private final Map<Long, Double> eventTotalWeights = new ConcurrentHashMap<>();

    public void startProcessing() {
        kafkaConsumer.subscribe(Collections.singleton(actionTopic));

        while (!Thread.currentThread().isInterrupted()) {
            processRecords(kafkaConsumer.poll(Duration.ofMillis(1000)));
        }
    }

    private void processRecords(ConsumerRecords<String, UserActionAvro> records) {
        records.forEach(record -> {
            UserActionAvro action = record.value();
            processUserAction(action);
        });
        kafkaConsumer.commitAsync();
    }

    private void processUserAction(UserActionAvro action) {
        double newWeight = getActionWeight(action.getActionType());
        long eventId = action.getEventId();
        long userId = action.getUserId();

        Double currentWeight = getUserEventWeight(eventId, userId);

        if (currentWeight == null || newWeight > currentWeight) {
            updateWeights(eventId, userId, newWeight, currentWeight);
        }
    }

    private void updateWeights(long eventId, long userId, double newWeight, Double oldWeight) {
        userEventWeights.computeIfAbsent(eventId, k -> new ConcurrentHashMap<>())
                .put(userId, newWeight);

        double weightDelta = oldWeight != null ? newWeight - oldWeight : newWeight;
        eventTotalWeights.merge(eventId, weightDelta, Double::sum);

        recalculateSimilarities(eventId, userId, newWeight, oldWeight);
    }

    private void recalculateSimilarities(long eventId, long userId, double newWeight, Double oldWeight) {
        userEventWeights.forEach((otherEventId, userWeights) -> {
            if (otherEventId != eventId && userWeights.containsKey(userId)) {
                double otherWeight = userWeights.get(userId);
                updateEventPairSimilarity(eventId, otherEventId, userId, newWeight, oldWeight, otherWeight);
            }
        });
    }

    private void updateEventPairSimilarity(long eventA, long eventB, long userId,
                                           double newWeightA, Double oldWeightA, double weightB) {
        EventPair pair = EventPair.of(eventA, eventB);

        double oldMin = oldWeightA != null ? Math.min(oldWeightA, weightB) : 0;
        double newMin = Math.min(newWeightA, weightB);
        double minDelta = newMin - oldMin;

        double newSumMin = eventPairSimilarities.merge(pair, minDelta, Double::sum);

        double sumA = eventTotalWeights.getOrDefault(eventA, 0.0);
        double sumB = eventTotalWeights.getOrDefault(eventB, 0.0);

        double similarity = newSumMin / (Math.sqrt(sumA) * Math.sqrt(sumB));

        sendSimilarityEvent(pair, similarity);
    }

    private void sendSimilarityEvent(EventPair pair, double similarity) {
        EventSimilarityAvro eventSimilarity = EventSimilarityAvro.newBuilder()
                .setEventA(pair.eventA())
                .setEventB(pair.eventB())
                .setScore(similarity)
                .setTimestamp(Instant.now())
                .build();

        kafkaProducer.send(new ProducerRecord<>(similarityTopic, eventSimilarity), (metadata, e) -> {
            if (e != null) {
                log.error("Failed to send similarity event", e);
            } else {
                log.debug("Sent similarity: {} for pair {}", similarity, pair);
            }
        });
    }

    private Double getUserEventWeight(long eventId, long userId) {
        Map<Long, Double> eventWeights = userEventWeights.get(eventId);
        return eventWeights != null ? eventWeights.get(userId) : null;
    }

    private double getActionWeight(ActionTypeAvro actionType) {
        return switch (actionType) {
            case VIEW -> 0.4;
            case REGISTER -> 0.8;
            case LIKE -> 1.0;
            default -> 0.0;
        };
    }

    private record EventPair(long eventA, long eventB) {
        static EventPair of(long a, long b) {
            return a < b ? new EventPair(a, b) : new EventPair(b, a);
        }
    }
}