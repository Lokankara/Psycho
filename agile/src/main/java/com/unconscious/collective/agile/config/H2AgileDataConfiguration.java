package com.unconscious.collective.agile.config;

import com.unconscious.collective.agile.dao.KanbanColumnRepository;
import com.unconscious.collective.agile.dao.ProgramIncrementRepository;
import com.unconscious.collective.agile.dao.SprintRepository;
import com.unconscious.collective.agile.dao.TaskRepository;
import com.unconscious.collective.agile.dao.UserStoryRepository;
import com.unconscious.collective.agile.service.AgileBoardService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "spring.datasource.mode", havingValue = "h2", matchIfMissing = true)
@EnableJpaRepositories(basePackages = "com.unconscious.collective.agile.dao")
public class H2AgileDataConfiguration {

    @Bean
    public AgileBoardService agileBoardService(
            ProgramIncrementRepository incrementRepository,
            SprintRepository sprintRepository,
            UserStoryRepository storyRepository,
            TaskRepository taskRepository,
            KanbanColumnRepository columnRepository) {
        return new AgileBoardService(incrementRepository, sprintRepository,
                storyRepository, taskRepository, columnRepository);
    }
}
