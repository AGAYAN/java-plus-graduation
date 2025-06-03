package ru.practicum.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.model.UserAction;

import java.util.List;

public interface UserActionRepository extends JpaRepository<UserAction, Long> {
    @Query("SELECT COUNT(DISTINCT a.userId) FROM UserAction a " +
            "WHERE a.action = :action AND a.eventId = :eventId " +
            "AND a.userId NOT IN (" +
            "   SELECT a2.userId FROM UserAction a2 " +
            "   WHERE a2.eventId = :eventId AND a2.action IN :excludedActions" +
            ")")
    long countUserIdsWithSpecificActionOnly(
            @Param("eventId") Long eventId,
            @Param("action") String action,
            @Param("excludedActions") List<String> excludedActions
    );

    @Query("SELECT DISTINCT a.eventId FROM UserAction a WHERE a.userId = :uid")
    List<Long> findEventIdsByUserId(@Param("uid") Long uid);

    @Query("SELECT a.eventId " +
            "FROM UserAction a WHERE a.userId = :userId " +
            "ORDER BY a.time")
    Page<Long> findActionsByUserIdOrderByTimestamp(
            @Param("userId") Long userId,
            Pageable pageable
    );

    @Query("SELECT DISTINCT a1.eventId FROM UserAction a1 " +
            "WHERE a1.eventId NOT IN (" +
            "   SELECT a2.eventId FROM UserAction a2 WHERE a2.userId = :uid" +
            ")")
    List<Long> findNotInteractedEventIdsByUserId(@Param("uid") Long uid);
}
