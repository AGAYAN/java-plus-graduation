package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dto.request.StatusRequest;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

  List<ParticipationRequest> findAllByRequesterId(List<Long> userId);

  List<ParticipationRequest> findAllByEventIdAndEventInitiatorId(Long userId, Long initiatorId);

  int countAllByEventIdAndStatus(Long eventId, StatusRequest status);

  ParticipationRequest findByRequesterIdAndEventId(Long userId, Long eventId);

  List<ParticipationRequest> findAllByEventIdInAndStatus(List<Long> eventIds,
                                                         StatusRequest statusRequest);

  List<ParticipationRequest> findAllByIdInAndEventIdAndStatus(List<Long> requestIds,
                                                                 Long eventId,
                                                                 StatusRequest statusRequest);

}
