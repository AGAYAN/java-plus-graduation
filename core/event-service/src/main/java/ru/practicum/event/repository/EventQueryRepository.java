package ru.practicum.event.repository;

import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventQueryRepository {

    List<Event> adminFindEvents(List<Long> users,
                                List<String> states,
                                List<Long> categories,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                int from,
                                int size);

    List<Event> publicGetEvents(String text,
                                List<Long> categories,
                                Boolean paid,
                                LocalDateTime rangeStart,
                                LocalDateTime rangeEnd,
                                Boolean onlyAvailable,
                                String sort,
                                int from,
                                int size);



}
