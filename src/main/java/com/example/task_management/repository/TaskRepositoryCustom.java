package com.example.task_management.repository;

import com.example.task_management.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Custom repository interface for Task entity to support keyword search.
 */
public interface TaskRepositoryCustom {
    /**
     * Finds tasks belonging to {@code ownerId} whose title or description contains the given {@code keyword}.
     * The search is case‑insensitive and matches anywhere in the text.
     *
     * @param ownerId the user (owner) identifier
     * @param keyword the search term; may be {@code null} or empty – in that case no keyword filtering is applied
     * @param pageable pagination and sorting information
     * @return a page of matching {@link Task} entities
     */
    Page<Task> findByOwnerIdAndKeyword(Long ownerId, String keyword, Pageable pageable);
}
