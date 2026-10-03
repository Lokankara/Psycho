package com.unconscious.collective.quiz.controller;

import java.util.Map;

/**
 * Incoming payload for {@link AnalysisController}: a map of question id to the
 * chosen {@code POSITIVE}/{@code NEGATIVE} pole.
 */
public record AnalysisRequest(Map<String, String> answers) {
}
