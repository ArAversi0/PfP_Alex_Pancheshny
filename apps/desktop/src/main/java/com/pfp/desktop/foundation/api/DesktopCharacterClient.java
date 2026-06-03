package com.pfp.desktop.foundation.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfp.desktop.foundation.json.LocalCharacterSheet;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class DesktopCharacterClient {
    private final URI baseUri;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DesktopCharacterClient(URI baseUri) {
        this(baseUri, HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build(), new ObjectMapper());
    }

    DesktopCharacterClient(URI baseUri, HttpClient httpClient, ObjectMapper objectMapper) {
        this.baseUri = normalize(baseUri);
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
    }

    public List<AccountCharacterCard> list(AuthSession session) throws AuthException {
        HttpRequest request = authorized("/v1/characters", session)
                .GET()
                .build();
        return send(request, new TypeReference<>() {
        });
    }

    public AccountCharacterCreated create(AuthSession session, String name) throws AuthException {
        try {
            HttpRequest request = authorized("/v1/characters", session)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of("name", name))))
                    .build();
            return send(request, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new AuthException("Could not prepare the request.", exception);
        }
    }

    public void delete(AuthSession session, UUID characterId) throws AuthException {
        HttpRequest request = authorized("/v1/characters/" + characterId, session)
                .DELETE()
                .build();
        send(request, new TypeReference<JsonNode>() {
        });
    }

    public AccountCharacterCreated importJson(AuthSession session, String json) throws AuthException {
        HttpRequest request = authorized("/v1/characters/import", session)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        return send(request, new TypeReference<>() {
        });
    }

    public String exportJson(AuthSession session, UUID characterId) throws AuthException {
        HttpRequest request = authorized("/v1/characters/" + characterId + "/export", session)
                .GET()
                .build();
        return sendText(request);
    }

    public AccountCharacterSheet getSheet(AuthSession session, UUID characterId) throws AuthException {
        HttpRequest request = authorized("/v1/characters/" + characterId + "/sheet", session)
                .GET()
                .build();
        return send(request, new TypeReference<>() {
        });
    }

    public void saveSheetBasics(AuthSession session, LocalCharacterSheet sheet) throws AuthException {
        saveSheetChanges(session, null, sheet);
    }

    public void saveSheetChanges(
            AuthSession session,
            LocalCharacterSheet original,
            LocalCharacterSheet sheet
    ) throws AuthException {
        UUID characterId = UUID.fromString(sheet.id());
        boolean saveAll = original == null;
        if (saveAll || !Objects.equals(original.name(), sheet.name()) || !Objects.equals(original.info(), sheet.info())) {
            put(session, "/v1/characters/" + characterId + "/info", Map.of(
                    "name", sheet.name(),
                    "level", sheet.info().level(),
                    "origin", sheet.info().origin(),
                    "background", sheet.info().background(),
                    "className", sheet.info().className(),
                    "specialization", sheet.info().specialization()
            ));
        }
        if (saveAll || !Objects.equals(original.image(), sheet.image())) {
            put(session, "/v1/characters/" + characterId + "/portrait", Map.of("imageUrl", sheet.image()));
        }
        if (saveAll || !Objects.equals(original.stats(), sheet.stats())) {
            put(session, "/v1/characters/" + characterId + "/stats", Map.of(
                    "strength", sheet.stats().strength(),
                    "dexterity", sheet.stats().dexterity(),
                    "stamina", sheet.stats().stamina(),
                    "intelligence", sheet.stats().intelligence(),
                    "charisma", sheet.stats().charisma(),
                    "luck", sheet.stats().luck(),
                    "mind", sheet.stats().mind()
            ));
        }
        if (saveAll || !Objects.equals(original.skills(), sheet.skills())) {
            put(session, "/v1/characters/" + characterId + "/skills", sheet.skills().stream()
                    .map(skill -> Map.of(
                            "statGroup", skill.stat(),
                            "skillName", skill.name(),
                            "level", skill.level()
                    ))
                    .toList());
        }
        if (saveAll || !Objects.equals(original.condition(), sheet.condition())) {
            put(session, "/v1/characters/" + characterId + "/condition", Map.of(
                    "passiveDefense", sheet.condition().passiveDefense(),
                    "movementSpeed", number(sheet.condition().movementSpeed()),
                    "maxCarryWeight", number(sheet.condition().maxCarryWeight()),
                    "hp", Map.of(
                            "head", bodyPart(sheet.condition().hp().head()),
                            "neck", bodyPart(sheet.condition().hp().neck()),
                            "torso", bodyPart(sheet.condition().hp().torso()),
                            "leftArm", bodyPart(sheet.condition().hp().leftArm()),
                            "rightArm", bodyPart(sheet.condition().hp().rightArm()),
                            "leftLeg", bodyPart(sheet.condition().hp().leftLeg()),
                            "rightLeg", bodyPart(sheet.condition().hp().rightLeg())
                    )
            ));
        }
        if (saveAll || !Objects.equals(original.blessings(), sheet.blessings())) {
            put(session, "/v1/characters/" + characterId + "/blessings", Map.of(
                    "blessings", sheet.blessings().blessings(),
                    "inspirations", sheet.blessings().inspirations()
            ));
        }
        if (saveAll || !Objects.equals(original.additionalInfo(), sheet.additionalInfo())) {
            put(session, "/v1/characters/" + characterId + "/additional-info", Map.of(
                    "appearance", sheet.additionalInfo().appearance(),
                    "detailedOrigin", sheet.additionalInfo().detailedOrigin(),
                    "allies", sheet.additionalInfo().allies(),
                    "notesPrimary", sheet.additionalInfo().notesPrimary(),
                    "notesSecondary", sheet.additionalInfo().notesSecondary()
            ));
        }
        if (saveAll || original.money().amountBase().compareTo(sheet.money().amountBase()) != 0) {
            put(session, "/v1/characters/" + characterId + "/money", Map.of("amountBase", number(sheet.money().amountBase())));
        }
        if (saveAll || !Objects.equals(original.money().displayCurrency(), sheet.money().displayCurrency())) {
            post(session, "/v1/characters/" + characterId + "/money/currency", Map.of("displayCurrency", sheet.money().displayCurrency()));
        }
    }

    public AccountCharacterSheet addInventoryRow(AuthSession session, UUID characterId) throws AuthException {
        post(session, "/v1/characters/" + characterId + "/inventory/rows", Map.of("rowsToAdd", 1));
        return getSheet(session, characterId);
    }

    public AccountCharacterSheet removeInventoryRow(AuthSession session, UUID characterId) throws AuthException {
        HttpRequest request = authorized("/v1/characters/" + characterId + "/inventory/rows", session)
                .DELETE()
                .build();
        send(request, new TypeReference<JsonNode>() {
        });
        return getSheet(session, characterId);
    }

    public AccountCharacterSheet moveInventoryItem(
            AuthSession session,
            UUID characterId,
            int fromSlotIndex,
            int toSlotIndex
    ) throws AuthException {
        post(session, "/v1/characters/" + characterId + "/inventory/slots/move", Map.of(
                "fromSlotIndex", fromSlotIndex,
                "toSlotIndex", toSlotIndex
        ));
        return getSheet(session, characterId);
    }

    public AccountCharacterSheet createItem(
            AuthSession session,
            UUID characterId,
            LocalCharacterSheet.InventoryItem item
    ) throws AuthException {
        return postForSheet(session, "/v1/characters/" + characterId + "/inventory/items", itemPayload(item));
    }

    public AccountCharacterSheet updateItem(
            AuthSession session,
            UUID characterId,
            LocalCharacterSheet.InventoryItem item
    ) throws AuthException {
        return putForSheet(session, "/v1/characters/" + characterId + "/inventory/items/" + UUID.fromString(item.id()),
                itemPayload(item));
    }

    public AccountCharacterSheet throwAwayItem(AuthSession session, UUID characterId, String itemId) throws AuthException {
        return deleteForSheet(session, "/v1/characters/" + characterId + "/inventory/items/" + UUID.fromString(itemId));
    }

    public AccountCharacterSheet sellTradeItem(AuthSession session, UUID characterId, String itemId) throws AuthException {
        return postForSheet(session, "/v1/characters/" + characterId + "/inventory/items/" + UUID.fromString(itemId) + "/sell",
                Map.of());
    }

    public AccountCharacterSheet equipItem(
            AuthSession session,
            UUID characterId,
            String itemId,
            String slotCode
    ) throws AuthException {
        return postForSheet(session, "/v1/characters/" + characterId + "/equipment/equip", Map.of(
                "itemId", UUID.fromString(itemId),
                "slotCode", slotCode
        ));
    }

    public AccountCharacterSheet unequipItem(AuthSession session, UUID characterId, String slotCode) throws AuthException {
        return postForSheet(session, "/v1/characters/" + characterId + "/equipment/unequip", Map.of("slotCode", slotCode));
    }

    public AccountCharacterSheet createSpell(
            AuthSession session,
            UUID characterId,
            LocalCharacterSheet.SpellPreview spell
    ) throws AuthException {
        return postForSheet(session, "/v1/characters/" + characterId + "/spells", spellPayload(spell));
    }

    public AccountCharacterSheet updateSpell(
            AuthSession session,
            UUID characterId,
            LocalCharacterSheet.SpellPreview spell
    ) throws AuthException {
        return putForSheet(session, "/v1/characters/" + characterId + "/spells/" + UUID.fromString(spell.id()),
                spellPayload(spell));
    }

    public AccountCharacterSheet deleteSpell(AuthSession session, UUID characterId, String spellId) throws AuthException {
        return deleteForSheet(session, "/v1/characters/" + characterId + "/spells/" + UUID.fromString(spellId));
    }

    private void put(AuthSession session, String path, Object payload) throws AuthException {
        sendWithBody("PUT", session, path, payload);
    }

    private void post(AuthSession session, String path, Object payload) throws AuthException {
        sendWithBody("POST", session, path, payload);
    }

    private AccountCharacterSheet postForSheet(AuthSession session, String path, Object payload) throws AuthException {
        return sendForSheet("POST", session, path, payload);
    }

    private AccountCharacterSheet putForSheet(AuthSession session, String path, Object payload) throws AuthException {
        return sendForSheet("PUT", session, path, payload);
    }

    private AccountCharacterSheet deleteForSheet(AuthSession session, String path) throws AuthException {
        HttpRequest request = authorized(path, session)
                .DELETE()
                .build();
        return send(request, new TypeReference<>() {
        });
    }

    private AccountCharacterSheet sendForSheet(
            String method,
            AuthSession session,
            String path,
            Object payload
    ) throws AuthException {
        try {
            HttpRequest request = authorized(path, session)
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            return send(request, new TypeReference<>() {
            });
        } catch (IOException exception) {
            throw new AuthException("Could not prepare the request.", exception);
        }
    }

    private static Map<String, Object> itemPayload(LocalCharacterSheet.InventoryItem item) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", item.type());
        payload.put("title", item.title());
        payload.put("imageUrl", item.image());
        payload.put("weight", number(item.weight()));
        payload.put("description", item.description());
        payload.put("equipmentType", "EQUIPMENT".equals(item.type()) ? item.equipmentType() : null);
        payload.put("sellPriceBase", "TRADE".equals(item.type()) ? number(item.sellPriceBase()) : null);
        return payload;
    }

    private static Map<String, Object> spellPayload(LocalCharacterSheet.SpellPreview spell) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", spell.name());
        payload.put("type", spell.type());
        payload.put("spellClass", spell.spellClass());
        payload.put("imageUrl", spell.image());
        payload.put("requirements", spell.requirements());
        payload.put("description", spell.description());
        return payload;
    }

    private void sendWithBody(String method, AuthSession session, String path, Object payload) throws AuthException {
        try {
            HttpRequest request = authorized(path, session)
                    .header("Content-Type", "application/json")
                    .method(method, HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload)))
                    .build();
            send(request, new TypeReference<JsonNode>() {
            });
        } catch (IOException exception) {
            throw new AuthException("Could not prepare the request.", exception);
        }
    }

    private static Map<String, Integer> bodyPart(LocalCharacterSheet.BodyPartHealth part) {
        return Map.of("max", part.max(), "current", part.current());
    }

    private static BigDecimal number(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private HttpRequest.Builder authorized(String path, AuthSession session) {
        return HttpRequest.newBuilder(resolve(path))
                .timeout(Duration.ofSeconds(12))
                .header("Accept", "application/json")
                .header("Authorization", session.authorizationHeader());
    }

    private <T> T send(HttpRequest request, TypeReference<T> responseType) throws AuthException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
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

    private String sendText(HttpRequest request) throws AuthException {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return response.body();
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
                return "Your session has expired. Please sign in again.";
            }
            if (status == 403) {
                return "You do not have permission to perform this action.";
            }
            return "The server could not process the request (" + status + ").";
        }
        String normalized = stripDiagnostics(message);
        return switch (normalized.toLowerCase()) {
            case "authentication is required" -> "Your session has expired. Please sign in again.";
            case "character limit reached" -> "Character limit reached.";
            default -> normalized;
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

    private static URI normalize(URI uri) {
        String value = uri.toString();
        return URI.create(value.endsWith("/") ? value : value + "/");
    }

    private URI resolve(String path) {
        return baseUri.resolve(path.startsWith("/") ? path.substring(1) : path);
    }
}
