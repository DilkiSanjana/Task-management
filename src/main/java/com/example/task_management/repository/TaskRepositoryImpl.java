package com.example.task_management.repository;

import com.example.task_management.entity.Task;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Implementation of the custom repository method that searches tasks by a keyword.
 * The query matches the keyword (case‑insensitive) against the task title or description.
 */
@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Task> findByOwnerIdAndKeyword(Long ownerId, String keyword, Pageable pageable) {
        // Base JPQL
        StringBuilder sb = new StringBuilder("SELECT t FROM Task t WHERE t.owner.id = :ownerId");
        if (StringUtils.hasText(keyword)) {
            sb.append(" AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :kw, '%'))")
              .append(" OR LOWER(t.description) LIKE LOWER(CONCAT('%', :kw, '%')))");
        }
        String jpql = sb.toString();
        TypedQuery<Task> query = em.createQuery(jpql, Task.class)
                .setParameter("ownerId", ownerId);
        if (StringUtils.hasText(keyword)) {
            query.setParameter("kw", keyword);
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Task> content = query.getResultList();

        // Count query for total elements
        StringBuilder countSb = new StringBuilder("SELECT COUNT(t) FROM Task t WHERE t.owner.id = :ownerId");
        if (StringUtils.hasText(keyword)) {
            countSb.append(" AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :kw, '%'))")
                  .append(" OR LOWER(t.description) LIKE LOWER(CONCAT('%', :kw, '%')))");
        }
        TypedQuery<Long> countQuery = em.createQuery(countSb.toString(), Long.class)
                .setParameter("ownerId", ownerId);
        if (StringUtils.hasText(keyword)) {
            countQuery.setParameter("kw", keyword);
        }
        long total = countQuery.getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
