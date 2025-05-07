package ru.practicum.request.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.service.RequestService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/users")
public class PrivateRequestController {

    private final RequestService requestService;

    @PostMapping("/{userId}/requests")
    public ResponseEntity<ParticipationRequestDto> addRequest(
            @PathVariable @NonNull Long userId,
            @RequestParam @NonNull Long eventId) {
        ParticipationRequestDto result = requestService.addRequest(userId, eventId);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getAllRequest(@PathVariable @NonNull Long userId) {
        return requestService.getAll(List.of(userId));
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    private ParticipationRequestDto cancel(@PathVariable @NonNull Long userId,
                                           @PathVariable @NonNull Long requestId) {
        return requestService.cancel(userId, requestId);
    }

    @GetMapping("/request")
    public List<ParticipationRequestDto> getAllRequests(@RequestParam List<Long> eventIds) {
        return requestService.getConfirmedRequestsByEventIds(eventIds);
    }


    @GetMapping("/request/full")
    public void getAllRequestse(@RequestParam List<EventFullDto> eventIds) {
        requestService.setConfirmedRequests(eventIds);
    }

}
