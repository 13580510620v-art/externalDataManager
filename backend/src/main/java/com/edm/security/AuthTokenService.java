package com.edm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class AuthTokenService {

    private static final int TOKEN_BYTE_SIZE = 32;
    private static final String TOKEN_HASH_ALGORITHM = "SHA-256";

    private final TokenSessionStore tokenSessionStore;
    private final Duration tokenTtl;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthTokenService(
            TokenSessionStore tokenSessionStore,
            @Value("${edm.auth.token-ttl:8h}") Duration tokenTtl,
            Clock clock
    ) {
        this.tokenSessionStore = tokenSessionStore;
        this.tokenTtl = tokenTtl;
        this.clock = clock;
    }

    public TokenSession create(LoginUser user) {
        byte[] tokenBytes = new byte[TOKEN_BYTE_SIZE];
        secureRandom.nextBytes(tokenBytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        Instant expiresAt = clock.instant().plus(tokenTtl);
        TokenSession session = new TokenSession(token, sha256Hex(token), expiresAt);
        tokenSessionStore.save(session, user);
        return session;
    }

    public Optional<LoginUser> resolve(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        return tokenSessionStore.find(sha256Hex(token));
    }

    public void logout(String token) {
        if (token == null || token.isBlank()) {
            return;
        }
        tokenSessionStore.delete(sha256Hex(token));
    }

    private String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance(TOKEN_HASH_ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(token.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 算法不可用", exception);
        }
    }
}
