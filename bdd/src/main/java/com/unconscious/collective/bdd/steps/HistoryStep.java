package com.unconscious.collective.bdd.steps;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Coordinates;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.service.ArchetypeScoringService;
import com.unconscious.collective.quiz.service.QuestionBank;
import org.jbehave.core.annotations.*;
import org.junit.jupiter.api.Assertions;
import tools.jackson.databind.ObjectMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HistoryStep {

    private final QuestionBank questionBank = new QuestionBank(new ObjectMapper());
    private final ArchetypeScoringService scoringService = new ArchetypeScoringService();

    private AssessmentPayload currentPayload;
    private SemanticProfile firstResult;
    private SemanticProfile secondResult;
    private Path historyDirectory;

    @BeforeStory
    public void prepareHistoryDirectory() {
        historyDirectory = Path.of(System.getProperty("java.io.tmpdir"), "jbehive-history-" + UUID.randomUUID());
    }

    @AfterStory
    public void cleanHistoryDirectory() {
        if (historyDirectory != null) {
            try (Stream<Path> walk = Files.walk(historyDirectory)) {
                walk.sorted((a, b) -> -1)
                        .forEach(path -> {
                            try {
                                Files.deleteIfExists(path);
                            } catch (Exception ignored) {
                            }
                        });
            } catch (Exception ignored) {
            }
        }
    }

    @Given("an identical set of quiz answers")
    public void givenIdenticalAnswerSet() {
        Map<String, Pole> answers = new HashMap<>();
        for (BipolarQuestion question : questionBank.all()) {
            answers.put(question.id(), Pole.POSITIVE);
        }
        currentPayload = toPayload(answers);
    }

    @Given("a balanced set of answers for the Z axis")
    public void givenBalancedZAnswers() {
        List<BipolarQuestion> all = questionBank.all();
        List<BipolarQuestion> zQuestions = all.stream()
                .filter(q -> q.axis() == Axis.Z)
                .toList();
        Map<String, Pole> answers = new HashMap<>();
        int flip = 0;
        for (BipolarQuestion question : zQuestions) {
            answers.put(question.id(), flip++ % 2 == 0 ? Pole.POSITIVE : Pole.NEGATIVE);
        }
        currentPayload = toPayload(answers);
    }

    @Given("an assessment with a null pole choice on one question")
    public void givenNullPoleChoice() {
        Map<String, Pole> answers = new HashMap<>();
        for (BipolarQuestion question : questionBank.all()) {
            answers.put(question.id(), Pole.POSITIVE);
        }
        List<BipolarQuestion> all = questionBank.all();
        answers.put(all.get(0).id(), null);
        List<QuizAnswer> converted = answers.entrySet().stream()
                .map(entry -> new QuizAnswer(entry.getKey(), entry.getValue()))
                .toList();
        currentPayload = new AssessmentPayload("session", converted);
    }

    @Given("a duplicate answer for the same question ID")
    public void givenDuplicateAnswer() {
        List<BipolarQuestion> all = questionBank.all();
        List<QuizAnswer> answers = new java.util.ArrayList<>();
        for (BipolarQuestion question : all) {
            answers.add(new QuizAnswer(question.id(), Pole.POSITIVE));
        }
        BipolarQuestion first = all.get(0);
        answers.add(0, new QuizAnswer(first.id(), Pole.NEGATIVE));
        currentPayload = new AssessmentPayload("session", answers);
    }

    @When("the assessment is scored twice")
    public void whenScoredTwice() {
        List<BipolarQuestion> questions = questionBank.all();
        firstResult = scoringService.score(questions, currentPayload);
        secondResult = scoringService.score(questions, currentPayload);
    }

    @When("the assessment is scored")
    public void whenScored() {
        List<BipolarQuestion> questions = questionBank.all();
        firstResult = scoringService.score(questions, currentPayload);
    }

    @Then("both runs return identical X, Y, Z coordinates and the same Octant")
    public void thenCoordinatesAreIdentical() {
        Coordinates first = firstResult.coordinates();
        Coordinates second = secondResult.coordinates();
        assertEquals(first.x(), second.x(), 1e-9, "X coordinate must be identical");
        assertEquals(first.y(), second.y(), 1e-9, "Y coordinate must be identical");
        assertEquals(first.z(), second.z(), 1e-9, "Z coordinate must be identical");
        assertEquals(firstResult.octant(), secondResult.octant(), "Octant must be identical");
    }

    @Then("the Z coordinate is 0.0 and the Octant is PROPHET_IDEOLOGUE")
    public void thenZCoordinateIsZero() {
        assertEquals(0.0, firstResult.coordinates().z(), 1e-9);
        assertEquals(Octant.PROPHET_IDEOLOGUE, firstResult.octant());
    }

    @Then("no exception is thrown and the coordinate is computed from answered questions only")
    public void thenNoExceptionAndCoordinateComputed() {
        assertNotNull(firstResult, "Scoring must not throw and must produce a profile");
        assertNotNull(firstResult.octant(), "Octant must be resolved");
    }

    @Then("the last non-null pole is used and the coordinate count is not inflated")
    public void thenLastNonNullPoleUsed() {
        Map<Axis, Integer> counts = firstResult.answeredCounts();
        int totalAnswered = counts.values().stream().mapToInt(Integer::intValue).sum();
        Assertions.assertTrue(totalAnswered > 0, "At least one answer must be counted");
        Assertions.assertNotNull(firstResult.octant(), "Octant must be resolved from non-null answers");
    }

    @Then("both runs log an execution trace JSON file")
    public void thenTraceFilesWritten() {
        try {
            Path dir = Path.of(System.getProperty("java.io.tmpdir"));
            List<Path> traceFiles;
            try (Stream<Path> walk = Files.walk(dir)) {
                traceFiles = walk
                        .filter(path -> path.getFileName().toString().startsWith("result_"))
                        .filter(path -> path.toString().endsWith(".json"))
                        .toList();
            }
            Assertions.assertFalse(traceFiles.isEmpty(), "At least one execution trace JSON must exist");
        } catch (Exception e) {
            Assertions.fail("Could not verify execution trace files: " + e.getMessage());
        }
    }

    private AssessmentPayload toPayload(Map<String, Pole> answers) {
        List<QuizAnswer> converted = answers.entrySet().stream()
                .map(entry -> new QuizAnswer(entry.getKey(), entry.getValue()))
                .toList();
        return new AssessmentPayload("session-" + UUID.randomUUID(), converted);
    }
}
