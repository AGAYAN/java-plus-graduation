package ru.practicum.request.mapper;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.request.model.ParticipationRequest;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

@UtilityClass
@Slf4j
public class RequestMapper {

  public ParticipationRequestDto mapToDto(final ParticipationRequest participationRequest) {
    log.debug("Mapping participationRequestDto {} to RequestDto.", participationRequest);
    Objects.requireNonNull(participationRequest);
    return new ParticipationRequestDto()
            .setId(participationRequest.getId())
            .setRequester(participationRequest.getRequester() != null
                    ? participationRequest.getRequester()
                    : null)
            .setEvent(participationRequest.getEvent() != null
                    ? participationRequest.getEvent()
                    : null)
            .setCreated(participationRequest.getCreated())
            .setStatus(participationRequest.getStatus().name());
  }

  public List<ParticipationRequestDto> mapToDto(final List<ParticipationRequest> requests) {
    if (requests == null || requests.isEmpty()) {
      return Collections.emptyList();
    }
    return requests.stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  public static EventRequestStatusUpdateResult toEventRequestStatusUpdateResult(
          final List<ParticipationRequest> confirmedRequests, final List<ParticipationRequest> rejectedRequests) {
    log.debug("Mapping parameters to the EventRequestStatusUpdateResult.");
    return new EventRequestStatusUpdateResult()
            .setConfirmedRequests(RequestMapper.mapToDto(confirmedRequests))
            .setRejectedRequests(RequestMapper.mapToDto(rejectedRequests));
  }

}
