package com.unconscious.collective.quiz.domain.quiz;

import com.unconscious.collective.quiz.domain.value.Pole;

import java.io.Serializable;

/**
 * The user's answer to a single {@link BipolarQuestion}.
 */
public record QuizAnswer(String questionId, Pole chosenPole) implements Serializable {
}
