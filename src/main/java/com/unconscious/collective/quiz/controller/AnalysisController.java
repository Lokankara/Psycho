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

    @GetMapping("/questions")
    public List<QuestionResponse> questions() {
        return quizService.questions().stream()
                .map(QuestionResponse::from)
                .toList();
    }

    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResult> processAssessment(@RequestBody AssessmentPayload payload) {
        AnalysisResult result = resultService.analyze(payload, quizService.questions());
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<AnalysisResponse> analyze(@RequestBody AnalysisRequest request) {
        SemanticProfile profile = quizService.evaluate(toAnswers(request.answers()));
        ArchetypeMatch match = matchingService.match(profile);
        return ResponseEntity.ok(AnalysisResponse.from(profile, match));
    }

    private static Map<String, Pole> toAnswers(Map<String, String> rawAnswers) {
        if (rawAnswers == null || rawAnswers.isEmpty()) {
            return Map.of();
        }
        return rawAnswers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> toPole(entry.getKey(), entry.getValue())));
    }

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
