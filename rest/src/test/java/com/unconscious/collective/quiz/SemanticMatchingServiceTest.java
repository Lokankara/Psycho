package com.unconscious.collective.quiz;

import com.unconscious.collective.quiz.domain.archetype.CoreDriveType;
import com.unconscious.collective.quiz.domain.archetype.ArchetypeMatch;
import com.unconscious.collective.quiz.domain.archetype.Octant;
import com.unconscious.collective.quiz.domain.archetype.PersonaArchetype;
import com.unconscious.collective.quiz.domain.dto.AssessmentPayload;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.quiz.QuizAnswer;
import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.service.ArchetypeScoringService;
import com.unconscious.collective.quiz.service.QuestionBank;
import com.unconscious.collective.quiz.service.SemanticMatchingService;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class SemanticMatchingServiceTest {

    private final QuestionBank bank = new QuestionBank(new ObjectMapper());
    private final ArchetypeScoringService scoring = new ArchetypeScoringService();
    private final SemanticMatchingService matching = new SemanticMatchingService();

    @Test
    void resolvesArchetypeDriveAndSymbolsForStrongProfile() {
        ArchetypeMatch match = matching.match(
                scoring.score(bank.all(), new AssessmentPayload("session", allAnswers(Pole.POSITIVE))));

        assertEquals(Octant.PROPHET_IDEOLOGUE, match.octant());
        assertEquals(PersonaArchetype.EVERYMAN, match.archetype());
        assertFalse(match.symbols().isEmpty());
        assertEquals(1.0, match.confidence(), 1e-9);
        assertEquals(CoreDriveType.BELONGING_AND_CONNECTION, match.coreDrive());
    }

    @Test
    void picksTheDriveOfTheStrongestAxis() {
        List<QuizAnswer> answers = new ArrayList<>();
        int x = 0;
        int y = 0;
        for (BipolarQuestion question : bank.all()) {
            Pole pole = switch (question.axis()) {
                case X -> (x++ % 2 == 0) ? Pole.NEGATIVE : Pole.POSITIVE;
                case Y -> (y++ % 2 == 0) ? Pole.NEGATIVE : Pole.POSITIVE;
                case Z -> Pole.POSITIVE;
            };
            answers.add(new QuizAnswer(question.id(), pole));
        }

        ArchetypeMatch match = matching.match(
                scoring.score(bank.all(), new AssessmentPayload("session", answers)));
        assertEquals(CoreDriveType.ABSTRACT_MEANING, match.coreDrive());
    }

    private List<QuizAnswer> allAnswers(Pole pole) {
        return bank.all().stream()
                .map(question -> new QuizAnswer(question.id(), pole))
                .toList();
    }
}
