package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProgramIncrementIdOrderByStartDateAsc(Long programIncrementId);
}
