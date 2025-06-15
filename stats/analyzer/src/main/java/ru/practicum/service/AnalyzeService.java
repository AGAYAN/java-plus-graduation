package ru.practicum.service;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.controller.EventController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.EventSimilarityAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.grpc.stats.recommendation.RecommendationMessage;
import ru.practicum.grpc.stats.recommendation.RecommendationsControllerGrpc;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.RecommendedEvent;
import ru.practicum.model.UserAction;
import ru.practicum.repository.EventSimilarityRepository;
import ru.practicum.repository.UserActionRepository;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class AnalyzeService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private static final Double VIEW_WEIGHT = 0.4;
    private static final Double REGISTER_WEIGHT = 0.8;
    private static final Double LIKE_WEIGHT = 1.0;
    private static final long NEIGHBORS_COUNT = 10;

    private final EventSimilarityRepository eventSimilarityRepository;
    private final UserActionRepository userActionRepository;
    private final EventController eventController;


    private final KafkaConsumer<String, EventSimilarityAvro> similarityKafkaConsumer;
    private final KafkaConsumer<String, UserActionAvro> actionsKafkaConsumer;

    @Value("${kafka.topics.similarity}")
    private String similarityTopic;

    @Value("${kafka.topics.actions}")
    private String actionsTopic;

    public void start() {
        similarityKafkaConsumer.subscribe(Collections.singletonList(similarityTopic));
        actionsKafkaConsumer.subscribe(Collections.singletonList(actionsTopic));

        while (true) {
            ConsumerRecords<String, EventSimilarityAvro> similarityRecords = similarityKafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, EventSimilarityAvro> record : similarityRecords) {
                EventSimilarityAvro eventSimilarityAvro = record.value();
                EventSimilarity similarity = new EventSimilarity();
                similarity.setEventIdA(eventSimilarityAvro.getEventA());
                similarity.setEventIdB(eventSimilarityAvro.getEventB());
                similarity.setSimilarityScore(eventSimilarityAvro.getScore());
                similarity.setCalculatedAt(eventSimilarityAvro
                        .getTimestamp()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                eventSimilarityRepository.save(similarity); // ✅ фикс
            }
            similarityKafkaConsumer.commitSync();

            ConsumerRecords<String, UserActionAvro> actionRecords = actionsKafkaConsumer.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, UserActionAvro> record : actionRecords) {
                UserActionAvro userActionAvro = record.value();
                UserAction action = new UserAction();
                action.setEventId(userActionAvro.getEventId());
                action.setUserId(userActionAvro.getUserId());
                action.setAction(userActionAvro.getActionType().toString());
                action.setTime(userActionAvro
                        .getTimestamp()
                        .atZone(ZoneId.systemDefault()).toLocalDateTime());
                userActionRepository.save(action); // ✅ фикс
            }
            actionsKafkaConsumer.commitSync();
        }
    }

    @Override
    public void getRecommendationsForUser(RecommendationMessage.UserPredictionsRequestProto request,
                                          StreamObserver<RecommendationMessage.RecommendedEventProto> responseObserver) {
        long userId = request.getUserId();
        int maxResults = request.getMaxResults();
        Pageable pageable = PageRequest.of(0, maxResults);

        try {
            List<Long> interactedEventIds = userActionRepository
                    .findActionsByUserIdOrderByTimestamp(userId, pageable)
                    .getContent();

            if (!interactedEventIds.isEmpty()) {
                List<Long> notInteractedEventIds = userActionRepository.findNotInteractedEventIdsByUserId(userId);

                List<Long> mostSimilarEventsIds = eventSimilarityRepository.findMostSimilarEventsIds(
                        interactedEventIds,
                        notInteractedEventIds,
                        pageable);

                for (int i = 0; i < mostSimilarEventsIds.size() && i < NEIGHBORS_COUNT; i++) {
                    Map<Long, Double> eventsAndSimilarities = eventSimilarityRepository
                            .findSimilarEvents(mostSimilarEventsIds.get(i), interactedEventIds)
                            .stream()
                            .collect(Collectors.toMap(
                                    RecommendedEvent::getId,
                                    RecommendedEvent::getSimilarityScore));

                    Map<Long, Double> eventsAndRatings = eventController
                            .findEventsByIds(eventsAndSimilarities.keySet())
                            .stream()
                            .collect(Collectors.toMap(
                                    EventFullDto::getId,
                                    EventFullDto::getRating));

                    double weightedMarksSum = 0.0;
                    for (Long eventId : eventsAndSimilarities.keySet()) {
                        weightedMarksSum += eventsAndSimilarities.get(eventId) * eventsAndRatings.getOrDefault(eventId, 0.0);
                    }

                    double sumSimilarities = eventsAndSimilarities.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .sum();

                    double result = sumSimilarities != 0 ? weightedMarksSum / sumSimilarities : 0.0;

                    RecommendationMessage.RecommendedEventProto event = RecommendationMessage.RecommendedEventProto.newBuilder()
                            .setEventId(mostSimilarEventsIds.get(i))
                            .setScore(result)
                            .build();
                    responseObserver.onNext(event);
                }
            }
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            responseObserver.onCompleted();
        }
    }

    @Override
    public void getSimilarEvents(RecommendationMessage.SimilarEventsRequestProto request,
                                 StreamObserver<RecommendationMessage.RecommendedEventProto> responseObserver) {
        List<Long> eventsByUserId = userActionRepository.findEventIdsByUserId(request.getUserId());
        Pageable pageable = PageRequest.of(0, request.getMaxResults());
        Page<EventSimilarity> eventSimilarities = eventSimilarityRepository.findSimilaritiesExcludingInteracted(
                request.getEventId(), eventsByUserId, pageable);

        for (EventSimilarity similarity : eventSimilarities) {
            long tempSimilarEventId = similarity.getEventIdA() == request.getEventId()
                    ? similarity.getEventIdB()
                    : similarity.getEventIdA();
            RecommendationMessage.RecommendedEventProto eventProto = RecommendationMessage.RecommendedEventProto.newBuilder()
                    .setEventId(tempSimilarEventId)
                    .setScore(similarity.getSimilarityScore())
                    .build();
            responseObserver.onNext(eventProto);
        }
        responseObserver.onCompleted();
    }

    @Override
    public void getInteractionsCount(RecommendationMessage.InteractionsCountRequestProto request,
                                     StreamObserver<RecommendationMessage.RecommendedEventProto> responseObserver) {
        request.getEventIdList().forEach(eventId -> {
            long likeCount = userActionRepository.countActionsByEventIdAndActionType(eventId, "LIKE");
            long registerCount = userActionRepository.countActionsByEventIdAndActionType(eventId, "REGISTER");
            long viewCount = userActionRepository.countActionsByEventIdAndActionType(eventId, "VIEW");

            double score = likeCount * LIKE_WEIGHT
                    + registerCount * REGISTER_WEIGHT
                    + viewCount * VIEW_WEIGHT;

            RecommendationMessage.RecommendedEventProto eventProto = RecommendationMessage
                    .RecommendedEventProto.newBuilder()
                    .setEventId(eventId)
                    .setScore(score)
                    .build();
            responseObserver.onNext(eventProto);
        });
        responseObserver.onCompleted();
    }
}
