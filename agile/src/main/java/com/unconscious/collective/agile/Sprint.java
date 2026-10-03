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

import java.time.LocalDate;

/**
 * A time-boxed sprint inside a program increment.
 */
@Entity
@Table(name = "agile_sprint")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_increment_id", nullable = false)
    private ProgramIncrement programIncrement;

    @Column(nullable = false, length = 128)
    private String name;

    @Column(length = 512)
    private String goal;

    private LocalDate startDate;

    private LocalDate endDate;

    public Sprint() {
    }

    public Sprint(ProgramIncrement programIncrement, String name, String goal,
                  LocalDate startDate, LocalDate endDate) {
        this.programIncrement = programIncrement;
        this.name = name;
        this.goal = goal;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public ProgramIncrement getProgramIncrement() {
        return programIncrement;
    }

    public String getName() {
        return name;
    }

    public String getGoal() {
        return goal;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }
}