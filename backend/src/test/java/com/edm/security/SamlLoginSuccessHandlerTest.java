package com.edm.security;

import com.edm.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.time.Instant;
import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SamlLoginSuccessHandlerTest {

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
    private final AuthTokenService tokenService = new AuthTokenService(
            store,
            Duration.ofHours(8),
            Clock.systemUTC()
    );
    private final LoginUser user = new LoginUser(1L, "saml-user", "SAML 用户", true, Set.of("dashboard:read"));
    private final SamlLoginSuccessHandler handler = new SamlLoginSuccessHandler(
            nameId -> "saml-user".equals(nameId) ? Optional.of(user) : Optional.empty(),
            tokenService,
            "LAX",
            false,
            "http://localhost:5173/dashboard",
            "http://localhost:5173/login?samlError=1"
    );
    private final SamlLoginSuccessHandler secureCookieHandler = new SamlLoginSuccessHandler(
            nameId -> "saml-user".equals(nameId) ? Optional.of(user) : Optional.empty(),
            tokenService,
            "LAX",
            true,
            "http://localhost:5173/dashboard",
            "http://localhost:5173/login?samlError=1"
    );

    @Test
    void issuesUnifiedTokenCookieAndRedirectsToFrontend() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken("saml-user", "credentials");

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/dashboard");
        assertThat(response.getHeader("Set-Cookie"))
                .contains("EDM_TOKEN=")
                .contains("HttpOnly")
                .containsIgnoringCase("SameSite=Lax");
        assertThat(sessions).hasSize(1);
    }

    @Test
    void issuesSecureTokenCookieWhenHttpsCookieModeIsEnabled() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken("saml-user", "credentials");

        secureCookieHandler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getHeader("Set-Cookie"))
                .contains("EDM_TOKEN=")
                .contains("HttpOnly")
                .contains("Secure")
                .containsIgnoringCase("SameSite=Lax");
    }

    @Test
    void rejectsUnknownSamlUserWithoutToken() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        Authentication authentication = new UsernamePasswordAuthenticationToken("missing-user", "credentials");

        assertThatThrownBy(() -> handler.onAuthenticationSuccess(request, response, authentication))
                .isInstanceOf(BusinessException.class);
        assertThat(sessions).isEmpty();
    }
}
