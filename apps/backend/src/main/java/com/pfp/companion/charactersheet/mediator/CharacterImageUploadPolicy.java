package com.pfp.companion.charactersheet.mediator;

import java.util.Locale;

public final class CharacterImageUploadPolicy {

    public static final long MAX_UPLOAD_BYTES = 2L * 1024 * 1024;

    public void validate(String filename, String contentType, long sizeBytes) {
        if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".jpg")) {
            throw new IllegalArgumentException("character image must use .jpg extension");
        }
        if (!"image/jpeg".equalsIgnoreCase(contentType)) {
            throw new IllegalArgumentException("character image must use image/jpeg content type");
        }
        if (sizeBytes < 0 || sizeBytes > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException("character image must not exceed 2 MB");
        }
    }
}

