package com.unconscious.collective.agile.dao;

import java.util.List;

import com.unconscious.collective.agile.model.entity.Task;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<@NonNull Task, @NonNull Long> {

    List<Task> findAllByOrderByCodeAsc();

    List<Task> findByBddStory(String bddStory);

    List<Task> findByUserStorySprintIdOrderByCodeAsc(Long sprintId);
}
