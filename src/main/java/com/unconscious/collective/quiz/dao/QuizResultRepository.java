package com.unconscious.collective.quiz.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizResultRepository extends JpaRepository<QuizResult, Long> {

    List<QuizResult> findAllByOrderByCreatedAtDesc();
}
