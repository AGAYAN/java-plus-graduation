package ru.practicum.event.service;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.mapper.CategoryMapper;
import ru.practicum.category.service.CategoryService;
import ru.practicum.controller.RequestController;
import ru.practicum.controller.UserController;
import ru.practicum.dto.category.CategoryDto;
import ru.practicum.dto.event.*;
import ru.practicum.dto.event.enums.SortType;
import ru.practicum.dto.request.ParticipationRequestDto;
import ru.practicum.dto.request.StatusRequest;
import ru.practicum.dto.user.UserDto;
import ru.practicum.dto.user.UserShortDto;
import ru.practicum.event.enums.State;
import ru.practicum.event.enums.StateAction;
import ru.practicum.event.mapper.EventMapper;
import ru.practicum.event.model.Event;
import ru.practicum.event.repository.EventRepository;
import ru.practicum.exception.BadRequestException;
import ru.practicum.exception.ConflictException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

  private final EventRepository eventRepository;
  private final CategoryService categoryService;
  private final RequestController requestController;
  private final StatsClient statsClient;
  private final UserController userController;

  /**
   * Saves a new event data initiated by a current user.
   */
  @Override
  public EventFullDto addEvent(final Long initiatorId, final NewEventDto eventDto) {
    log.debug("Persisting a new event with data: {} posted by user with ID={}.", eventDto,
            initiatorId);

    final UserDto initiator = userController.getUser(initiatorId);
    final CategoryDto category = categoryService.getCategoryById(eventDto.getCategory());
    final Event eventToSave = EventMapper.toEvent(eventDto, initiator, category);

    return EventMapper.toFullDto(eventRepository.save(eventToSave));
  }

  /**
   * Updates specified event with the provided data (Performed by ADMIN).
   */
  @Override
  public EventFullDto updateEvent(final long eventId, final UpdateEventAdminRequest param) {
    Event event = eventRepository.findById(eventId)
            .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не найдено"));

    if (event.getState().equals(State.PUBLISHED)) {
      throw new ConflictException("Событие " + event.getId() + " уже опубликовано");
    }
    if (event.getState().equals(State.CANCELED)) {
      throw new ConflictException("Нельзя опубликовать отмененное событие");
    }

    EventMapper.updateEventFromAdminRequest(event, param, event.getCategory());
    eventRepository.save(event);

    return EventMapper.toFullDto(event);
  }

  /**
   * Updates the specified event by the current user (who is the initiator of the event).
   */
  @Override
  public EventFullDto updateEvent(final Long userId, final Long eventId,
                                  final UpdateEventUserRequest eventDto) {
    log.debug("Updating event ID={}, posted by user with ID={} with data {}.",
        eventId, userId, eventDto);
    final Event eventToUpdate = getEnrichedEvent(userId, eventId);
    validateEventUpdatable(eventToUpdate, null);
    patchEventFields(eventToUpdate, eventDto);
    eventRepository.save(eventToUpdate);
    return EventMapper.toFullDto(eventToUpdate);
  }

  /**
   * Retrieves complete data about specific event created by current user.
   */
  @Transactional(readOnly = true)
  @Override
  public EventFullDto getEvent(final Long initiatorId, final Long eventId) {
    log.debug("Fetching event ID={}, posted by user with ID={}.", eventId, initiatorId);
    final Event event = getEnrichedEvent(initiatorId, eventId);
    return EventMapper.toFullDto(event);
  }

  /**
   * Retrieves detailed information about a published event by its ID.
   */
  @Transactional(readOnly = true)
  @Override
  public EventFullDto getEvent(final Long eventId) {
    log.debug("Fetching event ID={}.", eventId);
    Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
            .orElseThrow(() -> new NotFoundException(
                    "Event with id " + eventId + " not found or not published"));


    event.setInitiator(userController.getUser(event.getInitiatorId()));

    event.setConfirmedRequests(requestController.getAllRequests(List.of(eventId)).size());

    setViews(List.of(event));

    return EventMapper.toFullDto(event);
  }

  /**
   * Retrieves all existed in DB events that match the given conditions in the GetEventAdminRequest(performed by ADMIN).
   */

  @Transactional(readOnly = true)
  @Override
  public List<EventFullDto> getEvents(GetEventAdminRequest param) {
    log.info("Received request GET /admin/events with param {}", param);

    List<Event> events = eventRepository.adminFindEvents(
            param.getUsers(),
            param.getStates(),
            param.getCategories(),
            param.getRangeStart(),
            param.getRangeEnd(),
            param.getFrom(),
            param.getSize()
    );

    List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
    Map<Long, UserDto> initiators = getInitiators(events);
    Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);
    setViews(events);

    return events.stream().map(event -> {
      EventFullDto dto = EventMapper.toFullDto(event);
      dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L).intValue());
      dto.setInitiator(initiators.get(event.getInitiatorId()));
      return dto;
    }).collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  @Override
  public List<EventShortDto> getEvents(GetEventPublicParam param, HttpServletRequest request) {
    log.debug("Fetching events with params {}", param);
    if (param.getRangeStart() != null && param.getRangeEnd() != null &&
            param.getRangeStart().isAfter(param.getRangeEnd())) {
      throw new BadRequestException("Start date should be before end date");
    }

    List<Event> events = eventRepository.publicGetEvents(
            param.getText(),
            param.getCategories(),
            param.getPaid(),
            param.getRangeStart(),
            param.getRangeEnd(),
            param.getOnlyAvailable(),
            param.getSort() != null ? param.getSort().name() : null,
            param.getFrom(),
            param.getSize());

    List<Long> eventIds = events.stream().map(Event::getId).collect(Collectors.toList());
    Map<Long, UserDto> initiators = getInitiators(events);
    Map<Long, Long> confirmedRequests = getConfirmedRequests(eventIds);
    setViews(events);

    List<EventShortDto> result = events.stream().map(event -> {
      EventShortDto dto = EventMapper.toShortDto(event);
      dto.setConfirmedRequests(confirmedRequests.getOrDefault(event.getId(), 0L).intValue());
      dto.setInitiator(new UserShortDto(
              initiators.get(event.getInitiatorId()).getId(),
              initiators.get(event.getInitiatorId()).getName()
      ).getId());
      return dto;
    }).collect(Collectors.toList());

    if (param.getSort() != null && param.getSort().equals(SortType.VIEWS)) {
      result.sort(Comparator.comparing(EventShortDto::getViews).reversed());
    }

    return result;
  }

  private Map<Long, UserDto> getInitiators(List<Event> events) {
    List<Long> initiatorIds = events.stream()
            .map(Event::getInitiatorId)
            .distinct()
            .toList();

    return initiatorIds.stream()
            .collect(Collectors.toMap(
                    id -> id,
                    userController::getUser
            ));
  }

  public Map<Long, Long> getConfirmedRequests(List<Long> eventIds) {
    if (eventIds.isEmpty()) {
      return Collections.emptyMap();
    }

    return requestController.getAllRequests(eventIds).stream()
            .filter(request -> StatusRequest.CONFIRMED.name().equals(request.getStatus()))
            .collect(Collectors.groupingBy(
                    ParticipationRequestDto::getEvent,
                    Collectors.counting()
            ));
  }

  /**
   * Retries all events created by current user.
   */
  @Transactional(readOnly = true)
  @Override
  public List<EventShortDto> getEvents(final Long initiatorId, final Integer from,
                                       final Integer size) {
    final PageRequest page = PageRequest.of(from / size, size);
    final List<Event> events = eventRepository.findAllByInitiatorId(initiatorId, page).getContent();

    // Получаем список id событий
    List<Long> eventIds = events.stream()
            .map(Event::getId)
            .toList();

    // Получаем заявки на участие
    requestController.getAllRequests(eventIds);

    setViews(events);
    return EventMapper.toShortDto(events);
  }

  /**
   *  Retrieves a set of events based on the provided event IDs.
   */
  @Transactional(readOnly = true)
  @Override
  public Set<Event> getEvents(final Set<Long> eventIds) {
    log.debug("Retrieving set of events by theirs IDs: {}.", eventIds);
    Objects.requireNonNull(eventIds);
    return eventIds.isEmpty() ? Set.of() : eventRepository.findAllDistinctByIdIn(eventIds);
  }

  @Override
  public List<EventFullDto> findEventByIds(Set<Long> ids) {
    return eventRepository.findAllByIdIn(ids).stream()
            .map(EventMapper::toFullDto)
            .toList();
  }

  /**
   * Gets specified Event created by User with given ID, with confirmedRequests and views data set.
   */
  private Event getEnrichedEvent(final Long initiatorId, final Long eventId) {
    final Event event = fetchEvent(eventId, initiatorId);
    setViews(List.of(event));
    return event;
  }

  private Event fetchEvent(final Long eventId, final Long initiatorId) {
    log.debug("Fetching event with ID {} and initiator ID {}.", eventId, initiatorId);
    return eventRepository.findByIdAndInitiatorId(eventId, initiatorId)
            .map(event -> event.setConfirmedRequests(event.getConfirmedRequests()))
            .orElseThrow(() -> {
              log.warn("Event with ID={} and initiator ID={} not found.", eventId, initiatorId);
              return new NotFoundException("Event not found.");
            });
  }

  private void setViews(final List<Event> events) {
    log.debug("Setting views to the events list.");
    if (events == null || events.isEmpty()) {
      log.debug("Events list is empty.");
      return;
    }
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    final String start = events.stream()
        .min(Comparator.comparing(Event::getCreatedOn))
        .map(event -> event.getCreatedOn().format(formatter))
        .orElse(LocalDateTime.now().format(formatter));

    final String end = LocalDateTime.now().format(formatter);

    final String[] uris = events.stream()
        .map(e -> buildEventUri(e.getId()))
        .toArray(String[]::new);

    log.debug("Calling StatsClient with parameters: start={}, end={}, uris={}, unique={}.",
        start, end, Arrays.toString(uris), true);
    final Map<String, Long> views = Arrays.stream(statsClient.getStats(start, end, uris, true))
        .collect(Collectors.toMap(ViewStatsDto::getUri, ViewStatsDto::getHits));

    events.forEach(event ->
        event.setViews(views.getOrDefault(buildEventUri(event.getId()), 0L)));
    log.debug("Views has set successfully.");

  }

  private String buildEventUri(final Long eventId) {
    return String.format("/events/%d", eventId);
  }


  private void patchEventFields(final Event target, final UpdateEventUserRequest dataSource) {
    log.debug("Apply the patch on Event fields.");
    Optional.ofNullable(dataSource.getAnnotation()).ifPresent(target::setAnnotation);
    Optional.ofNullable(dataSource.getDescription()).ifPresent(target::setDescription);
    Optional.ofNullable(dataSource.getEventDate()).ifPresent(target::setEventDate);
    Optional.ofNullable(dataSource.getLocation()).ifPresent(target::setLocation);
    Optional.ofNullable(dataSource.getPaid()).ifPresent(target::setPaid);
    Optional.ofNullable(dataSource.getParticipantLimit()).ifPresent(target::setParticipantLimit);
    Optional.ofNullable(dataSource.getRequestModeration()).ifPresent(target::setRequestModeration);
    Optional.ofNullable(dataSource.getTitle()).ifPresent(target::setTitle);

    Optional.ofNullable(dataSource.getCategory()).ifPresent(categoryId ->
        target.setCategory(CategoryMapper.toCategory(categoryService.getCategoryById(categoryId))));

    Optional.ofNullable(dataSource.getStateAction())
        .ifPresent(stateAction ->
            target.setState(StateAction.fromString(stateAction).getState()));
  }

  private void validateEventUpdatable(final Event event, final @Nullable UpdateEventAdminRequest param) {
    log.debug("Validate event date is in the future and has right state.");
    if (param != null) {
      validateEventDate(event.getEventDate(), 1);
      validateEventState(event,param);
    } else {
      validateEventDate(event.getEventDate(), 2);
      validateEventState(event);
    }
  }

  private void validateEventState(final Event event) {
    if (State.PUBLISHED.equals(event.getState())) {
      throw new ConflictException("Only pending or canceled events can be changed");
    }
  }

  private void validateEventState(final Event event, final UpdateEventAdminRequest param) {
    if (param.getStateAction() != null) {
      if (param.getStateAction().equals(StateAction.PUBLISH_EVENT.name()) && !event.getState()
          .equals(State.PENDING)) {
        throw new ConflictException("Cannot publish the event because it's not in the right state: "
            + event.getState());
      }
      if (param.getStateAction().equals(StateAction.REJECT_EVENT.name()) && event.getState()
          .equals(State.PUBLISHED)) {
        throw new ConflictException("Cannot reject the event because it is already published.");
      }
    }
  }

  private void validateEventDate(final LocalDateTime eventDate, final int minimumTimeGap) {
    log.debug("Validate event date at least is two hours ahead..");
    if (!eventDate.isAfter(LocalDateTime.now().plusHours(minimumTimeGap))) {
      throw new ConflictException("The event date must be at least two hours in the future.");
    }
  }


}
