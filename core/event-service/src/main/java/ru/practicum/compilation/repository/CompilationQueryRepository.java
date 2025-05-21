package ru.practicum.compilation.repository;

import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.compilation.CompilationParam;

import java.util.List;

public interface CompilationQueryRepository {

  List<Compilation> findAllBy(CompilationParam searchParam);

}
