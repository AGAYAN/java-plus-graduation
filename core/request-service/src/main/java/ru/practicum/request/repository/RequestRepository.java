package ru.practicum.request.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.dto.request.StatusRequest;
import ru.practicum.request.model.ParticipationRequest;

import java.util.List;

public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

  List<ParticipationRequest> findAllByRequesterIn(List<Long> userIds);

  List<ParticipationRequest> findAllByEventAndRequester(Long userId, Long initiatorId);

  int countAllByEventAndStatus(Long eventId, StatusRequest status);

  ParticipationRequest findByRequesterAndEvent(Long userId, Long eventId);

  List<ParticipationRequest> findAllByEventInAndStatus(List<Long> eventIds,
                                                         StatusRequest statusRequest);

  List<ParticipationRequest> findAllByIdInAndEventAndStatus(List<Long> requestIds,
                                                                 Long eventId,
                                                                 StatusRequest statusRequest);

  List<ParticipationRequest> findAllByEvent(Long eventId);

  //List<ParticipationRequest> findAllByEvent(List<Long> eventIds);

}
