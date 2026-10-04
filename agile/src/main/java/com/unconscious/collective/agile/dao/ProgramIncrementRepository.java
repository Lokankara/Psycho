package com.unconscious.collective.agile.dao;

import java.util.List;

import com.unconscious.collective.agile.model.entity.ProgramIncrement;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramIncrementRepository extends JpaRepository<@NonNull ProgramIncrement, @NonNull Long> {

    List<ProgramIncrement> findAllByOrderByStartDateAsc();
}
