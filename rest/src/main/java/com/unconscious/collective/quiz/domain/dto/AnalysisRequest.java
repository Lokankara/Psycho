package com.unconscious.collective.quiz.domain.dto;

import java.util.Map;

import com.unconscious.collective.quiz.controller.AnalysisController;

/**
 * Incoming payload for {@link AnalysisController}: a map of question id to the
 * chosen {@code POSITIVE}/{@code NEGATIVE} pole.
 */
public record AnalysisRequest(Map<String, String> answers) {
}
