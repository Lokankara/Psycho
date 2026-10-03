package com.unconscious.collective.bdd;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findAllByOrderByCodeAsc();

    List<UserStory> findBySprintIdOrderByCodeAsc(Long sprintId);

    Optional<UserStory> findByBddStory(String bddStory);
}
