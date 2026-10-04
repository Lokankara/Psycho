package com.unconscious.collective.bdd;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "agile_task")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(length = 2048)
    private String description;

    @Column(length = 256)
    private String bddStory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BddStatus executionStatus = BddStatus.NOT_RUN;

    private Instant lastRunAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_story_id")
    private UserStory userStory;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "kanban_column_id", nullable = false)
    private KanbanColumn kanbanColumn;

    public Task() {
    }
}
