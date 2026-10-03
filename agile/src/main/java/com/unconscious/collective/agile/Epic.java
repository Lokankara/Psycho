package com.unconscious.collective.bdd;

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
    private String key;

    @Column(nullable = false, length = 256)
    private String title;

    @Column(length = 2000)
    private String description;

    public Epic() {
    }

    public Epic(ProgramIncrement programIncrement, String key, String title, String description) {
        this.programIncrement = programIncrement;
        this.key = key;
        this.title = title;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public ProgramIncrement getProgramIncrement() {
        return programIncrement;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}