package ru.practicum.event.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.*;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.event.service.EventService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

  private final EventService eventService;

  @PostMapping
  public ResponseEntity<EventFullDto> addEvent(
      @PathVariable("userId") @NotNull @Positive Long userId,
      @Validated @RequestBody NewEventDto eventDto) {
    log.info("Request received POST /users/{}/events to add event {}", userId, eventDto);
    final EventFullDto eventSaved = eventService.addEvent(userId, eventDto);
    log.info("Event added successfully with ID={}.", eventSaved.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(eventSaved);
  }

  @GetMapping
  public ResponseEntity<List<EventShortDto>> getEvents(
      @PathVariable("userId") @NotNull @Positive Long userId,
      @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
      @RequestParam(defaultValue = "10") @Positive Integer size) {
    log.info("Request received GET /users/{}/events?from={}&size={}", userId, from, size);
    final List<EventShortDto> result = eventService.getEvents(userId,from,size);
    log.info("Sending event list size {}.", result.size());
    return ResponseEntity.ok(result);
  }

  @GetMapping("/{eventId}")
  public ResponseEntity<EventFullDto> getEvent(
      @PathVariable("userId") @NotNull @Positive Long userId,
      @PathVariable("eventId")@NotNull @Positive Long eventId) {
    log.info("Request received GET /users/{}/events/{}", userId, eventId);
    final EventFullDto result = eventService.getEvent(userId,eventId);
    log.info("Sending event ID={} data.", result.getId());
    return ResponseEntity.ok(result);
  }

  @PatchMapping("/{eventId}")
  public ResponseEntity<EventFullDto> updateEvent(
      @PathVariable("userId") @NotNull @Positive Long userId,
      @PathVariable("eventId")@NotNull @Positive Long eventId,
      @Validated @RequestBody UpdateEventUserRequest eventDto) {
    log.info("Request received Patch /users/{}/events/{} with data {}.",
        userId, eventId,eventDto);
    final EventFullDto eventUpdated = eventService.updateEvent(userId, eventId, eventDto);
    log.info("Event updated successfully with ID={}.", eventUpdated.getId());
    return ResponseEntity.status(HttpStatus.OK).body(eventUpdated);
  }

//  @GetMapping("/{eventId}/requests")
//  public ResponseEntity<List<ParticipationRequestDto>> getEventParticipants(
//      @PathVariable("userId") @NotNull @Positive Long userId,
//      @PathVariable("eventId") @NotNull @Positive Long eventId) {
//    log.info("Request received GET /users/{}/events/{}/requests", userId, eventId);
//    final List<ParticipationRequestDto> result = eventService.getRequests(userId, eventId);
//    log.info("Sending event participant list size {}.", result.size());
//    return ResponseEntity.ok(result);
//  }
//
//  @PatchMapping("/{eventId}/requests")
//  public ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatus(
//      @PathVariable("userId") @NotNull @Positive Long userId,
//      @PathVariable("eventId") @NotNull @Positive Long eventId,
//      @Validated @RequestBody EventRequestStatusUpdateRequest updateStatusDto) {
//    log.info("Request received Patch /users/{}/events/{}/requests, with data: {}",
//        userId, eventId, updateStatusDto);
//    final EventRequestStatusUpdateResult result = eventService.updateRequestsStatus(userId, eventId, updateStatusDto);
//    log.info("Event participation requests statuses updated {}.", result);
//    return ResponseEntity.ok(result);
//  }
}
