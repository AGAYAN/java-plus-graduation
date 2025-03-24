package ru.practicum.event.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * Is used in the PRIVATE APIs - as RESPONSE
 * <p>
 * GET /users/{userId}/requests
 * <p>
 * POST /users/{userId}/requests
 * <p>
 * PATCH /users/{userId}/requests/{requestId}/cancel
 * <p>
 * GET /users/{userId}/events/{eventId}/requests
 * <p>
 * PATCH /users/{userId}/events/{eventId}/requests
 *
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Accessors(chain = true)
public class ParticipationRequestDto {

  private Long id;
  private Long eventId;
  private Long requester;
  private String status;
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  private LocalDateTime created = LocalDateTime.now();
}


