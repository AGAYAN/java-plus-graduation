package ru.practicum.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.UpdateEventUserRequest;

@FeignClient(name = "event-service", path = "/events")
public interface EventController {

    @GetMapping("/{eventId}")
    EventFullDto getEvent(@PathVariable("eventId") Long eventId);

    @PatchMapping("/{eventId}")
    ResponseEntity<EventFullDto> updateEvent(
            @PathVariable("eventId") @NotNull @Positive Long eventId,
            @Validated @RequestBody UpdateEventUserRequest eventDto);
}