package com.unconscious.collective.quiz.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.time.Instant;

/**
 * A persisted snapshot of a completed quiz run.
 */
@Entity
@Table(name = "quiz_result")
public class QuizResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private double x;

    @Column(nullable = false)
    private double y;

    @Column(nullable = false)
    private double z;

    @Column(nullable = false, length = 64)
    private String octantCode;

    public QuizResult() {
    }

    public QuizResult(Instant createdAt, double x, double y, double z, String octantCode) {
        this.createdAt = createdAt;
        this.x = x;
        this.y = y;
        this.z = z;
        this.octantCode = octantCode;
    }

    public Long getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public String getOctantCode() {
        return octantCode;
    }
}
