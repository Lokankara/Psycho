package com.unconscious.collective.quiz.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.unconscious.collective.quiz.domain.quiz.BipolarQuestion;
import com.unconscious.collective.quiz.domain.value.Axis;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Loads the bipolar question bank from every classpath resource matching
 * {@code quiz/set_*.json}, concatenated in ascending set order so that
 * presentation order is reproducible across runs.
 * The bank is immutable and shared across all quiz sessions.
 */
@Component
public class QuestionBank {

    private static final Pattern SET_RESOURCE = Pattern.compile("set_(\\d+)\\.json");

    private final List<BipolarQuestion> questions;

    /**
     * Loads the shared question bank from every {@code quiz/set_*.json} resource in set order.
     * Unchecked JSON parsing and mapping errors propagate to the caller.
     *
     * @throws IllegalStateException    if no resource matches or closing a stream fails
     * @throws IllegalArgumentException if a question names an unknown axis or duplicates an id
     * @throws NullPointerException     if a parsed question has a null axis
     */
    public QuestionBank(ObjectMapper objectMapper) {
        this.questions = List.copyOf(load(objectMapper));
    }

    /**
     * Returns an unmodifiable question list in presentation order.
     * Axis names are trimmed and uppercased before lookup. Unchecked JSON parsing
     * and mapping errors propagate; caught I/O errors become {@link IllegalStateException}.
     *
     * @throws IllegalStateException    if no {@code quiz/set_*.json} matches or closing a stream fails
     * @throws IllegalArgumentException if a normalized axis name is unknown
     * @throws NullPointerException     if a parsed question has a null axis
     */
    private static List<BipolarQuestion> load(ObjectMapper objectMapper) {
        List<Resource> resources = resolveSets();
        if (resources.isEmpty()) {
            throw new IllegalStateException("no quiz/set_*.json was found on the classpath");
        }
        List<BipolarQuestion> loaded = new ArrayList<>();
        for (Resource resource : resources) {
            loaded.addAll(readSet(objectMapper, resource));
        }
        requireUniqueIds(loaded);
        return loaded;
    }

    /**
     * Resolves every {@code quiz/set_*.json} classpath resource ordered by set number,
     * so that {@code set_0.json} always precedes {@code set_1.json} regardless of the
     * order in which the class loader reports them.
     *
     * @throws IllegalStateException if the classpath cannot be searched
     */
    private static List<Resource> resolveSets() {
        Resource[] found;
        try {
            found = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:quiz/set_*.json");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to resolve quiz/set_*.json on the classpath", e);
        }
        List<Resource> resources = new ArrayList<>(Arrays.asList(found));
        resources.sort(Comparator.comparingInt(QuestionBank::setIndexOf));
        return resources;
    }

    /**
     * Returns the numeric part of a {@code set_N.json} filename, or
     * {@link Integer#MAX_VALUE} when the filename does not follow that shape.
     */
    private static int setIndexOf(Resource resource) {
        Matcher matcher = SET_RESOURCE.matcher(String.valueOf(resource.getFilename()));
        return matcher.matches() ? Integer.parseInt(matcher.group(1)) : Integer.MAX_VALUE;
    }

    /**
     * Parses a single question set file into presentation order.
     *
     * @throws IllegalStateException    if the resource is missing or closing its stream fails
     * @throws IllegalArgumentException if a question names an unknown axis
     * @throws NullPointerException     if a parsed question has a null axis
     */
    private static List<BipolarQuestion> readSet(ObjectMapper objectMapper, Resource resource) {
        try (InputStream in = resource.getInputStream()) {
            QuestionJson[] parsed = objectMapper.readValue(in, QuestionJson[].class);
            return Arrays.stream(parsed)
                    .map(q -> new BipolarQuestion(
                            q.id(),
                            Axis.valueOf(q.axis().trim().toUpperCase()),
                            q.negative(),
                            q.positive()))
                    .toList();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + resource.getFilename(), e);
        }
    }

    /** All questions in presentation order. */
    public List<BipolarQuestion> all() {
        return questions;
    }

    /**
     * Rejects a duplicated question id. The scoring axis map is keyed by question id and
     * silently keeps the last entry, so a duplicate would remap an existing answer onto
     * another axis and make coordinates depend on bank history instead of on the payload.
     *
     * @throws IllegalArgumentException if any id occurs more than once
     */
    private static void requireUniqueIds(List<BipolarQuestion> loaded) {
        Set<String> seen = new HashSet<>();
        loaded.stream().filter(question -> !seen.add(question.id())).forEach(question -> {
            throw new IllegalArgumentException("duplicate question id: " + question.id());
        });
    }

    /** JSON transfer object used only for parsing. */
    record QuestionJson(String id, String axis, String negative, String positive) {
    }
}
