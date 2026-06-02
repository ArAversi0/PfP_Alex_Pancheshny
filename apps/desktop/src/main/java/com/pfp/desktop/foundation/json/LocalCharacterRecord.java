package com.pfp.desktop.foundation.json;

public record LocalCharacterRecord(
        String id,
        String name,
        String image,
        int level,
        String className,
        String specialization,
        String createdAt,
        String updatedAt,
        String fileName
) {
    public String classLine() {
        if (!className.isBlank() && !specialization.isBlank()) {
            return className + " / " + specialization;
        }
        if (!className.isBlank()) {
            return className;
        }
        if (!specialization.isBlank()) {
            return specialization;
        }
        return "Unwritten class";
    }
}
