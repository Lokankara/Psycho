package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findAllByOrderByCodeAsc();

    List<Task> findByUserStoryIdOrderByCodeAsc(Long userStoryId);

    Optional<Task> findByBddStory(String bddStory);
}
