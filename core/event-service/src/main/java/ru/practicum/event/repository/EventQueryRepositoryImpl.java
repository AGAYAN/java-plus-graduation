package ru.practicum.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;

import ru.practicum.category.model.Category;
import ru.practicum.controller.RequestController;
import ru.practicum.controller.UserController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.SortType;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.StatusRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.model.Location;


import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final EntityManager entityManager;
    private UserController userController;
    private final StatsClient statsClient;
    private RequestController requestController;

    @Autowired
    public EventQueryRepositoryImpl(EntityManager entityManager, RequestController requestController, StatsClient statsClient) {
        this.requestController = requestController;
        this.entityManager = entityManager;
        this.statsClient = statsClient;
    }

    public EventQueryRepositoryImpl(final EntityManager entityManager, StatsClient statsClient) {
        this.entityManager = entityManager;
        this.statsClient = statsClient;
    }

    @Override
    public List<EventFullDto> adminFindEvents(final List<Long> users,
                                              final List<String> states,
                                              final List<Long> categories,
                                              final LocalDateTime rangeStart,
                                              final LocalDateTime rangeEnd,
                                              int from,
                                              int size) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Event> eventTable = query.from(Event.class);

        Join<Event, Category> categoryJoin = eventTable.join("category");


        query.multiselect(
                eventTable.get("annotation").alias("annotation"),
                categoryJoin.get("id").alias("categoryId"),
                categoryJoin.get("name").alias("categoryName"),
                eventTable.get("eventDate").alias("eventDate"),
                eventTable.get("id").alias("eventId"),
                eventTable.get("initiatorId").alias("initiatorId"),
                eventTable.get("paid").alias("paid"),
                eventTable.get("title").alias("title"),
                eventTable.get("description").alias("description"),
                eventTable.get("createdOn").alias("createdOn"),
                eventTable.get("publishedOn").alias("publishedOn"),
                eventTable.get("participantLimit").alias("participantLimit"),
                eventTable.get("state").alias("state"),
                eventTable.get("location").get("lat").alias("lat"),
                eventTable.get("location").get("lon").alias("lon"),
                eventTable.get("requestModeration").alias("requestModeration")
        );

        Predicate predicate = cb.conjunction();

        if (users != null && !users.isEmpty()) {
            predicate = cb.and(predicate, eventTable.get("initiatorId").in(users));
        }

        if (states != null && !states.isEmpty()) {
            List<State> stateEnums = states.stream()
                    .map(State::valueOf) // Преобразуем строку в перечисление
                    .collect(Collectors.toList());

            predicate = cb.and(predicate, eventTable.get("state").in(stateEnums));
        }

        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, categoryJoin.get("id").in(categories));
        }

        if (rangeStart != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), rangeStart));
        }

        if (rangeEnd != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(eventTable.get("eventDate"), rangeEnd));
        }

        query.where(predicate);

        List<Tuple> tuples = fetchResults(query, from, size);
        List<EventFullDto> resultList = mapToEventFullDtos(tuples);

        populateEventDetails(resultList, rangeStart, rangeEnd);

        return resultList;
    }

    @Override
    public List<EventShortDto> publicGetEvents(final String text,
                                               final List<Long> categories,
                                               final Boolean paid,
                                               final LocalDateTime rangeStart,
                                               final LocalDateTime rangeEnd,
                                               final Boolean onlyAvailable,
                                               final SortType sort,
                                               final int from,
                                               final int size) {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Event> eventTable = query.from(Event.class);

        Join<EventFullDto, Category> categoryJoin = eventTable.join("category");


        query.multiselect(
                eventTable.get("annotation").alias("annotation"),
                categoryJoin.get("id").alias("categoryId"),
                categoryJoin.get("name").alias("categoryName"),
                eventTable.get("eventDate").alias("eventDate"),
                eventTable.get("id").alias("eventId"),
                eventTable.get("initiatorId").alias("initiatorId"),
                eventTable.get("paid").alias("paid"),
                eventTable.get("title").alias("title")
        );

        Predicate predicate = cb.conjunction();
        predicate = cb.and(predicate, cb.equal(eventTable.get("state"), State.PUBLISHED));

        if (text != null && !text.trim().isEmpty()) {
            String pattern = "%" + text.trim().toLowerCase() + "%";
            predicate = cb.and(predicate,
                    cb.or(
                            cb.like(cb.lower(eventTable.get("annotation")), pattern),
                            cb.like(cb.lower(eventTable.get("description")), pattern)
                    )
            );
        }

        if (categories != null && !categories.isEmpty()) {
            predicate = cb.and(predicate, categoryJoin.get("id").in(categories));
        }

        if (paid != null) {
            predicate = cb.and(predicate, cb.equal(eventTable.get("paid"), paid));
        }

        LocalDateTime now = LocalDateTime.now();
        if (rangeStart != null) {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), rangeStart));
        } else {
            predicate = cb.and(predicate, cb.greaterThanOrEqualTo(eventTable.get("eventDate"), now));
        }
        if (rangeEnd != null) {
            predicate = cb.and(predicate, cb.lessThanOrEqualTo(eventTable.get("eventDate"), rangeEnd));
        }

        query.where(predicate);

        if (sort != null && sort.equals(SortType.EVENT_DATE)) {
            query.orderBy(cb.asc(eventTable.get("eventDate")));
        }

        List<Tuple> tuples = fetchResults(query, from, size);

        List<EventShortDto> resultList = mapToEventShortDtos(tuples);

        populateEventShortDetails(resultList, rangeStart, rangeEnd);

        if (sort != null && sort.equals(SortType.VIEWS)) {
            resultList.sort(Comparator.comparing(EventShortDto::getViews).reversed());
        }

        return resultList;
    }

    private List<Tuple> fetchResults(CriteriaQuery<Tuple> query, int from, int size) {
        TypedQuery<Tuple> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(from);
        typedQuery.setMaxResults(size);
        return typedQuery.getResultList();
    }

    private void populateEventShortDetails(List<EventShortDto> eventFullDtos, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<String> uris = eventFullDtos.stream()
            .map(event -> "/events/" + event.getId())
            .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsForEvents(rangeStart, rangeEnd, uris);

        eventFullDtos.forEach(event -> {
            String uri = "/events/" + event.getId();
            event.setViews(viewsMap.getOrDefault(uri, 0L));
        });

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(
            eventFullDtos.stream().map(EventShortDto::getId).collect(Collectors.toList())
        );

        eventFullDtos.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(),
            0L).intValue()));
    }

    private void populateEventDetails(List<EventFullDto> eventFullDtos, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<String> uris = eventFullDtos.stream()
            .map(event -> "/events/" + event.getId())
            .collect(Collectors.toList());

        Map<String, Long> viewsMap = getViewsForEvents(rangeStart, rangeEnd, uris);

        eventFullDtos.forEach(event -> {
            String uri = "/events/" + event.getId();
            event.setViews(viewsMap.getOrDefault(uri, 0L));
        });

        Map<Long, Long> confirmedRequestsMap = getConfirmedRequests(
            eventFullDtos.stream().map(EventFullDto::getId).collect(Collectors.toList())
        );

        eventFullDtos.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(),
            0L).intValue()));
    }

    private Map<String, Long> getViewsForEvents(LocalDateTime rangeStart, LocalDateTime rangeEnd, List<String> uris) {
        String start = rangeStart != null ? rangeStart.toString() : LocalDateTime.now().toString();
        String end = rangeEnd != null ? rangeEnd.toString() : LocalDateTime.now().toString();

        ViewStatsDto[] stats = statsClient.getStats(start, end, uris.toArray(new String[0]), true);

        return Arrays.stream(stats)
                .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits, (a, b) -> b));
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
        if (eventIds == null || eventIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ParticipationRequestDto> requestDtos = requestController.getAllRequests(eventIds);

        return requestDtos.stream()
                .filter(request -> StatusRequest.CONFIRMED.name().equals(request.getStatus()))
                .collect(Collectors.groupingBy(
                        ParticipationRequestDto::getEvent,
                        Collectors.counting()
                ));
    }

    private List<EventFullDto> mapToEventFullDtos(List<Tuple> tuples) {
        return tuples.stream().map(tuple -> {
            String state = Optional.ofNullable(tuple.get("state", State.class))
                    .map(State::name)
                    .orElse("");

            Long initiatorId = tuple.get("initiatorId", Long.class);
            UserDto initiator;

            try {
                initiator = userController.getUser(initiatorId);
            } catch (Exception e) {
                initiator = new UserDto();
                initiator.setId(initiatorId);
                initiator.setName("Неизвестный пользователь");
            }

            return new EventFullDto(
                    tuple.get("annotation", String.class),
                    new CategoryDto(
                            tuple.get("categoryId", Long.class),
                            tuple.get("categoryName", String.class)
                    ),
                    0,
                    tuple.get("createdOn", LocalDateTime.class),
                    Optional.ofNullable(tuple.get("description", String.class)).orElse(""),
                    tuple.get("eventDate", LocalDateTime.class),
                    tuple.get("eventId", Long.class),
                    initiator,
                    new Location(
                            tuple.get("lat", Float.class),
                            tuple.get("lon", Float.class)
                    ),
                    tuple.get("paid", Boolean.class),
                    tuple.get("participantLimit", Integer.class),
                    tuple.get("publishedOn", LocalDateTime.class),
                    tuple.get("requestModeration", Boolean.class),
                    state,
                    tuple.get("title", String.class)
            );
        }).collect(Collectors.toList());
    }

    private List<EventShortDto> mapToEventShortDtos(List<Tuple> tuples) {
        return tuples.stream()
                .map(tuple -> {
                    Long initiatorId = tuple.get("initiatorId", Long.class);
                    UserDto initiator;

                    try {
                        initiator = userController.getUser(initiatorId);
                    } catch (Exception e) {
                        initiator = new UserDto();
                        initiator.setId(initiatorId);
                        initiator.setName("Неизвестный пользователь");
                    }

                    return new EventShortDto(
                            tuple.get("annotation", String.class),
                            new CategoryDto(
                                    tuple.get("categoryId", Long.class),
                                    tuple.get("categoryName", String.class)
                            ),
                            tuple.get("eventDate", LocalDateTime.class),
                            tuple.get("eventId", Long.class),
                            new UserShortDto(
                                    initiator.getId(),
                                    initiator.getName()
                            ).getId(),
                            tuple.get("paid", Boolean.class),
                            tuple.get("title", String.class)
                    );
                })
                .collect(Collectors.toList());
    }

}