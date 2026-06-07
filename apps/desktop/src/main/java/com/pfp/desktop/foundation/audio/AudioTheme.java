package com.pfp.desktop.foundation.audio;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum AudioTheme {
    NONE("None", ""),
    MINSTREL_DANCE("Minstrel Dance", "/com/pfp/desktop/audio/music/minstrel_dance.mp3"),
    VAMPIRES_PIANO("Vampires Piano", "/com/pfp/desktop/audio/music/vampires_piano.mp3"),
    DARK_BISHOP("Dark Bishop", "/com/pfp/desktop/audio/music/dark_bishop.mp3");

    private final String displayName;
    private final String resourcePath;

    AudioTheme(String displayName, String resourcePath) {
        this.displayName = displayName;
        this.resourcePath = resourcePath;
    }

    public String displayName() {
        return displayName;
    }

    public String resourcePath() {
        return resourcePath;
    }

    public boolean hasTrack() {
        return !resourcePath.isBlank();
    }

    public static List<String> displayNames() {
        return Arrays.stream(values()).map(AudioTheme::displayName).toList();
    }

    public static AudioTheme fromDisplayName(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        Optional<AudioTheme> exact = Arrays.stream(values())
                .filter(theme -> theme.displayName.equalsIgnoreCase(value.trim()))
                .findFirst();
        return exact.orElse(NONE);
    }
}
