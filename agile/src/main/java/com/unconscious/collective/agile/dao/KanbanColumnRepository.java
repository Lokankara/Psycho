package com.unconscious.collective.agile.dao;

import java.util.List;
import java.util.Optional;

import com.unconscious.collective.agile.model.entity.KanbanColumn;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KanbanColumnRepository extends JpaRepository<@NonNull KanbanColumn, @NonNull Long> {

    List<KanbanColumn> findAllByOrderByPositionAsc();

    Optional<KanbanColumn> findByCode(String code);

    default List<KanbanColumn> findAllByOrderByPositionAscOrDefault() {
        List<KanbanColumn> columns = findAllByOrderByPositionAsc();
        if (columns.isEmpty()) {
            return List.of(
                    new KanbanColumn("TO_DO", "To Do", 0),
                    new KanbanColumn("IN_PROGRESS", "In Progress", 1),
                    new KanbanColumn("CODE_REVIEW", "Code Review", 2),
                    new KanbanColumn("DONE", "Done", 3)
            );
        }
        return columns;
    }
}
