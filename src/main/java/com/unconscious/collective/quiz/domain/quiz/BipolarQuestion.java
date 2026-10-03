package com.unconscious.collective.quiz.domain.quiz;

import com.unconscious.collective.quiz.domain.value.Pole;
import com.unconscious.collective.quiz.domain.value.Axis;

/**
 * A bipolar forced-choice question: the user picks between two opposite
 * statements that both belong to the same {@link Axis}. The chosen pole earns
 * one vote for that axis.
 *
 * @param id                stable identifier (used as the answer key)
 * @param axis              the axis measured by this item
 * @param negativeStatement statement keyed to the &minus; pole of the axis
 * @param positiveStatement statement keyed to the &plus; pole of the axis
 */
public record BipolarQuestion(
        String id,
        Axis axis,
        String negativeStatement,
        String positiveStatement) implements QuestionItem {

    /** Returns the positive statement for POSITIVE, and the negative statement otherwise, including null. */
    public String statement(Pole pole) {
        return pole == Pole.POSITIVE ? positiveStatement : negativeStatement;
    }
}
