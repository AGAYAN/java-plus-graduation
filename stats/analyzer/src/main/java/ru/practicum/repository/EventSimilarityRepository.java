package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.EventSimilarity;
import ru.practicum.model.RecommendedEvent;

import java.util.List;

public interface EventSimilarityRepository extends JpaRepository<EventSimilarity, Long> {

    @Query("SELECT e FROM EventSimilarity e " +
            "WHERE (e.eventIdA = :eventId OR e.eventIdB = :eventId) " +
            "AND NOT (e.eventIdA IN :interactedList " +
            "AND e.eventIdA IN :interactedList) " +
            "ORDER BY e.similarityScore DESC")
    Page<EventSimilarity> findSimilaritiesExcludingInteracted(
            @Param("eventId") Long eventId,
            @Param("interactedList") List<Long> interactedList,
            Pageable pageable
    );

    @Query("SELECT CASE " +
            "WHEN e.eventIdA IN :notInteractedList AND e.eventIdB IN :interactedList THEN e.eventIdA " +
            "WHEN e.eventIdB IN :notInteractedList AND e.eventIdA IN :interactedList THEN e.eventIdB " +
            "ELSE -1L " +
            "END " +
            "FROM EventSimilarity e " +
            "WHERE (e.eventIdA IN :notInteractedList AND e.eventIdB IN :interactedList) " +
            "   OR (e.eventIdB IN :notInteractedList AND e.eventIdA IN :interactedList) " +
            "ORDER BY e.similarityScore DESC")
    List<Long> findMostSimilarEventsIds(
            @Param("interactedList") List<Long> interactedList,
            @Param("notInteractedList") List<Long> notInteractedList,
            Pageable pageable
    );

    @Query("SELECT NEW ru.practicum.model.RecommendedEvent(" +
            "CASE WHEN e.eventIdA = :eventId THEN e.eventIdB ELSE e.eventIdA END, " +
            "e.similarityScore AS score) " +
            "FROM EventSimilarity e " +
            "WHERE (e.eventIdA = :eventId OR e.eventIdB = :eventId) " +
            "AND (e.eventIdA IN :interactedList OR e.eventIdB IN :interactedList) " +
            "ORDER BY e.similarityScore DESC")
    List<RecommendedEvent> findSimilarEvents(
            @Param("eventId") Long eventId,
            @Param("interactedList") List<Long> interactedList
    );
}
