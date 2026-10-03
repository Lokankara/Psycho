package com.unconscious.collective.quiz.service;

import com.unconscious.collective.quiz.dao.QuizResult;
import com.unconscious.collective.quiz.dao.QuizResultRepository;
import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.archetype.NarrativeArchetype;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AnalysisResult;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Coordinates;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.value.Vector3D;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persists completed profiles and exposes the run history.
 */
@Service
public class ResultService {

    private static final Logger LOG = LoggerFactory.getLogger(ResultService.class);

    private final QuizResultRepository repository;
    private final ArchetypeScoringService scoringService;
    private final SemanticMatchingService matchingService;

    public ResultService(QuizResultRepository repository,
                         ArchetypeScoringService scoringService,
                         SemanticMatchingService matchingService) {
        this.repository = repository;
        this.scoringService = scoringService;
        this.matchingService = matchingService;
    }

    /**
     * Persists a new snapshot of the profile's coordinates and octant with the current time.
     * Persistence failures propagate to the caller.
     *
     * @return the saved entity, including its generated ID
     */
    public QuizResult save(SemanticProfile profile) {
        Coordinates coordinates = profile.coordinates();
        QuizResult result = new QuizResult(
                Instant.now(),
                coordinates.x(),
                coordinates.y(),
                coordinates.z(),
                profile.octant().name());
        QuizResult save = repository.save(result);
        LOG.info("QuizResult saved {}", save);
        return save;
    }

    /**
     * Scores and matches an assessment without persisting it.
     * Null pole choices are skipped; the last non-null choice for a duplicate question
     * ID wins. IDs absent from the supplied bank are ignored, and unanswered axes score zero.
     *
     * @return an analysis identified by the payload's session ID, with the dominant
     *         drive's narrative and the scored octant's shadow
     */
    public AnalysisResult analyze(AssessmentPayload payload, List<BipolarQuestion> questionsBank) {
        Map<String, Pole> answers = new HashMap<>();
        for (QuizAnswer answer : payload.answers()) {
            if (answer.chosenPole() != null) {
                answers.put(answer.questionId(), answer.chosenPole());
            }
        }

        SemanticProfile profile = scoringService.score(questionsBank, answers);
        ArchetypeMatch match = matchingService.match(profile);
        Coordinates coordinates = profile.coordinates();

        return new AnalysisResult(
                payload.sessionId(),
                new Vector3D(coordinates.x(), coordinates.y(), coordinates.z()),
                match.octant(),
                match.archetype(),
                NarrativeArchetype.from(match.coreDrive()),
                profile.octant().shadow(),
                match.coreDrive(),
                match.confidence(),
                match.symbols()
        );
    }

    /**
     * Returns all saved results ordered from newest to oldest, without a result limit.
     * Persistence failures propagate to the caller.
     */
    public List<QuizResult> recent() {
        return repository.findAllByOrderByCreatedAtDesc();
    }
}
