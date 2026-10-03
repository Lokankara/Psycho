package com.unconscious.collective.quiz.domain.quiz;

import com.unconscious.collective.quiz.domain.value.Axis;

import java.io.Serializable;

/**
 * A single item of the quiz. Modelled as a sealed hierarchy so that different
 * answer formats (forced-choice, Likert, &hellip;) can be added safely.
 */
public sealed interface QuestionItem extends Serializable permits BipolarQuestion {

    String id();

    Axis axis();
}
