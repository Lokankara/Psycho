package com.unconscious.collective.agile.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * An epic owned by a program increment and broken down into user stories.
 */
@Entity
@Table(name = "agile_epic")
public class Epic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_increment_id", nullable = false)
    private ProgramIncrement programIncrement;

    @Column(nullable = false, unique = true, length = 32)
    private String code;

    @Column(nullable = false, length = 256)
    private String name;

    @Column(length = 512)
    private String goal;

    @Column(length = 2000)
    private String description;

    public Epic() {
    }

    public Epic(String code, String title, String description, ProgramIncrement programIncrement) {
        this.code = code;
        this.name = title;
        this.description = description;
        this.programIncrement = programIncrement;
    }

    public Long getId() {
        return id;
    }

    public ProgramIncrement getProgramIncrement() {
        return programIncrement;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return name;
    }

    public String getGoal() {
        return goal;
    }

    public String getDescription() {
        return description;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setProgramIncrement(ProgramIncrement programIncrement) {
        this.programIncrement = programIncrement;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGoal(String goal) {
        this.goal = goal;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}