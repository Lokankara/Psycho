package com.unconscious.collective.qa;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class BaseUrl {

    private BaseUrl() {
    }

    public static final String VALUE = resolve();
    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final Duration WARMUP_POLL_INTERVAL = Duration.ofSeconds(5);
    private static final int WARMUP_MAX_ATTEMPTS = 12;

    private static String resolve() {
        String fromProperty = System.getProperty("baseUrl");
        if (fromProperty != null && !fromProperty.isBlank()) {
            return fromProperty.trim();
        }
        String fromEnvironment = System.getenv("BASE_URL");
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment.trim();
        }
        return System.getProperty("BASE_URL", "https://collective-unconscious.onrender.com").trim();
    }

    public static void warmUp() {
        String target = VALUE + "/api/analysis/questions";
        for (int attempt = 1; attempt <= WARMUP_MAX_ATTEMPTS; attempt++) {
            try {
                HttpRequest request = HttpRequest.newBuilder(URI.create(target))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    return;
                }
            } catch (IOException | InterruptedException ignored) {
                Thread.interrupted();
            }
            try {
                Thread.sleep(WARMUP_POLL_INTERVAL.toMillis());
            } catch (InterruptedException ignored) {
                Thread.interrupted();
                return;
            }
        }
    }
}
