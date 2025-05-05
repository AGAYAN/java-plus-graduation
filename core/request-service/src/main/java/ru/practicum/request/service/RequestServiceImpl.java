package ru.practicum.request.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.controller.EventController;
import ru.practicum.controller.UserController;
import ru.practicum.dto.event.EventFullDto;
import ru.practicum.dto.event.EventRequestStatusUpdateRequest;
import ru.practicum.dto.event.EventRequestStatusUpdateResult;
import ru.practicum.dto.event.enums.State;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.StatusRequest;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.request.mapper.RequestMapper;
import ru.practicum.request.model.ParticipationRequest;
import ru.practicum.request.repository.RequestRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {

  private final RequestRepository requestRepository;
  private final UserController userClient;
  private final EventController eventClient;

  @Override
  @Transactional
  public ParticipationRequestDto addRequest(final Long userId, final Long eventId) {
    Long user = userClient.getUser(userId).getId();

    if (eventId == null || eventId <= 0) {
      throw new IllegalArgumentException("ID события должен быть больше 0");
    }

    EventFullDto eventResponse;
    try {
      eventResponse = eventClient.getEventById(eventId);
    } catch (FeignException.NotFound e) {
      throw new ConflictException("Событие с id " + eventId + " не найдено");
    } catch (FeignException e) {
      throw new ConflictException("Ошибка обращения к event-service: " + e.getMessage());
    }

    if (eventResponse == null) {
      throw new NotFoundException("Событие не найдено");
    }

    log.info("Получено событие: {}", eventResponse);

    if (eventResponse.getInitiator() == null || eventResponse.getInitiator().getId() == null) {
      throw new ConflictException("У события отсутствует инициатор");
    }
    if (userId.equals(eventResponse.getInitiator().getId())) {
      throw new ConflictException("Нельзя подать заявку на участие в своём собственном событии");
    }

    if (!eventResponse.getState().equals("PUBLISHED")) {
      throw new ConflictException("Нельзя участвовать в неопубликованном событии");
    }

    ParticipationRequest existingRequest = requestRepository.findByRequesterAndEvent(userId, eventId);

    if (existingRequest != null && !StatusRequest.CANCELED.equals(existingRequest.getStatus())) {
      throw new ConflictException("Вы уже отправили заявку на участие в этом событии");
    }

    int confirmedRequests = requestRepository.countAllByEventAndStatus(eventId, StatusRequest.CONFIRMED);

    ParticipationRequest participationRequest = new ParticipationRequest(userId, eventId);

    if (eventResponse.getParticipantLimit() != 0 && eventResponse.getParticipantLimit() <= confirmedRequests) {
      throw new ConflictException("Достигнут лимит участников для этого события");
    }

    if (!eventResponse.getRequestModeration() || eventResponse.getParticipantLimit() == 0) {
      participationRequest.setStatus(StatusRequest.CONFIRMED);
    }

    ParticipationRequest savedRequest = requestRepository.save(participationRequest);

    return RequestMapper.mapToDto(savedRequest);
  }


  @Override
  @Transactional(readOnly = true)
  public List<ParticipationRequestDto> getAll(final List<Long> eventIds) {
//    userRepository.findById(userId)
//        .orElseThrow(() -> new NotFoundException("Нету такого user"));

    //userClient.getUser(userId.getFirst());

    return requestRepository.findAllByRequesterIn(eventIds)
        .stream()
        .map(RequestMapper::mapToDto)
        .toList();
  }

  @Override
  @Transactional
  public ParticipationRequestDto cancel(final Long userId, final Long requestId) {
//    userRepository.findById(userId)
//        .orElseThrow(() -> new NotFoundException("Нету такого user"));

    userClient.getUser(userId);

    ParticipationRequest participationRequest = requestRepository.findById(requestId)
        .orElseThrow(() -> new NotFoundException("Нету такого запроса"));

    if (!userId.equals(participationRequest.getRequester())) {
      throw new NotFoundException("Отменить может только владелец заявки");
    }

    participationRequest.setStatus(StatusRequest.CANCELED);

    requestRepository.save(participationRequest);

    return RequestMapper.mapToDto(participationRequest);
  }


//  public List<ParticipationRequestDto> getAllRequestsByEventIds(List<Long> eventIds) {
//    if (eventIds == null || eventIds.isEmpty()) {
//      return List.of();
//    }
//
//    List<ParticipationRequest> requests = requestRepository.findAllByEventIdIn(eventIds);
//
//    return requests.stream()
//            .map(requestMapper::toParticipationRequestDto)
//            .collect(Collectors.toList());
//  }



  @Transactional(readOnly = true)
  @Override
  public List<ParticipationRequestDto> getRequests(final Long initiatorId, final Long eventId) {
    log.debug("Retrieving event participants for event ID={}, posted by user ID={}.", eventId, initiatorId);


    EventFullDto event = eventClient.getEventById(eventId);

    if (event == null) {
      throw new ConflictException("Событие не найдено");
    }

    if (event.getInitiator() == null) {
      throw new ConflictException("Инициатор события не найден");
    }

    Long eventInitiatorId = event.getInitiator().getId();

    if (!eventInitiatorId.equals(initiatorId)) {
      throw new ConflictException("Пользователь не является инициатором события");
    }

    // Возвращаем все заявки на это событие
    List<ParticipationRequest> requests = requestRepository.findAllByEvent(eventId);
    return RequestMapper.mapToDto(requests);
  }

  private EventRequestStatusUpdateResult autoConfirmRequests(final List<Long> requestIds,
                                                             final Long eventId) {
    log.debug("Confirming All requests.");
    final List<ParticipationRequest> requestsToUpdate = getPendingRequests(requestIds, eventId);
    requestsToUpdate.forEach(request -> request.setStatus(StatusRequest.CONFIRMED));
    requestRepository.saveAll(requestsToUpdate);

    return RequestMapper.toEventRequestStatusUpdateResult(requestsToUpdate, Collections.emptyList());
  } // второй

  private List<ParticipationRequest> getPendingRequests(final List<Long> requestIds,
                                                        final Long eventId) {
    log.debug("Fetching participation requests with IDs:{} and PENDING status.", requestIds);
    final List<ParticipationRequest> requests = requestRepository.findAllByIdInAndEventAndStatus(
            requestIds, eventId, StatusRequest.PENDING);
    if (requestIds.size() > requests.size()) {
      log.warn("StatusRequest should be PENDING for all requests to be updated.");
      throw new ConflictException(
              "StatusRequest should be PENDING for all requests to be updated.");
    }
    return requests;
  }


  private EventRequestStatusUpdateResult processRequestsWithLimit(
          final List<ParticipationRequest> requestsToUpdate,
          final StatusRequest newStatus, final Integer availableSlots) {
    log.debug("Processing requests {} to update status with {}. Available slots ={}",
            requestsToUpdate, newStatus, availableSlots);
    final List<ParticipationRequest> confirmedRequests = new ArrayList<>();
    final List<ParticipationRequest> rejectedRequests = new ArrayList<>();
    int available = availableSlots;

    if (newStatus.equals(StatusRequest.REJECTED)) {
      requestsToUpdate.forEach(r -> r.setStatus(newStatus));
      rejectedRequests.addAll(requestRepository.saveAll(requestsToUpdate));
    } else {
      for (ParticipationRequest request : requestsToUpdate) {
        if (available > 0) {
          request.setStatus(StatusRequest.CONFIRMED);
          confirmedRequests.add(request);
          available--;
        } else {
          request.setStatus(StatusRequest.REJECTED);
          rejectedRequests.add(request);
        }
      }
    }
    requestRepository.saveAll(confirmedRequests);
    requestRepository.saveAll(rejectedRequests);
    return RequestMapper.toEventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
  } // третий

  @Override
  public List<ParticipationRequestDto> getConfirmedRequestsByEventIds(List<Long> eventIds) {
    log.debug("Fetching confirmed participation requests for event IDs: {}", eventIds);

    if (eventIds == null || eventIds.isEmpty()) {
      log.debug("Event ID list is empty or null.");
      return List.of();
    }

    List<ParticipationRequest> confirmedRequests = requestRepository
            .findAllByEventInAndStatus(eventIds, StatusRequest.CONFIRMED);

    return confirmedRequests.stream()
            .map(RequestMapper::mapToDto)
            .collect(Collectors.toList());
  }

  @Override
  public void setConfirmedRequests(final List<EventFullDto> events) {
    log.debug("Setting Confirmed requests to the events list {}.", events);
    if (events.isEmpty()) {
      log.debug("Events list is empty.");
      return;
    }
    final List<Long> eventIds = events.stream().map(EventFullDto::getId).toList();
    final Map<Long, List<ParticipationRequest>> confirmedRequests =
            requestRepository.findAllByEventInAndStatus(eventIds, StatusRequest.CONFIRMED)
                    .stream()
                    .collect(Collectors.groupingBy(
                            participantRequest -> participantRequest.getEvent()));

    events.forEach(event ->
            event.setConfirmedRequests(
                    confirmedRequests.getOrDefault(event.getId(), List.of()).size()));
    log.debug("Confirmed requests has set successfully to the events with IDs {}.", eventIds);
  }

  @Override
  public EventRequestStatusUpdateResult updateRequestsStatus(
          final Long initiatorId,
          final Long eventId,
          final EventRequestStatusUpdateRequest updateStatusDto) {
    log.debug(
            "Updating participation requests {} for the event {} created by user {} with statusRequest {}",
            updateStatusDto.getRequestIds(), eventId, initiatorId, updateStatusDto.getStatus());

    //validateUserExist(initiatorId);
    final EventFullDto event = eventClient.getEventById(eventId);
   //final EventFullDto event = fetchEvent(eventId, initiatorId);
    final StatusRequest newStatus = updateStatusDto.getStatus();
    final Boolean isModerated = event.getRequestModeration(); //
    final Integer participantLimit = event.getParticipantLimit();
    final Integer confirmed = event.getConfirmedRequests();

    if (!isModerated || participantLimit == 0) {
      return autoConfirmRequests(updateStatusDto.getRequestIds(), eventId);
    }

    final List<ParticipationRequest> requestsToUpdate = getPendingRequests(
            updateStatusDto.getRequestIds(), eventId);

    int availableSlots = participantLimit - confirmed;
    if (availableSlots <= 0) {
      log.warn(
              "Participant limit for the event {} has been reached: limit={}, confirmed requests={}.",
              eventId, event.getParticipantLimit(), event.getConfirmedRequests());
      throw new ConflictException("Participant limit for this event has been reached.");
    }
    return processRequestsWithLimit(requestsToUpdate, newStatus, availableSlots);
  }


}
