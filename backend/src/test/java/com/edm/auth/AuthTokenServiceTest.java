package com.edm.auth;

import com.edm.security.AuthTokenService;
import com.edm.security.LoginUser;
import com.edm.security.TokenSession;
import com.edm.security.TokenSessionStore;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class AuthTokenServiceTest {

    private final Map<String, LoginUser> sessions = new HashMap<>();
    private final TokenSessionStore store = new TokenSessionStore() {
        @Override
        public void save(TokenSession session, LoginUser user) {
            sessions.put(session.tokenHash(), user);
        }

        @Override
        public Optional<LoginUser> find(String tokenHash) {
            return Optional.ofNullable(sessions.get(tokenHash));
        }

        @Override
        public void delete(String tokenHash) {
            sessions.remove(tokenHash);
        }
    };
    private final Instant now = Instant.parse("2026-09-03T03:00:00Z");
    private final AuthTokenService service = new AuthTokenService(
            store,
            Duration.ofHours(8),
            Clock.fixed(now, ZoneOffset.UTC)
    );

    @Test
    void storesHashInsteadOfPlaintextToken() {
        LoginUser user = new LoginUser(1L, "admin", "系统管理员", true, Set.of("dashboard:read"));

        TokenSession session = service.create(user);

        assertThat(session.token().getBytes()).hasSizeGreaterThanOrEqualTo(32);
        assertThat(session.tokenHash()).isNotEqualTo(session.token());
        assertThat(sessions).doesNotContainKey(session.token());
        assertThat(sessions).containsKey(session.tokenHash());
        assertThat(session.expiresAt()).isEqualTo(now.plus(Duration.ofHours(8)));
        assertThat(service.resolve(session.token())).contains(user);
    }

    @Test
    void logoutRemovesSession() {
        LoginUser user = new LoginUser(1L, "admin", "系统管理员", true, Set.of("dashboard:read"));
        TokenSession session = service.create(user);

        service.logout(session.token());

        assertThat(service.resolve(session.token())).isEmpty();
    }
}
