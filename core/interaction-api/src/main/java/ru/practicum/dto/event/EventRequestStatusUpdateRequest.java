package ru.practicum.dto.event;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import ru.practicum.dto.request.StatusRequest;



import java.util.List;


/**
 * Used In the PRIVATE APIs - as REQUEST body
 * <p> PATCH /users/{userId}/events/{eventId}/requests
 */
@Getter
@Setter
public class EventRequestStatusUpdateRequest {

  @NotNull(message = "RequestIds cannot be null.")
  @NotEmpty(message = "RequestIds cannot be empty.")
  private List<Long> requestIds;

  @NotNull(message = "Status cannot be null.") // исправить
  private StatusRequest status;

}
