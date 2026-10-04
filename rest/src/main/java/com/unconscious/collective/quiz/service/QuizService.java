package com.unconscious.collective.quiz.service;

import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Pole;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Application-facing facade over the question bank and the scoring engine.
 */
@Service
public class QuizService {

    private final QuestionBank questionBank;
    private final ArchetypeScoringService scoringService;

    public QuizService(QuestionBank questionBank, ArchetypeScoringService scoringService) {
        this.questionBank = questionBank;
        this.scoringService = scoringService;
    }

    /** Returns the shared, unmodifiable question bank in presentation order. */
    public List<BipolarQuestion> getQuestions() {
        return questionBank.all();
    }

    /**
     * Evaluates the answers currently collected in a session.
     * Missing or null choices and unknown question IDs are ignored. Unanswered
     * axes score zero and select the positive pole. Each call creates a profile
     * with a new subject ID and no matches.
     */
    public SemanticProfile evaluate(Map<String, Pole> answers) {
        List<QuizAnswer> converted = answers.entrySet().stream()
                .map(entry -> new QuizAnswer(entry.getKey(), entry.getValue()))
                .toList();
        return scoringService.score(
                questionBank.all(),
                new AssessmentPayload(UUID.randomUUID().toString(), converted));
    }
}
