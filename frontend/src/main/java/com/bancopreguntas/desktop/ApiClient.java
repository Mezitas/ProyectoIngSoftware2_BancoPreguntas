package com.bancopreguntas.desktop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

final class ApiClient {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(4))
            .build();

    private final String users = baseUrl("BANCO_API_USERS_URL", "http://localhost:8081");
    private final String questions = baseUrl("BANCO_API_QUESTIONS_URL", "http://localhost:8083");
    private final String reviews = baseUrl("BANCO_API_REVIEWS_URL", "http://localhost:8085");
    private final String notifications = baseUrl("BANCO_API_NOTIFICATIONS_URL", "http://localhost:8086");

    private static String baseUrl(String variable, String fallback) {
        String value = System.getenv(variable);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.replaceAll("/+$", "");
    }

    JsonNode login(String username, String password) {
        ObjectNode body = mapper.createObjectNode()
                .put("username", username)
                .put("password", password);
        return send(users, "/auth/login", "POST", body);
    }

    JsonNode allQuestions() {
        return send(questions, "/questions", "GET", null);
    }

    JsonNode question(long id) {
        return send(questions, "/questions/" + id, "GET", null);
    }

    JsonNode saveQuestion(Long id, ObjectNode body) {
        return send(questions, id == null ? "/questions" : "/questions/" + id,
                id == null ? "POST" : "PUT", body);
    }

    JsonNode submitQuestion(long id, long authorId) {
        return send(questions, "/questions/" + id + "/submit", "POST",
                mapper.createObjectNode().put("requestedBy", authorId));
    }

    JsonNode operation(String id) {
        return send(questions, "/operations/" + id, "GET", null);
    }

    JsonNode assignmentsForQuestion(long questionId) {
        return send(reviews, "/reviews/question/" + questionId, "GET", null);
    }

    JsonNode assignmentsForReviewer(long reviewerId) {
        return send(reviews, "/reviews/reviewer/" + reviewerId, "GET", null);
    }

    JsonNode reviewResults(long questionId) {
        return send(reviews, "/reviews/question/" + questionId + "/results", "GET", null);
    }

    JsonNode assignReviewer(long questionId, long reviewerId, long adminId) {
        ObjectNode body = mapper.createObjectNode()
                .put("questionId", questionId)
                .put("reviewerId", reviewerId)
                .put("assignedBy", adminId);
        return send(reviews, "/reviews/assign", "POST", body);
    }

    JsonNode submitReview(long questionId, long reviewerId, String result, String comments) {
        ObjectNode body = mapper.createObjectNode()
                .put("questionId", questionId)
                .put("reviewerId", reviewerId)
                .put("result", result)
                .put("comments", comments);
        return send(reviews, "/reviews/result", "POST", body);
    }

    JsonNode notifications(long recipientId) {
        return send(notifications, "/notifications?recipientId=" + recipientId, "GET", null);
    }

    private JsonNode send(String base, String path, String method, JsonNode body) {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
                .timeout(Duration.ofSeconds(15))
                .header("Accept", "application/json");
        if (body == null) {
            request.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            try {
                request.header("Content-Type", "application/json")
                        .method(method, HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)));
            } catch (IOException exception) {
                throw new ApiException("No fue posible preparar la solicitud al backend.", exception);
            }
        }

        try {
            HttpResponse<String> response = http.send(request.build(), HttpResponse.BodyHandlers.ofString());
            String content = response.body();
            JsonNode result = content == null || content.isBlank() ? mapper.nullNode() : mapper.readTree(content);
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException(errorMessage(result, response.statusCode()));
            }
            return result;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ApiException("La solicitud al backend fue interrumpida.", exception);
        } catch (IOException | IllegalArgumentException exception) {
            throw new ApiException("No fue posible conectar con " + base + ". Verifica que el servicio esté iniciado.", exception);
        }
    }

    private static String errorMessage(JsonNode body, int status) {
        for (String field : new String[]{"message", "detail", "error", "title"}) {
            JsonNode value = body == null ? null : body.get(field);
            if (value != null && value.isValueNode() && !value.asText().isBlank()) {
                return value.asText();
            }
        }
        return "Error HTTP " + status + " del backend.";
    }

    static final class ApiException extends RuntimeException {
        ApiException(String message) {
            super(message);
        }

        ApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
