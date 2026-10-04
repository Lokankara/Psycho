package com.unconscious.collective.qa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.codeborne.selenide.Selenide;
import com.unconscious.collective.qa.ui.steps.Step;
import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.BeforeStories;

public final class TestHooks {

    private static final Path TRACE_FILE = Path.of("..", "result", "bdd_trace.jsonl");
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    protected TestHooks() {
    }

    @BeforeStories
    public void beforeAllStories() {
        BaseUrl.warmUp();
        Browser.open();
    }

    @AfterStories
    public void afterAllStories() {
        Browser.close();
    }

    public void afterEachStep(Step step) {
        String stepName = step.getStepAsString();
        String status = step.getResult() != null && step.getResult().isFailure()
                ? "FAILED"
                : "PASSED";
        String screenshot = "";

        if ("FAILED".equals(status)) {
            try {
                String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
                String safeName = stepName.replaceAll("[^a-zA-Z0-9._-]+", "_");
                Path screenshotDir = Path.of("..", "result", "screenshots");
                Files.createDirectories(screenshotDir);
                Path screenshotPath = screenshotDir.resolve(stamp + "_" + safeName + ".png");

                Selenide.screenshot(screenshotPath.toString());
                screenshot = screenshotPath.toString();
            } catch (Exception ignored) {
            }
        }

        String line = String.format(
                "{\"timestamp\":\"%s\",\"step\":\"%s\",\"status\":\"%s\",\"screenshot\":\"%s\"}%n",
                LocalDateTime.now().format(FORMATTER),
                stepName.replace("\"", "\\\""),
                status,
                screenshot
        );

        try {
            Files.createDirectories(TRACE_FILE.getParent());
            Files.writeString(TRACE_FILE, line,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write BDD trace: " + TRACE_FILE, e);
        }
    }
}
