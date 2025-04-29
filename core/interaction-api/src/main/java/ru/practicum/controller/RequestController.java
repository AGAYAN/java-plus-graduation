package ru.practicum.controller;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;


@FeignClient(name = "request-service", path = "/users")
public interface RequestController {

   @GetMapping("/request")
   List<ParticipationRequestDto> getAllRequests(@RequestParam List<Long> eventIds);

   @GetMapping("/request/full")
   void getAllRequestse(@RequestParam List<EventFullDto> eventIds);
}