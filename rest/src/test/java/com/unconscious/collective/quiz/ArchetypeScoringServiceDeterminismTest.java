package com.unconscious.collective.quiz;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.service.ArchetypeScoringService;
import com.unconscious.collective.quiz.service.QuestionBank;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression tests locking the fix for the non-deterministic coordinate
 * calculation reported in {@code task/bug/bug_report_coordinates.md}.
 *
 * <p>The reported defect: identical or near-identical submissions drifted
 * (e.g. {@code -1.00} vs {@code -0.67} on an axis) because null poles and
 * duplicate answers inflated the per-axis denominator {@code N}. These tests
 * reproduce that scenario and assert the coordinate no longer drifts and the
 * per-axis count is not inflated.</p>
 */
class ArchetypeScoringServiceDeterminismTest {

    private final QuestionBank bank = new QuestionBank(new ObjectMapper());
    private final ArchetypeScoringService service = new ArchetypeScoringService();

    @Test
    void scoringTheSamePayloadTwiceYieldsIdenticalCoordinatesAndOctant() {
        AssessmentPayload payload = payload(allNegative());

        SemanticProfile first = service.score(bank.all(), payload);
        SemanticProfile second = service.score(bank.all(), payload);

        assertEquals(first.coordinates().x(), second.coordinates().x(), 1e-9);
        assertEquals(first.coordinates().y(), second.coordinates().y(), 1e-9);
        assertEquals(first.coordinates().z(), second.coordinates().z(), 1e-9);
        assertEquals(first.octant(), second.octant());
    }

    @Test
    void nullPolesAndDuplicateAnswersDoNotDriftTheCoordinate() {
        List<BipolarQuestion> questions = bank.all();
        List<QuizAnswer> answers = new ArrayList<>(allNegative());
        answers.add(new QuizAnswer(questions.get(0).id(), Pole.NEGATIVE));
        answers.add(new QuizAnswer(questions.get(1).id(), null));

        SemanticProfile profile = service.score(questions, payload(answers));

        assertEquals(-1.0, profile.coordinates().x(), 1e-9);
        assertEquals(-1.0, profile.coordinates().y(), 1e-9);
        assertEquals(-1.0, profile.coordinates().z(), 1e-9);
        assertEquals(Octant.MASTER_PRAGMATIST, profile.octant());
    }

    @Test
    void duplicateAnswersDoNotInflateThePerAxisCount() {
        List<BipolarQuestion> questions = bank.all();
        BipolarQuestion xQuestion = questions.stream()
                .filter(question -> question.axis() == Axis.X)
                .findFirst()
                .orElseThrow();
        List<QuizAnswer> answers = new ArrayList<>(allNegative());
        answers.add(new QuizAnswer(xQuestion.id(), Pole.NEGATIVE));

        SemanticProfile profile = service.score(questions, payload(answers));

        int xQuestionCount = (int) questions.stream().filter(q -> q.axis() == Axis.X).count();
        assertEquals(xQuestionCount, profile.answeredCounts().get(Axis.X).intValue());
    }

    @Test
    void nullPoleAnswerIsSkippedWithoutInflatingTheCount() {
        List<BipolarQuestion> questions = bank.all();
        List<QuizAnswer> answers = new ArrayList<>(allNegative());
        answers.set(0, new QuizAnswer(questions.get(0).id(), null));

        SemanticProfile profile = service.score(questions, payload(answers));

        assertNotNull(profile.octant());
        int answered = profile.answeredCounts().values().stream().mapToInt(Integer::intValue).sum();
        assertEquals(questions.size() - 1, answered);
    }

    @Test
    void questionIdsAreUniqueSoTheAxisMapCannotRemapAnAnswer() {
        Set<String> seen = new HashSet<>();

        for (BipolarQuestion question : bank.all()) {
            assertTrue(seen.add(question.id()), "duplicate question id: " + question.id());
        }
    }

    @Test
    void questionBankIsImmutableSoRepeatedRunsRenderTheSameQuestions() {
        List<BipolarQuestion> firstRead = bank.all();
        List<BipolarQuestion> secondRead = bank.all();

        assertEquals(firstRead, secondRead);
        assertEquals(firstRead.stream().map(BipolarQuestion::id).toList(),
                secondRead.stream().map(BipolarQuestion::id).toList());
        assertThrows(UnsupportedOperationException.class, () -> firstRead.add(firstRead.get(0)));
        assertThrows(UnsupportedOperationException.class, () -> firstRead.remove(0));
    }

    private AssessmentPayload payload(List<QuizAnswer> answers) {
        return new AssessmentPayload("session", answers);
    }

    private List<QuizAnswer> allNegative() {
        return bank.all().stream()
                .map(question -> new QuizAnswer(question.id(), Pole.NEGATIVE))
                .toList();
    }
}
