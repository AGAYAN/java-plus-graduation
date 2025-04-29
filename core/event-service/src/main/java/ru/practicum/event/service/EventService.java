package ru.practicum.event.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Pageable;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.event.model.Event;


import java.util.List;
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

  /**
   * Retrieves information about participation requests for the current user's event.
   */
 //List<ParticipationRequestDto> getRequests(Long initiatorId, Long eventId);

  /**
   * Updates the participation request statuses for the specified event of the current user. The
   * statuses can be changed to either {@code CONFIRMED} or {@code REJECTED}.
   */
  //EventRequestStatusUpdateResult updateRequestsStatus(Long userId, Long eventId, EventRequestStatusUpdateRequest updateStatusDto);

}
