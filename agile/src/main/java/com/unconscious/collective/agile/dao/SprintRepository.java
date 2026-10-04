package com.unconscious.collective.agile.dao;

import java.util.List;

import com.unconscious.collective.agile.model.entity.Sprint;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SprintRepository extends JpaRepository<@NonNull Sprint, @NonNull Long> {

    List<Sprint> findByProgramIncrementIdOrderByStartDateAsc(Long programIncrementId);
}
