package com.pfp.companion.charactersheet.control;

public record DeletedResponse(boolean deleted) {

    public static DeletedResponse success() {
        return new DeletedResponse(true);
    }
}
