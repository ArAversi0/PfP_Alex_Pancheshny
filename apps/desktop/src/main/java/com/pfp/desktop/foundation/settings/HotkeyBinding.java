package com.pfp.desktop.foundation.settings;

public record HotkeyBinding(String actionId, String label, String combination) {
    public HotkeyBinding {
        actionId = text(actionId);
        label = text(label);
        combination = text(combination);
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }
}
