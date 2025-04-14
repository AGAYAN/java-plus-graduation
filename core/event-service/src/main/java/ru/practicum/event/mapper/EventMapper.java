package ru.practicum.event.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import ru.practicum.category.mapper.CategoryMapper;

import ru.practicum.category.model.Category;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.user.UserDto;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@UtilityClass
@Slf4j
public class EventMapper {

  public static Event toEvent(final NewEventDto eventDto, final UserDto initiator, final CategoryDto category) {
    log.debug("Mapping NewEventDto {} to the Event.", eventDto);
    Objects.requireNonNull(eventDto);
    Objects.requireNonNull(initiator);
    return new Event()
        .setAnnotation(eventDto.getAnnotation())
        .setCategory(CategoryMapper.toCategory(category))
        .setDescription(eventDto.getDescription())
        .setEventDate(eventDto.getEventDate())
        .setLocation(eventDto.getLocation())
        .setPaid(eventDto.getPaid())
        .setCreatedOn(LocalDateTime.now())
        .setInitiatorId(initiator.getId())
        .setInitiator(initiator)
        .setParticipantLimit(eventDto.getParticipantLimit())
        .setTitle(eventDto.getTitle())
        .setRequestModeration(eventDto.getRequestModeration())
        .setState(State.PENDING);
  }

  public static EventFullDto toFullDto(final Event event) {
    log.debug("Mapping Event {} to the EventFullDto.", event);
    Objects.requireNonNull(event);
    return new EventFullDto()
        .setId(event.getId())
        .setAnnotation(event.getAnnotation())
        .setCategory(CategoryMapper.toCategoryDto(event.getCategory()))
        .setConfirmedRequests(event.getConfirmedRequests())
        .setEventDate(event.getEventDate())
        .setInitiator(event.getInitiator())
        .setPaid(event.getPaid())
        .setTitle(event.getTitle())
        .setViews(event.getViews())
        .setCreatedOn(event.getCreatedOn())
        .setDescription(event.getDescription())
        .setLocation(event.getLocation())
        .setParticipantLimit(event.getParticipantLimit())
        .setPublishedOn(event.getPublishedOn())
        .setRequestModeration(event.getRequestModeration())
        .setState(event.getState().name());
  }

  public static EventShortDto toShortDto(final Event event) {
    log.debug("Mapping Event {} to the EventShortDto.", event);
    Objects.requireNonNull(event);
    return new EventShortDto(
        event.getAnnotation(),
        CategoryMapper.toCategoryDto(event.getCategory()),
        event.getConfirmedRequests(),
        event.getEventDate(),
        event.getId(),
            event.getInitiator().getId(),
        event.getPaid(),
        event.getTitle(),
        event.getViews());
  }

  public static List<EventShortDto> toShortDto(final List<Event> events) {
    if (events == null || events.isEmpty()) {
      return Collections.emptyList();
    }
    return events.stream()
        .map(EventMapper::toShortDto)
        .toList();
  }

  public static Event updateEventFromAdminRequest(Event event, UpdateEventAdminRequest request, Category category) {
    if (request.getAnnotation() != null) {
      event.setAnnotation(request.getAnnotation());
    }
    if (request.getCategory() != null) {
      event.setCategory(category);
    }
    if (request.getDescription() != null) {
      event.setDescription(request.getDescription());
    }
    if (request.getEventDate() != null) {
      event.setEventDate(request.getEventDate());
    }
    if (request.getLocation() != null) {
      event.setLocation(request.getLocation());
    }
    if (request.getPaid() != null) {
      event.setPaid(request.getPaid());
    }
    if (request.getParticipantLimit() != null) {
      event.setParticipantLimit(request.getParticipantLimit());
    }
    if (request.getRequestModeration() != null) {
      event.setRequestModeration(request.getRequestModeration());
    }
    if (request.getTitle() != null) {
      event.setTitle(request.getTitle());
    }

    // обработка действия с состоянием
    if (request.getStateAction() != null) {
      switch (request.getStateAction()) {
        case "PUBLISH_EVENT" -> event.setState(State.PUBLISHED);
        case "REJECT_EVENT" -> event.setState(State.CANCELED);
        default -> throw new IllegalArgumentException("Unknown stateAction: " + request.getStateAction());
      }
    }

    return event;
  }


//  public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
//          final List<ParticipationRequest> confirmedRequests, final List<ParticipationRequest> rejectedRequests) {
//    log.debug("Mapping parameters to the EventRequestStatusUpdateResult.");
//    return new EventRequestStatusUpdateResult()
//        .setConfirmedRequests(RequestMapper.mapToDto(confirmedRequests))
//        .setRejectedRequests(RequestMapper.mapToDto(rejectedRequests));
//  }
}
