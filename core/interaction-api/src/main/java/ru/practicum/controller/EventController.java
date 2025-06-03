package ru.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;

import java.util.List;
import java.util.Set;

@FeignClient(name = "event-service")
public interface EventController {

    @GetMapping("/events/{eventId}")
    EventFullDto getEventById(@PathVariable Long eventId);

    @GetMapping(path = "/events")
    List<EventFullDto> findEventsByIds(@RequestParam Set<Long> ids);
}