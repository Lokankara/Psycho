package com.unconscious.collective.agile.dao;

import java.util.List;
import java.util.Optional;

import com.unconscious.collective.agile.model.entity.KanbanColumn;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KanbanColumnRepository extends JpaRepository<@NonNull KanbanColumn, @NonNull Long> {

    List<KanbanColumn> findAllByOrderByPositionAsc();

    Optional<KanbanColumn> findByCode(String code);
}
