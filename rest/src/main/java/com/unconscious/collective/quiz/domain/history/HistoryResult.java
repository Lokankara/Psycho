package com.unconscious.collective.quiz.domain.history;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class HistoryResult {

    private static final Logger LOG = LoggerFactory.getLogger(HistoryResult.class);
    private static final DateTimeFormatter STAMP =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").withZone(ZoneId.systemDefault());

    private final Path directory;
    private final ObjectMapper mapper;

    public HistoryResult(@Value("${app.history.dir:../result/history}") String configuredDirectory,
            ObjectMapper objectMapper) {
        this.directory = Path.of(configuredDirectory);
        this.mapper = objectMapper;
    }

    public Optional<Path> write(ExecutionTrace trace) {
        try {
            Files.createDirectories(directory);
            Path target = uniqueFile(STAMP.format(trace.timestamp()));
            Files.writeString(target, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(trace));
            LOG.info("Execution trace written to {}", target);
            return Optional.of(target);
        } catch (IOException e) {
            LOG.warn("Unable to write execution trace to {}: {}", directory, e.getMessage());
            return Optional.empty();
        }
    }

    private Path uniqueFile(String stamp) {
        Path candidate = directory.resolve("result_" + stamp + ".json");
        int suffix = 1;
        while (Files.exists(candidate)) {
            candidate = directory.resolve("result_" + stamp + "_" + suffix + ".json");
            suffix++;
        }
        return candidate;
    }
}
