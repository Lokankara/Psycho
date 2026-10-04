package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EpicRepository extends JpaRepository<Epic, Long> {

    List<Epic> findByProgramIncrementIdOrderByCodeAsc(Long programIncrementId);

    Optional<Epic> findByCode(String code);
}
