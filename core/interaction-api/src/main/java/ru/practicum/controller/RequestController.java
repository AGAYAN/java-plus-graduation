package ru.practicum.controller;

import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

@FeignClient(name = "request-service", path = "/users/{userId}/requests")
public interface RequestController {
    @GetMapping
    List<ParticipationRequestDto> getAllRequest(@PathVariable @NonNull List<Long> userId);
}
