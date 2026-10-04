package com.unconscious.collective.agile.model.entity;

import java.time.Instant;

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

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getBddStory() {
        return bddStory;
    }

    public BddStatus getExecutionStatus() {
        return executionStatus;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public UserStory getUserStory() {
        return userStory;
    }

    public KanbanColumn getKanbanColumn() {
        return kanbanColumn;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setBddStory(String bddStory) {
        this.bddStory = bddStory;
    }

    public void setExecutionStatus(BddStatus executionStatus) {
        this.executionStatus = executionStatus;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public void setUserStory(UserStory userStory) {
        this.userStory = userStory;
    }

    public void setKanbanColumn(KanbanColumn kanbanColumn) {
        this.kanbanColumn = kanbanColumn;
    }
}
