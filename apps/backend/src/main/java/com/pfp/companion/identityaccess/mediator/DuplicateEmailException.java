package com.pfp.companion.identityaccess.mediator;

public final class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("email is already registered");
    }
}
