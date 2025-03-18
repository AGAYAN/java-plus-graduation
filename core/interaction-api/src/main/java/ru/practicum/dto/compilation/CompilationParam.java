package ru.practicum.dto.compilation;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CompilationParam {

  private  Boolean pinned;

  private int from;

  private int size;

}
