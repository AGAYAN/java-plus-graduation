package ru.practicum.request.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/events")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PrivateEventController {

    private RequestService requestService;

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
    public ResponseEntity<EventRequestStatusUpdateResult> changeRequestStatus(
            @PathVariable("userId") @NotNull @Positive Long userId,
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Validated @RequestBody EventRequestStatusUpdateRequest updateStatusDto) {
        log.info("Request received Patch /users/{}/events/{}/requests, with data: {}",
                userId, eventId, updateStatusDto);
        final EventRequestStatusUpdateResult result = requestService.updateRequestsStatus(userId, eventId, updateStatusDto);
        log.info("Event participation requests statuses updated {}.", result);
        return ResponseEntity.ok(result);
    }
}
