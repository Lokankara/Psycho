package com.unconscious.collective.quiz;

import com.unconscious.collective.quiz.domain.archetype.CoreDriveType;
import com.unconscious.collective.quiz.domain.archetype.NarrativeArchetype;
import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.PersonaArchetype;
import com.unconscious.collective.quiz.domain.dto.AnalysisResult;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.history.HistoryResult;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.service.ArchetypeScoringService;
import com.unconscious.collective.quiz.service.QuestionBank;
import com.unconscious.collective.quiz.service.ResultService;
import com.unconscious.collective.quiz.service.SemanticMatchingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ResultServiceTest {

    private final QuestionBank bank = new QuestionBank(new ObjectMapper());
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Path historyDir;
    private final HistoryResult historyResult;
    private final ResultService service;

    ResultServiceTest(@TempDir Path historyDir) {
        this.historyDir = historyDir;
        this.historyResult = new HistoryResult(historyDir.toString(), objectMapper);
        this.service = new ResultService(null, new ArchetypeScoringService(),
                new SemanticMatchingService(), historyResult);
    }

    @Test
    void analyzeScoresAllPositiveAnswersIntoTheExpectedOctant() {
        AnalysisResult result = analyze(allAnswers(Pole.POSITIVE));

        assertEquals(Octant.PROPHET_IDEOLOGUE, result.octant());
        assertEquals(1.0, result.position().x(), 1e-9);
        assertEquals(1.0, result.position().y(), 1e-9);
        assertEquals(1.0, result.position().z(), 1e-9);
        assertEquals(PersonaArchetype.EVERYMAN, result.dominant());
        assertEquals(CoreDriveType.BELONGING_AND_CONNECTION, result.drive());
        assertEquals(NarrativeArchetype.SACRIFICE, result.trajectory());
        assertNotNull(result.shadowManifestation());
        assertEquals(1.0, result.confidence(), 1e-9);
        assertFalse(result.symbols().isEmpty(), "octant must contribute archetypal symbols");
    }

    @Test
    void analyzeUsesThePayloadSessionIdAsObjectId() {
        String sessionId = UUID.randomUUID().toString();
        AssessmentPayload payload =
                new AssessmentPayload(sessionId, allAnswers(Pole.NEGATIVE));

        assertEquals(sessionId, service.analyze(payload, bank.all()).objectId());
    }

    @Test
    void analyzeSkipsAnswersWithUnknownPole() {
        List<QuizAnswer> answers = new ArrayList<>(bank.all().stream()
                .map(question -> new QuizAnswer(question.id(), Pole.POSITIVE))
                .toList());
        answers.set(0, new QuizAnswer(answers.get(0).questionId(), null));
        AnalysisResult result = service.analyze(
                new AssessmentPayload("session", answers), bank.all());

        assertEquals(Octant.PROPHET_IDEOLOGUE, result.octant());
    }

    @Test
    void analyzeWritesExecutionTraceJson() throws IOException {
        service.analyze(new AssessmentPayload("session", allAnswers(Pole.POSITIVE)), bank.all());

        try (Stream<Path> files = Files.list(historyDir)) {
            List<Path> jsonFiles = files
                    .filter(path -> path.getFileName().toString().startsWith("result_"))
                    .toList();
            assertFalse(jsonFiles.isEmpty(), "HistoryResult must write a result_*.json file");
            String content = Files.readString(jsonFiles.get(0));
            assertEquals("session", objectMapper.readTree(content).path("sessionId").asText());
        }
    }

    private AnalysisResult analyze(List<QuizAnswer> answers) {
        return service.analyze(new AssessmentPayload("session", answers), bank.all());
    }

    private List<QuizAnswer> allAnswers(Pole pole) {
        return bank.all().stream()
                .map(question -> new QuizAnswer(question.id(), pole))
                .toList();
    }
}