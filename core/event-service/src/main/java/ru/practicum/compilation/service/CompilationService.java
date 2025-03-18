package ru.practicum.compilation.service;

import ru.practicum.dto.compilation.CompilationDto;
import ru.practicum.dto.compilation.CompilationParam;
import ru.practicum.dto.compilation.NewCompilationDto;
import ru.practicum.dto.compilation.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

  CompilationDto save(NewCompilationDto compilationDto);

  void delete(Long compId);

  CompilationDto update(Long compId, UpdateCompilationRequest compDto);

  List<CompilationDto> get(CompilationParam searchParam);

  CompilationDto get(Long compId);
}
