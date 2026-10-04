package com.unconscious.collective.qa;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class Report {

    public static final Path FAILURE_LOG_DIR = Path.of("..", "logs", "e2e");

    private Report() {
    }

    public static Path writeFailure(String label, Throwable failure) {
        try {
            Files.createDirectories(FAILURE_LOG_DIR);
            Path target = FAILURE_LOG_DIR.resolve(safe(label) + "_" + stamp() + ".log");
            Files.writeString(target, render(label, failure), StandardCharsets.UTF_8);
            return target;
        } catch (IOException writeFailure) {
            return null;
        }
    }

    private static String render(String label, Throwable failure) {
        StringWriter text = new StringWriter();
        PrintWriter writer = new PrintWriter(text);
        writer.println("BDD failure: " + label);
        writer.println("Base URL: " + BaseUrl.VALUE);
        writer.println("Timestamp: " + LocalDateTime.now());
        writer.println("Failure:");
        failure.printStackTrace(writer);
        return text.toString();
    }

    private static String stamp() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    }

    private static String safe(String label) {
        return label.replaceAll("[^a-zA-Z0-9._-]+", "_");
    }
}
