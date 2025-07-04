package ru.practicum.compilation.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import ru.practicum.compilation.model.Compilation;
import ru.practicum.dto.compilation.CompilationParam;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CompilationQueryRepositoryImpl implements CompilationQueryRepository {

  private final EntityManager entityManager;

  @Override
  public List<Compilation> findAllBy(final CompilationParam param) {
    Pageable page = PageRequest.of(param.getFrom() / param.getSize(), param.getSize());
    CriteriaBuilder cb = entityManager.getCriteriaBuilder();
    CriteriaQuery<Compilation> query = cb.createQuery(Compilation.class);

    Root<Compilation> compilationTable = query.from(Compilation.class);
    compilationTable.fetch("events", JoinType.LEFT);

    query.select(compilationTable).distinct(true);

    log.debug("Fetching compilations with pinned={}, from={}, size={}",
            param.getPinned(), param.getFrom(), param.getSize());

    if (param.getPinned() != null) {
      query.where(cb.equal(compilationTable.get("pinned"), param.getPinned()));
    }

    TypedQuery<Compilation> typedQuery = entityManager.createQuery(query);
    typedQuery.setFirstResult(page.getPageNumber() * page.getPageSize());
    typedQuery.setMaxResults(page.getPageSize());

    return typedQuery.getResultList();
  }

}
