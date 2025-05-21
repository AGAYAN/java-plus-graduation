package ru.practicum.event.service;

import jakarta.persistence.Tuple;
import jakarta.servlet.http.HttpServletRequest;
import ru.practicum.dto.event.*;
import ru.practicum.event.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventService {

  /**
   * Saves a new event data initiated by a current user.
   */
  EventFullDto addEvent(Long initiatorId, NewEventDto eventDto);

  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   */
  EventFullDto updateEvent(long eventId, UpdateEventAdminRequest param);

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   */
  EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest eventDto);

  /**
   * Retrieves complete data about specific event created by a current user.
   */
  EventFullDto getEvent(Long initiatorId, Long eventId);

  /**
   * Retrieves detailed information about a published event by its ID.
   */
  EventFullDto getEvent(Long eventId);

  /**
   * Retrieves all existed in DB events (performed by ADMIN).
   */
  List<EventFullDto> getEvents(GetEventAdminRequest param);

  /**
   * Retries all events created by current user.
   */
  List<EventShortDto> getEvents(Long initiatorId, Integer from, Integer size);

  /**
   * Retrieving published events with filtering options.
   */
  List<EventShortDto> getEvents(GetEventPublicParam param, HttpServletRequest request);

  /**
   *  Retrieves a set of events based on the provided event IDs.
   */
  Set<Event> getEvents(Set<Long> events);

  Map<String, Long> getViewsForEvents(LocalDateTime rangeStart, LocalDateTime rangeEnd, List<java.lang.String> uris);

  List<EventFullDto> mapToEventFullDtos(List<Tuple> tuples);

  List<EventShortDto> mapToEventShortDtos(List<Tuple> tuples);

  Map<Long, Long> getConfirmedRequests(List<Long> eventIds);

  }
