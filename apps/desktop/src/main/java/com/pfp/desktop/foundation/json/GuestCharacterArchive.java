package com.pfp.desktop.foundation.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class GuestCharacterArchive {

    public static final int CHARACTER_LIMIT = 30;

    private final Path root;
    private final Path characterDirectory;
    private final Path indexPath;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
    private final List<LocalCharacterRecord> records = new ArrayList<>();

    public GuestCharacterArchive(Path root) {
        this.root = root;
        this.characterDirectory = root.resolve("guest-characters");
        this.indexPath = root.resolve("guest-index.json");
    }

    public void initialize() throws IOException {
        Files.createDirectories(characterDirectory);
        records.clear();
        if (Files.exists(indexPath)) {
            ArchiveIndex index = objectMapper.readValue(indexPath.toFile(), ArchiveIndex.class);
            records.addAll(index.characters());
            removeMissingFiles();
        } else {
            save();
        }
    }

    public List<LocalCharacterRecord> list() {
        return records.stream()
                .sorted(Comparator.comparing(LocalCharacterRecord::updatedAt).reversed())
                .toList();
    }

    public LocalCharacterRecord createCharacter(String requestedName) throws IOException {
        ensureCapacity();
        String name = normalizeName(requestedName);
        ObjectNode document = CharacterJsonDocuments.newCharacter(objectMapper, name);
        String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(document);
        return storeNewCharacter(json);
    }

    public LocalCharacterRecord importCharacter(Path source) throws IOException {
        ensureCapacity();
        String json = Files.readString(source, StandardCharsets.UTF_8);
        validateCanonicalDocument(json);
        return storeNewCharacter(json);
    }

    public void exportCharacter(String id, Path target) throws IOException {
        LocalCharacterRecord record = requireRecord(id);
        Files.copy(characterPath(record), target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void deleteCharacter(String id) throws IOException {
        LocalCharacterRecord record = requireRecord(id);
        records.removeIf(candidate -> candidate.id().equals(id));
        Files.deleteIfExists(characterPath(record));
        save();
    }

    public String readCharacterJson(String id) throws IOException {
        return Files.readString(characterPath(requireRecord(id)), StandardCharsets.UTF_8);
    }

    public LocalCharacterSheet readCharacterSheet(String id) throws IOException {
        LocalCharacterRecord record = requireRecord(id);
        String json = Files.readString(characterPath(record), StandardCharsets.UTF_8);
        JsonNode rootNode = validateCanonicalDocument(json);
        return LocalCharacterSheet.fromJson(id, rootNode);
    }

    public LocalCharacterRecord saveCharacterSheet(LocalCharacterSheet sheet) throws IOException {
        LocalCharacterRecord existing = requireRecord(sheet.id());
        String json = Files.readString(characterPath(existing), StandardCharsets.UTF_8);
        ObjectNode rootNode = (ObjectNode) validateCanonicalDocument(json);
        sheet.applyTo(rootNode);
        Files.writeString(characterPath(existing), prettyJson(rootNode), StandardCharsets.UTF_8);

        String now = Instant.now().toString();
        LocalCharacterRecord updated = sheet.toRecord(existing, now);
        records.replaceAll(record -> record.id().equals(updated.id()) ? updated : record);
        save();
        return updated;
    }

    public LocalCharacterRecord requireRecord(String id) {
        return records.stream()
                .filter(record -> record.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown character: " + id));
    }

    public Path root() {
        return root;
    }

    private LocalCharacterRecord storeNewCharacter(String json) throws IOException {
        String id = UUID.randomUUID().toString();
        String fileName = id + ".json";
        Path target = characterDirectory.resolve(fileName);
        JsonNode rootNode = validateCanonicalDocument(json);
        Files.writeString(target, prettyJson(rootNode), StandardCharsets.UTF_8);

        String now = Instant.now().toString();
        JsonNode character = rootNode.path("character");
        JsonNode info = character.path("info");
        LocalCharacterRecord record = new LocalCharacterRecord(
                id,
                textOrDefault(character, "name", "New Character"),
                textOrDefault(character, "image", ""),
                intOrDefault(info, "level", 1),
                textOrDefault(info, "class", ""),
                textOrDefault(info, "specialization", ""),
                now,
                now,
                fileName
        );
        records.add(record);
        save();
        return record;
    }

    private JsonNode validateCanonicalDocument(String json) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(json);
        if (!rootNode.path("schemaVersion").asText().equals(CharacterJsonDocuments.SCHEMA_VERSION)) {
            throw new IllegalArgumentException("Only PfP character JSON schema 1.0 is supported");
        }
        if (!rootNode.path("character").isObject()) {
            throw new IllegalArgumentException("Character JSON must contain a character object");
        }
        if (textOrDefault(rootNode.path("character"), "name", "").isBlank()) {
            throw new IllegalArgumentException("Character JSON must contain a character name");
        }
        return rootNode;
    }

    private String prettyJson(JsonNode node) throws JsonProcessingException {
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
    }

    private Path characterPath(LocalCharacterRecord record) {
        return characterDirectory.resolve(record.fileName()).normalize();
    }

    private void ensureCapacity() {
        if (records.size() >= CHARACTER_LIMIT) {
            throw new IllegalStateException("Guest archive limit reached");
        }
    }

    private void save() throws IOException {
        objectMapper.writeValue(indexPath.toFile(), new ArchiveIndex(records));
    }

    private void removeMissingFiles() throws IOException {
        records.removeIf(record -> !Files.exists(characterPath(record)));
        save();
    }

    private static String normalizeName(String requestedName) {
        if (requestedName == null || requestedName.isBlank()) {
            return "New Character";
        }
        return requestedName.trim();
    }

    private static String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return defaultValue;
        }
        return value.asText(defaultValue);
    }

    private static int intOrDefault(JsonNode node, String field, int defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || !value.isIntegralNumber()) {
            return defaultValue;
        }
        return value.intValue();
    }

    private record ArchiveIndex(List<LocalCharacterRecord> characters) {
        private ArchiveIndex {
            characters = characters == null ? List.of() : List.copyOf(characters);
        }
    }
}
