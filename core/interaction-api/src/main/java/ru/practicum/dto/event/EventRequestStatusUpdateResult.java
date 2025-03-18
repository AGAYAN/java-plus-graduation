package ru.practicum.dto.event;

import lombok.Data;
import lombok.experimental.Accessors;
import ru.practicum.dto.request.ParticipationRequestDto;


import java.util.ArrayList;
import java.util.List;

/**
 * Used in the PRIVATE API as RESPONSE
 * <p>
 * PATCH /users/{userId}/events/{eventId}/requests
 */
@Data
@Accessors(chain = true)
public class EventRequestStatusUpdateResult {

  private List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
  private List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();
 }
