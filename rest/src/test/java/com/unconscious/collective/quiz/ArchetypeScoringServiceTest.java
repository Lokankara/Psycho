package com.unconscious.collective.quiz;

import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.SemanticProfile;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.service.ArchetypeScoringService;
import com.unconscious.collective.quiz.service.QuestionBank;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ArchetypeScoringServiceTest {

    private final QuestionBank bank = new QuestionBank(new ObjectMapper());
    private final ArchetypeScoringService service = new ArchetypeScoringService();

    @Test
    void bankContainsBalancedQuestionsForEveryAxis() {
        List<BipolarQuestion> questions = bank.all();
        assertFalse(questions.isEmpty());
        for (Axis axis : Axis.values()) {
            long count = questions.stream().filter(q -> q.axis() == axis).count();
            assertTrue(count >= 4, "Expected several questions for axis " + axis);
        }
    }

    @Test
    void mapsEveryOctantToTheExpectedCoordinates() {
        for (Octant expected : Octant.values()) {
            SemanticProfile profile = service.score(bank.all(), answersFor(expected));
            assertEquals(expected, profile.octant(), "Wrong octant for " + expected);
            for (Axis axis : Axis.values()) {
                double expectedValue = expected.isPositive(axis) ? 1.0 : -1.0;
                assertEquals(expectedValue, profile.coordinates().value(axis), 1e-9);
            }
        }
    }

    @Test
    void emptyAnswersProduceZeroCoordinatesAndDefaultOctant() {
        SemanticProfile profile = service.score(bank.all(), Map.of());
        assertEquals(0.0, profile.coordinates().x(), 1e-9);
        assertEquals(0.0, profile.coordinates().y(), 1e-9);
        assertEquals(0.0, profile.coordinates().z(), 1e-9);
        assertEquals(Octant.PROPHET_IDEOLOGUE, profile.octant());
    }

    @Test
    void balancedAnswersOnAnAxisCancelOut() {
        Map<String, Pole> answers = new HashMap<>();
        int flip = 0;
        for (BipolarQuestion question : bank.all()) {
            if (question.axis() == Axis.X) {
                answers.put(question.id(), flip++ % 2 == 0 ? Pole.NEGATIVE : Pole.POSITIVE);
            }
        }
        SemanticProfile profile = service.score(bank.all(), answers);
        assertEquals(0.0, profile.coordinates().x(), 1e-9);
//        assertEquals(bank.all().stream().filter(q -> q.axis() == Axis.X).count());
    }

    private Map<String, Pole> answersFor(Octant octant) {
        Map<String, Pole> answers = new HashMap<>();
        for (BipolarQuestion question : bank.all()) {
            boolean positive = octant.isPositive(question.axis());
            answers.put(question.id(), positive ? Pole.POSITIVE : Pole.NEGATIVE);
        }
        return answers;
    }
}
