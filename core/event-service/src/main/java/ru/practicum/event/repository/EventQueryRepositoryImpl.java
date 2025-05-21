package ru.practicum.event.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import ru.practicum.category.model.Category;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventShortDto;
import ru.practicum.dto.event.enums.SortType;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;
import ru.practicum.event.service.EventService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final EntityManager entityManager;
    private EventService eventService;

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
        List<EventFullDto> resultList = eventService.mapToEventFullDtos(tuples);

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

        Join<Event, Category> categoryJoin = eventTable.join("category");


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

        List<EventShortDto> resultList = eventService.mapToEventShortDtos(tuples);

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

        Map<String, Long> viewsMap = eventService.getViewsForEvents(rangeStart, rangeEnd, uris);

        eventFullDtos.forEach(event -> {
            String uri = "/events/" + event.getId();
            event.setViews(viewsMap.getOrDefault(uri, 0L));
        });

        Map<Long, Long> confirmedRequestsMap = eventService.getConfirmedRequests(
            eventFullDtos.stream().map(EventShortDto::getId).collect(Collectors.toList())
        );

        eventFullDtos.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(),
            0L).intValue()));
    }

    private void populateEventDetails(List<EventFullDto> eventFullDtos, LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<String> uris = eventFullDtos.stream()
            .map(event -> "/events/" + event.getId())
            .collect(Collectors.toList());

        Map<String, Long> viewsMap = eventService.getViewsForEvents(rangeStart, rangeEnd, uris);

        eventFullDtos.forEach(event -> {
            String uri = "/events/" + event.getId();
            event.setViews(viewsMap.getOrDefault(uri, 0L));
        });

        Map<Long, Long> confirmedRequestsMap = eventService.getConfirmedRequests(
            eventFullDtos.stream().map(EventFullDto::getId).collect(Collectors.toList())
        );

        eventFullDtos.forEach(event -> event.setConfirmedRequests(confirmedRequestsMap.getOrDefault(event.getId(),
            0L).intValue()));
    }


}