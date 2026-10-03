package com.unconscious.collective.quiz.controller;

import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AnalysisResult;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.service.QuizService;
import com.unconscious.collective.quiz.service.ResultService;
import com.unconscious.collective.quiz.service.SemanticMatchingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin REST API over the quiz/scoring/matching services.
 */
@RestController
@RequestMapping("/api/analysis")
public class AnalysisController {

    private final QuizService quizService;
    private final ResultService resultService;
    private final SemanticMatchingService matchingService;

    public AnalysisController(QuizService quizService, ResultService resultService,
                              SemanticMatchingService matchingService) {
        this.quizService = quizService;
        this.resultService = resultService;
        this.matchingService = matchingService;
    }

    /** Returns the question bank in presentation order with axis titles and both statements. */
    @GetMapping("/questions")
    public List<QuestionResponse> questions() {
        return quizService.questions().stream()
                .map(QuestionResponse::from)
                .toList();
    }

    /**
     * Returns an assessment analysis with HTTP 200 without saving a history entry.
     * Duplicate question IDs use their last non-null pole; unanswered axes score zero.
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> processAssessment(@RequestBody AssessmentPayload payload) {
        AnalysisResult result = resultService.analyze(payload, quizService.questions());
        return ResponseEntity.ok(result);
    }

    /**
     * Returns a scored and matched profile with HTTP 200 without saving a history entry.
     * A null or empty answer map is scored as unanswered. Pole names are trimmed and
     * uppercased; unknown question IDs do not contribute to the score.
     *
     * @throws ResponseStatusException with HTTP 400 if any pole value is null or unrecognized
     */
    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        SemanticProfile profile = quizService.evaluate(toAnswers(request.answers()));
        ArchetypeMatch match = matchingService.match(profile);
        return ResponseEntity.ok(AnalysisResponse.from(profile, match));
    }

    /**
     * Converts pole names keyed by question ID; a null or empty map yields an empty map.
     *
     * @throws ResponseStatusException with HTTP 400 if any pole value is null or unrecognized
     */
    private static Map<String, Pole> toAnswers(Map<String, String> rawAnswers) {
        if (rawAnswers == null || rawAnswers.isEmpty()) {
            return Map.of();
        }
        return rawAnswers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toPole(entry.getKey(), entry.getValue())));
    }

    /**
     * Resolves a trimmed, uppercased pole name.
     *
     * @param questionId identifier included in validation errors
     * @throws ResponseStatusException with HTTP 400 if the value is null or unrecognized
     */
    private static Pole toPole(String questionId, String rawValue) {
        if (rawValue == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Pole value for '" + questionId + "' must not be null");
        }
        try {
            return Pole.valueOf(rawValue.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Unrecognized pole '" + rawValue + "' for '" + questionId + "'");
        }
    }
}
