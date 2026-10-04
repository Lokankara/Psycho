package com.unconscious.collective.agile.model.entity;

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

import java.time.Instant;

@Entity
@Table(name = "user_story")
public class UserStory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(length = 4096)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String acceptanceCriteria;

    private Integer storyPoints;

    @Column(length = 16)
    private String priority;

    @Column(length = 256)
    private String bddStory;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BddStatus executionStatus = BddStatus.NOT_RUN;

    private Instant lastRunAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "epic_id")
    private Epic epic;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id")
    private Sprint sprint;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "kanban_column_id", nullable = false)
    private KanbanColumn kanbanColumn;

    public UserStory() {
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

    public String getAcceptanceCriteria() {
        return acceptanceCriteria;
    }

    public Integer getStoryPoints() {
        return storyPoints;
    }

    public String getPriority() {
        return priority;
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

    public Epic getEpic() {
        return epic;
    }

    public Sprint getSprint() {
        return sprint;
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

    public void setAcceptanceCriteria(String acceptanceCriteria) {
        this.acceptanceCriteria = acceptanceCriteria;
    }

    public void setStoryPoints(Integer storyPoints) {
        this.storyPoints = storyPoints;
    }

    public void setPriority(String priority) {
        this.priority = priority;
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

    public void setEpic(Epic epic) {
        this.epic = epic;
    }

    public void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    public void setKanbanColumn(KanbanColumn kanbanColumn) {
        this.kanbanColumn = kanbanColumn;
    }
}
