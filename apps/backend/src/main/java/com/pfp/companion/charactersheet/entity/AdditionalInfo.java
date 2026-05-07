package com.pfp.companion.charactersheet.entity;

public record AdditionalInfo(String appearance, String detailedOrigin, String allies,
        String notesPrimary, String notesSecondary) {

    public AdditionalInfo {
        appearance = normalized(appearance);
        detailedOrigin = normalized(detailedOrigin);
        allies = normalized(allies);
        notesPrimary = normalized(notesPrimary);
        notesSecondary = normalized(notesSecondary);
    }

    public static AdditionalInfo empty() {
        return new AdditionalInfo("", "", "", "", "");
    }

    private static String normalized(String value) {
        return value == null ? "" : value;
    }
}

