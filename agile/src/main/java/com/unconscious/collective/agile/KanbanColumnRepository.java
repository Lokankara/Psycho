package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    List<KanbanColumn> findAllByOrderByPositionAsc();

    Optional<KanbanColumn> findByCode(String code);
}
