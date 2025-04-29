package ru.practicum.compilation.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.compilation.mapper.CompilationMapper;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.compilation.repository.CompilationRepository;
import ru.practicum.controller.EventController;
import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationParam;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.event.service.EventService;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CompilationServiceImpl implements CompilationService {

  private final CompilationRepository compilationRepository;
  private final EventService eventService;
  private final EventController eventController;
  private final EventRepository eventRepository;
  private CompilationMapper compilationMapper;

  /**
   * Saves new compilation; may contain NO events.
   */
  @Override
  public CompilationDto save(final NewCompilationDto compilationDto) {
    if (compilationDto.getTitle() == null || compilationDto.getTitle().isBlank()) {
      throw new ValidationException("Поле title не может быть пустой или состоять из пробела");
    }

    return compilationMapper.toCompilationDto(compilationRepository
            .save(compilationMapper.toCompilation(compilationDto,
                    eventRepository.findAllByIdIn(compilationDto.getEvents()))));
  }

  /**
   * Removes compilation with given ID from the DB.
   */
  @Override
  public void delete(final Long compId) {
    log.debug("Deleting the compilation with ID = {}", compId);
    validateExists(compId);
    compilationRepository.deleteById(compId);
    log.debug("Compilation with ID = {} has been successfully deleted.", compId);
  }

  /**
   * Updates compilations by ID with new given data.
   */
  @Override
  public CompilationDto update(final Long compId, final UpdateCompilationRequest compDto) {
    log.debug("Updating the compilation ID = {} with data {}.", compId, compDto);
    validateExists(compId);
    final Compilation compilation = fetchCompilation(compId);
    patchCompilationData(compilation, compDto);
    try {
      compilationRepository.save(compilation);
      return CompilationMapper.toCompilationDto(compilation);
    } catch (ConstraintViolationException exception) {
      log.warn("Could not update compilation. Title should be unique.");
      throw new ConflictException("Could not execute statement. Title should be unique.");
    }
  }

  /**
   * Retrieves Event Compilations by specified filters defined in the CompilationParam.
   */
  @Override
  public List<CompilationDto> get(final CompilationParam searchParam) {
    Objects.requireNonNull(searchParam);
    final List<Compilation> compilations = compilationRepository.findAllBy(searchParam);
    return CompilationMapper.toCompilationDto(compilations);
  }

  /**
   * Retrieves Event Compilation info by its ID.
   */
  @Override
  public CompilationDto get(final Long compId) {
    final Compilation compilation = fetchCompilation(compId);
    return CompilationMapper.toCompilationDto(compilation);
  }

  private void patchCompilationData(final Compilation target, final UpdateCompilationRequest dataSource) {
    log.debug("Apply the patch on Compilation fields.");
    Optional.ofNullable(dataSource.getPinned()).ifPresent(target::setPinned);
    Optional.ofNullable(dataSource.getTitle()).ifPresent(target::setTitle);
    Optional.ofNullable(dataSource.getEvents()).ifPresent(eventIds ->
            target.setEvents(eventService.getEvents(eventIds)));
  }

  private Compilation fetchCompilation(final Long compId) {
    log.debug("Fetching Compilation record ID {}.", compId);
    return compilationRepository.findByIdEnriched(compId)
        .orElseThrow(() -> {
          log.warn("Compilation with ID {} was not found.", compId);
          return new NotFoundException("The required Compilation record was not found.");
        });
  }

  private void validateExists(final Long compId) {
    log.debug("Validating Compilation record ID {} exists in the DB.", compId);
    if (!compilationRepository.existsById(compId)) {
      log.warn("Compilation ID {} was not found.", compId);
      throw new NotFoundException("The required Compilation record was not found.");
    }
  }
}
