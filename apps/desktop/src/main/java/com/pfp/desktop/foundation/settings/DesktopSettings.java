package com.pfp.desktop.foundation.settings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class DesktopSettings {
    private int windowWidth = 1100;
    private int windowHeight = 720;
    private boolean fullscreen;
    private String musicTheme = "None";
    private boolean musicEnabled = false;
    private boolean soundEnabled = true;
    private int musicVolume = 50;
    private int soundVolume = 70;
    private List<HotkeyBinding> hotkeys = defaultHotkeys();

    public static DesktopSettings defaults() {
        return new DesktopSettings();
    }

    public static List<HotkeyBinding> defaultHotkeys() {
        return new ArrayList<>(List.of(
                new HotkeyBinding("close-dialog", "Close dialog / cancel", "Esc"),
                new HotkeyBinding("fullscreen", "Toggle fullscreen", "F11"),
                new HotkeyBinding("settings", "Open Settings", "Ctrl+,"),
                new HotkeyBinding("exit", "Exit application", "Ctrl+Q"),
                new HotkeyBinding("new-character", "Create character", "Ctrl+N"),
                new HotkeyBinding("import-json", "Import JSON", "Ctrl+I"),
                new HotkeyBinding("edit-sheet", "Toggle sheet editing", "Ctrl+E"),
                new HotkeyBinding("export-open-character", "Export opened character", "Ctrl+Shift+E"),
                new HotkeyBinding("archive", "Return to character archive", "Ctrl+A"),
                new HotkeyBinding("sheet-character-info", "Sheet: Character Info", "Ctrl+1"),
                new HotkeyBinding("sheet-condition", "Sheet: Condition", "Ctrl+2"),
                new HotkeyBinding("sheet-inventory", "Sheet: Inventory and Spells", "Ctrl+3"),
                new HotkeyBinding("sheet-additional-info", "Sheet: Additional Info", "Ctrl+4"),
                new HotkeyBinding("add-item", "Add inventory item", "Ctrl+Shift+N"),
                new HotkeyBinding("equip-active-item", "Equip selected equipment", "Ctrl+Shift+Q"),
                new HotkeyBinding("sell-active-item", "Sell selected item", "Ctrl+Shift+S"),
                new HotkeyBinding("add-spell", "Add spell", "Ctrl+Shift+D"),
                new HotkeyBinding("lore", "Open Lore", "Ctrl+L"),
                new HotkeyBinding("rule-book", "Open Rule Book", "Ctrl+R")
        ));
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public void setWindowWidth(int windowWidth) {
        this.windowWidth = windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public void setWindowHeight(int windowHeight) {
        this.windowHeight = windowHeight;
    }

    public boolean isFullscreen() {
        return fullscreen;
    }

    public void setFullscreen(boolean fullscreen) {
        this.fullscreen = fullscreen;
    }

    public String getMusicTheme() {
        return musicTheme;
    }

    public void setMusicTheme(String musicTheme) {
        this.musicTheme = musicTheme;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setMusicEnabled(boolean musicEnabled) {
        this.musicEnabled = musicEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public int getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(int musicVolume) {
        this.musicVolume = clampPercent(musicVolume);
    }

    public int getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(int soundVolume) {
        this.soundVolume = clampPercent(soundVolume);
    }

    public List<HotkeyBinding> getHotkeys() {
        if (hotkeys == null || hotkeys.isEmpty()) {
            hotkeys = defaultHotkeys();
        }
        return hotkeys.stream()
                .filter(binding -> !"back".equals(binding.actionId()))
                .filter(binding -> !"open-active-item".equals(binding.actionId()))
                .filter(binding -> !"open-active-spell".equals(binding.actionId()))
                .toList();
    }

    public void setHotkeys(List<HotkeyBinding> hotkeys) {
        this.hotkeys = hotkeys == null || hotkeys.isEmpty() ? defaultHotkeys() : new ArrayList<>(hotkeys);
    }

    public Optional<String> actionFor(String combination) {
        String normalized = text(combination);
        return getHotkeys().stream()
                .filter(binding -> binding.combination().equalsIgnoreCase(normalized))
                .map(HotkeyBinding::actionId)
                .findFirst();
    }

    private static int clampPercent(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }
}
