package com.edm.security;

import java.time.Instant;

public record TokenSession(String token, String tokenHash, Instant expiresAt) {
}
