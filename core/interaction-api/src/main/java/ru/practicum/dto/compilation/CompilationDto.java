package ru.practicum.dto.compilation;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.dto.event.EventShortDto;

import java.util.Set;

/**
 * Is used in the Admin API - as RESPONSE
 * <p>
 * POST /admin/compilations
 */
@Data
@Accessors(chain = true)
public class CompilationDto {

  private Long id;

  private Boolean pinned;

  private String title;

  private Set<EventShortDto> events;
}
