package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationParam;

import java.util.List;

@RestController
@RequestMapping("/compilations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PublicCompilationController {

  private final CompilationService service;

  @GetMapping
  public ResponseEntity<List<CompilationDto>> get(@RequestParam(required = false) Boolean pinned,
                                                  @RequestParam(defaultValue = "0") @PositiveOrZero Integer from,
                                                  @RequestParam(defaultValue = "10") @Positive Integer size) {
    log.info(
        "Request received GET /compilations to retrieve compilation pinned = {}, from {}, size {}",
        pinned, from, size);
    final List<CompilationDto> compilations = service.get(new CompilationParam(pinned, from, size));
    log.info("Returning {} Compilations.", compilations.size());
    return ResponseEntity.status(HttpStatus.OK).body(compilations);
  }

  @GetMapping("/{compId}")
  public ResponseEntity<CompilationDto> get(@PathVariable("compId") @Positive Long compId) {
    log.info("Request received GET /compilations/{}.", compId);
    final CompilationDto compilation = service.get(compId);
    log.info("Returning Compilations with ID {}.", compilation.getId());
    return ResponseEntity.status(HttpStatus.OK).body(compilation);
  }

}
