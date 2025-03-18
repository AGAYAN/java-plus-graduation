package ru.practicum.request.service;


import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.request.ParticipationRequestDto;

import java.util.List;

public interface RequestService {

   ParticipationRequestDto addRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getAll(List<Long> userId);

    ParticipationRequestDto cancel(Long userId, Long requestId);

    List<ParticipationRequestDto> getRequests(final Long initiatorId, final Long eventId);

    EventRequestStatusUpdateResult updateRequestsStatus(
            final Long initiatorId,
            final Long eventId,
            final EventRequestStatusUpdateRequest updateStatusDto);
}
