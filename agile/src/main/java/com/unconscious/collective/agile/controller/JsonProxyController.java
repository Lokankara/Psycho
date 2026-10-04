package com.unconscious.collective.agile.controller;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/json")
public class JsonProxyController {

    private final String baseUrl;
    private final HttpClient http;

    public JsonProxyController(@Value("${json.store.base-url:http://127.0.0.1:8080}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    }

    /** Lists stored JSON items from the Flask store. */
    @GetMapping
    public ResponseEntity<byte[]> list() {
        return forward("GET", "/json", null);
    }

    /** Creates one JSON item in the Flask store. */
    @PostMapping
    public ResponseEntity<byte[]> create(@RequestBody(required = false) String body) {
        return forward("POST", "/json", body);
    }

    /** Reads one JSON item from the Flask store. */
    @GetMapping("/{name}")
    public ResponseEntity<byte[]> read(@PathVariable String name) {
        return forward("GET", "/json/" + name, null);
    }

    /** Creates or overwrites one JSON item in the Flask store. */
    @PutMapping("/{name}")
    public ResponseEntity<byte[]> write(@PathVariable String name, @RequestBody(required = false) String body) {
        return forward("PUT", "/json/" + name, body);
    }

    /** Deletes one JSON file from the Flask store. */
    @DeleteMapping("/{name}")
    public ResponseEntity<byte[]> delete(@PathVariable String name) {
        return forward("DELETE", "/json/" + name, null);
    }

    private ResponseEntity<byte[]> forward(String method, String path, String body) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json");
            switch (method) {
                case "GET" -> builder.GET();
                case "DELETE" -> builder.DELETE();
                case "PUT" -> builder.PUT(HttpRequest.BodyPublishers.ofString(body == null ? "null" : body));
                case "POST" -> builder.POST(HttpRequest.BodyPublishers.ofString(body == null ? "null" : body));
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported method " + method);
            }
            HttpResponse<byte[]> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());
            String contentType = response.headers().firstValue("Content-Type").orElse("application/json");
            return ResponseEntity.status(response.statusCode())
                    .header("Content-Type", contentType)
                    .body(response.body());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "JSON store unavailable", e);
        }
    }
}
