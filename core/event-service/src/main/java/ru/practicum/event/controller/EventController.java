package ru.practicum.event.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.event.service.EventService;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events/feign")
public class EventController {
    private final EventService eventService;

    @GetMapping(path = "/events")
    public List<EventFullDto> findEventsByIds(@RequestParam Set<Long> ids) {
        return eventService.findEventByIds(ids);
    }
}
