package ru.practicum.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.user.UserShortDto;


import java.time.LocalDateTime;

/**
 * Used in the PRIVATE API - as RESPONSE
 * <p> GET /users/{userId}/events
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class EventShortDto {

  private String annotation;
  private CategoryDto category;
  private Integer confirmedRequests;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime eventDate;
  private Long id;
  private Long initiator; // переделать
  private Boolean paid;
  private String title;
  private Long views;

  public EventShortDto(String annotation, CategoryDto category, LocalDateTime eventDate, Long id, Long initiator, Boolean paid, String title) {
    this.annotation = annotation;
    this.category = category;
    this.eventDate = eventDate;
    this.id = id;
    this.initiator = initiator;
    this.paid = paid;
    this.title = title;
  }
}
