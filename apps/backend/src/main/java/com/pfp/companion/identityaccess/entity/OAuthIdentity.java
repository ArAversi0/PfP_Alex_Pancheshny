package com.pfp.companion.identityaccess.entity;

public record OAuthIdentity(String provider, String providerSubject) {

    public OAuthIdentity {
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider must not be blank");
        }
        if (providerSubject == null || providerSubject.isBlank()) {
            throw new IllegalArgumentException("providerSubject must not be blank");
        }
    }
}

