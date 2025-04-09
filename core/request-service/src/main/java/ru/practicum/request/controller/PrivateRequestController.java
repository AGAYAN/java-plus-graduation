package ru.practicum.request.controller;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
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
    @ResponseStatus(HttpStatus.CREATED)
    private ParticipationRequestDto addRequest(@PathVariable @NonNull Long userId,
                                               @RequestParam @NonNull Long eventId) {
        return requestService.addRequest(userId, eventId);
    }

    @GetMapping("/{userId}/requests")
    public List<ParticipationRequestDto> getAllRequest(@RequestParam("userId") @NonNull List<Long> userId) {
        return requestService.getAll(userId);
    }

    @PatchMapping("/{userId}/requests/{requestId}/cancel")
    private ParticipationRequestDto cancel(@PathVariable @NonNull Long userId,
                                           @PathVariable @NonNull Long requestId) {
        return requestService.cancel(userId, requestId);
    }
}
