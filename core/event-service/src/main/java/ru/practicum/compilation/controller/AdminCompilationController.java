package ru.practicum.compilation.controller;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.compilation.service.CompilationService;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

@RestController
@RequestMapping("/admin/compilations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class AdminCompilationController {

  private final CompilationService service;

  @PostMapping
  public ResponseEntity<CompilationDto> saveCompilation(
      @Validated @RequestBody NewCompilationDto compDto) {
    log.info("Request received POST /admin/compilations to save compilation {}", compDto);

    final CompilationDto savedCompilation = service.save(compDto);
    log.info("Compilation saved successfully with ID={}.", savedCompilation.getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(savedCompilation);
  }

  @DeleteMapping("/{compId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteCompilation(@PathVariable("compId") @Positive Long compId) {
    log.info("Request received DELETE /admin/compilations/{} to delete compilation.", compId);
    service.delete(compId);
    log.info("Compilation deleted successfully.");
  }

  @PatchMapping("/{compId}")
  public ResponseEntity<CompilationDto> updateCompilation(@PathVariable("compId") @Positive Long compId,
      @Validated @RequestBody UpdateCompilationRequest compDto) {
    log.info("Request received PATCH /admin/compilations/{} to update compilation with data {}.",
        compId, compDto);
    final CompilationDto updatedCompilation = service.update(compId, compDto);
    log.info("Compilation ID={} updated successfully.", updatedCompilation.getId());
    return ResponseEntity.status(HttpStatus.OK).body(updatedCompilation);
  }

}
