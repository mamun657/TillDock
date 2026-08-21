package com.tilldock.auth.security;

import java.security.Principal;
import java.util.UUID;

public record AuthenticatedMerchant(UUID id, String role) implements Principal {
    @Override
    public String getName() {
        return id.toString();
    }
}