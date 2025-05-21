package ru.practicum.request.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.exception.ConflictException;
import ru.practicum.request.service.RequestService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users/{userId}/events")
@Slf4j
@Validated
public class PrivateEventController {

    private final RequestService requestService;

    @Autowired
    public PrivateEventController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/{eventId}/requests")
    public ResponseEntity<List<ParticipationRequestDto>> getEventParticipants(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId) {
        log.info("Request received GET /users/{}/events/{}/requests", userId, eventId);
        final List<ParticipationRequestDto> result = requestService.getRequests(userId, eventId);
        log.info("Sending event participant list size {}.", result.size());
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{eventId}/requests")
    public ResponseEntity<?> changeRequestStatus(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Validated @RequestBody EventRequestStatusUpdateRequest updateStatusDto) {
        log.info("Request received Patch /users/{}/events/{}/requests, with data: {}", userId, eventId, updateStatusDto);
        try {
            EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(userId, eventId, updateStatusDto);
            log.info("Event participation requests statuses updated {}.", result);
            return ResponseEntity.ok(result);
        } catch (ConflictException ex) {
            log.warn("Conflict when updating request statuses: {}", ex.getMessage());
            Map<String, String> errorBody = Map.of("error", ex.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);
        }
    }
}
