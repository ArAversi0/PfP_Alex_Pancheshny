package com.pfp.desktop.foundation.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public final class DesktopAuthClient {
    private static final String DEFAULT_API_URL = "http://localhost:8080/api";

    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DesktopAuthClient() {
        this(defaultBaseUri(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build(), new ObjectMapper());
    }

    public DesktopAuthClient(URI baseUri, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUri = normalize(baseUri);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public URI baseUri() {
        return baseUri;
    }

    public AuthUser register(String email, String password, String confirmPassword) throws AuthException {
        return post("/v1/auth/register", Map.of(
                "email", email,
                "password", password,
                "confirmPassword", confirmPassword
        ), null, AuthUser.class);
    }

    public AuthSession login(String email, String password) throws AuthException {
        return post("/v1/auth/login", Map.of(
                "email", email,
                "password", password
        ), null, AuthSession.class);
    }

    public AuthUser currentUser(AuthSession session) throws AuthException {
        return get("/v1/auth/me", session.authorizationHeader(), AuthUser.class);
    }

    public void checkServerAvailable() throws AuthException {
        HttpRequest request = HttpRequest.newBuilder(resolve("/v1/auth/me"))
                .timeout(Duration.ofSeconds(5))
                .header("Accept", "application/json")
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 200 || status == 401 || status == 403) {
                return;
            }
            throw new AuthException(humanizeError(status, response.body()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AuthException("The request was interrupted.", exception);
        } catch (IOException exception) {
            throw new AuthException("The server is unavailable. Check that the backend is running.", exception);
        }
    }

    public String resendVerification(String email) throws AuthException {
        JsonNode response = post("/v1/auth/verify-email/resend", Map.of("email", email), null, JsonNode.class);
        return response.path("message").asText("Verification email requested.");
    }

    public void logout(AuthSession session) throws AuthException {
        if (session.refreshToken() == null || session.refreshToken().isBlank()) {
            return;
        }
        post("/v1/auth/logout", Map.of("refreshToken", session.refreshToken()), session.authorizationHeader(), JsonNode.class);
    }

    private <T> T get(String path, String authorization, Class<T> responseType) throws AuthException {
        HttpRequest request = HttpRequest.newBuilder(resolve(path))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .header("Authorization", authorization)
                .GET()
                .build();
        return send(request, responseType);
    }

    private <T> T post(String path, Object payload, String authorization, Class<T> responseType) throws AuthException {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(resolve(path))
                    .timeout(Duration.ofSeconds(12))
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)));
            if (authorization != null && !authorization.isBlank()) {
                builder.header("Authorization", authorization);
            }
            return send(builder.build(), responseType);
        } catch (IOException exception) {
            throw new AuthException("Could not prepare the request.", exception);
        }
    }

    private <T> T send(HttpRequest request, Class<T> responseType) throws AuthException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                if (responseType == Void.class) {
                    return null;
                }
                return objectMapper.readValue(response.body(), responseType);
            }
            throw new AuthException(humanizeError(response.statusCode(), response.body()));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AuthException("The request was interrupted.", exception);
        } catch (IOException exception) {
            throw new AuthException("The server is unavailable. Check that the backend is running.", exception);
        }
    }

    private String humanizeError(int status, String body) {
        String message = extractMessage(body);
        if (message == null || message.isBlank()) {
            if (status == 401) {
                return "Authentication is required. Please sign in again.";
            }
            if (status == 403) {
                return "You do not have permission to perform this action.";
            }
            return "The server could not process the request (" + status + ").";
        }
        return switch (stripDiagnostics(message).toLowerCase()) {
            case "email is already registered" ->
                    "An account with this email already exists. Sign in instead.";
            case "email verification is required" ->
                    "Please verify your email before signing in.";
            case "invalid email or password" ->
                    "The email or password is incorrect.";
            case "password confirmation does not match" ->
                    "The password confirmation does not match.";
            case "password must contain at least 8 characters" ->
                    "Password must be at least 8 characters long.";
            case "refresh token is invalid or expired" ->
                    "Your session has expired. Please sign in again.";
            case "user account is unavailable" ->
                    "This account is no longer available.";
            default -> stripDiagnostics(message);
        };
    }

    private String extractMessage(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            JsonNode json = objectMapper.readTree(body);
            JsonNode fieldErrors = json.path("fieldErrors");
            if (fieldErrors.isArray() && !fieldErrors.isEmpty()) {
                JsonNode first = fieldErrors.get(0);
                String field = first.path("field").asText("");
                String message = first.path("message").asText("");
                return field.isBlank() ? message : field + ": " + message;
            }
            if (json.hasNonNull("message")) {
                return json.path("message").asText();
            }
            if (json.hasNonNull("error")) {
                return json.path("error").asText();
            }
        } catch (IOException ignored) {
            return body;
        }
        return body;
    }

    private static String stripDiagnostics(String message) {
        return message
                .replaceFirst("^[A-Za-z0-9_.]*Exception:\\s*", "")
                .replaceFirst("\\s+at\\s+com\\.pfp\\..*$", "")
                .trim();
    }

    private static URI defaultBaseUri() {
        String configured = System.getProperty("pfp.apiUrl");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PFP_DESKTOP_API_URL");
        }
        if (configured == null || configured.isBlank()) {
            configured = DEFAULT_API_URL;
        }
        return URI.create(configured);
    }

    private static URI normalize(URI uri) {
        String value = uri.toString();
        return URI.create(value.endsWith("/") ? value : value + "/");
    }

    private URI resolve(String path) {
        return baseUri.resolve(path.startsWith("/") ? path.substring(1) : path);
    }
}
