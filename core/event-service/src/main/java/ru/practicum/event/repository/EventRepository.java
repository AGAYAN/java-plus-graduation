package ru.practicum.event.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.event.enums.State;
import ru.practicum.event.model.Event;

import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, EventQueryRepository {

  Page<Event> findAllByInitiatorId(Long initiatorId, PageRequest page);

  @Query("SELECT e FROM Event e WHERE e.id = :id AND e.initiatorId = :initiatorId")
  Optional<Event> findByIdAndInitiatorId(@Param("id") Long id, @Param("initiatorId") Long initiatorId);

  Optional<Event> findByIdAndState(Long id, State state);

  boolean existsByCategoryId(Long id);

  Set<Event> findAllDistinctByIdIn(Set<Long> eventIds);

  Set<Event> findAllByIdIn(@Param("ids") Set<Long> ids);

}

