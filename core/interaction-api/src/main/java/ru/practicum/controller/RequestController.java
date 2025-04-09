package ru.practicum.controller;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NonNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;


@FeignClient(name = "request-service")
public interface RequestController {

    @GetMapping("/users/requests")
    List<ParticipationRequestDto> getAllRequest(@RequestParam("userId") @NonNull List<Long> userId);

}