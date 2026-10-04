package com.unconscious.collective.quiz.service;


import com.unconscious.collective.quiz.domain.value.Axis;
import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * Loads the bipolar question bank from the classpath resource {@code /questions.json}.
 * The bank is immutable and shared across all quiz sessions.
 */
@Component
public class QuestionBank {

    private final List<BipolarQuestion> questions;

    /**
     * Loads the shared question bank from {@code /questions.json} in resource order.
     * Unchecked JSON parsing and mapping errors propagate to the caller.
     *
     * @throws IllegalStateException if the resource is missing or closing its stream fails
     * @throws IllegalArgumentException if a question names an unknown axis
     * @throws NullPointerException if a parsed question has a null axis
     */
    public QuestionBank(ObjectMapper objectMapper) {
        this.questions = load(objectMapper);
    }

    /**
     * Returns an unmodifiable question list from the classpath resource.
     * Axis names are trimmed and uppercased before lookup. Unchecked JSON parsing
     * and mapping errors propagate; caught I/O errors become {@link IllegalStateException}.
     *
     * @throws IllegalStateException if the resource is missing or closing its stream fails
     * @throws IllegalArgumentException if a normalized axis name is unknown
     * @throws NullPointerException if a parsed question has a null axis
     */
    private static List<BipolarQuestion> load(ObjectMapper objectMapper) {
        try (InputStream in = QuestionBank.class.getResourceAsStream("/questions.json")) {
            if (in == null) {
                throw new IllegalStateException("questions.json was not found on the classpath");
            }
            QuestionJson[] parsed = objectMapper.readValue(in, QuestionJson[].class);
            return Arrays.stream(parsed)
                    .map(q -> new BipolarQuestion(
                            q.id(),
                            Axis.valueOf(q.axis().trim().toUpperCase()),
                            q.negative(),
                            q.positive()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read questions.json", e);
        }
    }

    /** All questions in presentation order. */
    public List<BipolarQuestion> all() {
        return questions;
    }

    public int size() {
        return questions.size();
    }

    /** JSON transfer object used only for parsing. */
    record QuestionJson(String id, String axis, String negative, String positive) {
    }
}
