package com.unconscious.collective.quiz.controller;

import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;

/**
 * Public representation of a quiz question.
 */
public record QuestionResponse(String id, Axis axis, String title, String negative, String positive) {

    /** Creates the API question representation, using the axis title as its title. */
    public static QuestionResponse from(BipolarQuestion question) {
        return new QuestionResponse(
                question.id(),
                question.axis(),
                question.axis().title(),
                question.negativeStatement(),
                question.positiveStatement());
    }
}
