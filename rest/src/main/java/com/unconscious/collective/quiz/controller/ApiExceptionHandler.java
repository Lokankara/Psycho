package com.unconscious.collective.quiz.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Maps API failures to {@code {"error": "..."}} JSON bodies so clients always
 * receive a human-readable message instead of a generic error page.
 */
@RestControllerAdvice(basePackages = "com.unconscious.collective.quiz.controller")
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Preserves the HTTP status and returns an error body using the reason, or exception message. */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException exception) {
        String message = exception.getReason() != null ? exception.getReason() : exception.getMessage();
        log.warn("request failed: {}", message);
        return ResponseEntity.status(exception.getStatusCode())
                .body(Map.of("error", message));
    }

    /** Returns HTTP 400 with the exception message, or "Invalid request" when the message is null. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        log.warn("illegal argument: {}", exception.getMessage());
        String message = exception.getMessage() != null ? exception.getMessage() : "Invalid request";
        return ResponseEntity.status(400)
                .body(Map.of("error", message));
    }
}
