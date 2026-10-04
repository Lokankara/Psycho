package com.unconscious.collective.agile.dao;

import java.util.List;

import com.unconscious.collective.agile.model.entity.UserStory;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStoryRepository extends JpaRepository<@NonNull UserStory, @NonNull Long> {

    List<UserStory> findAllByOrderByCodeAsc();

    List<UserStory> findBySprintIdOrderByCodeAsc(Long sprintId);

    List<UserStory> findByBddStory(String bddStory);
}
