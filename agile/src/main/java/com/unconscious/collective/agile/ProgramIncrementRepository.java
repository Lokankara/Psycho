package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramIncrementRepository extends JpaRepository<ProgramIncrement, Long> {

    List<ProgramIncrement> findAllByOrderByStartDateAsc();

    Optional<ProgramIncrement> findByCode(String code);
}
