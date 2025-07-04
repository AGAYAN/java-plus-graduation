package ru.practicum.compilation.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.compilation.model.Compilation;

import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long>,
    CompilationQueryRepository {

  @EntityGraph(attributePaths = {"events", "events.category"})
  @Query("""
    SELECT c
    FROM Compilation c
    WHERE c.id = :id
    """)
  Optional<Compilation> findByIdEnriched(@Param("id") Long compId);
}
