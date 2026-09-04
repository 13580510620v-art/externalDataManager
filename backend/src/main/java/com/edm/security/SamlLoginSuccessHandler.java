package com.edm.security;

import com.edm.exception.BusinessException;
import com.edm.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

public class SamlLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String TOKEN_COOKIE = "EDM_TOKEN";

    private final SamlUserService samlUserService;
    private final AuthTokenService authTokenService;
    private final String cookieSameSite;
    private final boolean cookieSecure;
    private final String failureUrl;

    public SamlLoginSuccessHandler(
            SamlUserService samlUserService,
            AuthTokenService authTokenService,
            String cookieSameSite,
            boolean cookieSecure,
            String successUrl,
            String failureUrl
    ) {
        super(successUrl);
        this.samlUserService = samlUserService;
        this.authTokenService = authTokenService;
        this.cookieSameSite = cookieSameSite;
        this.cookieSecure = cookieSecure;
        this.failureUrl = failureUrl;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        LoginUser user = samlUserService.findByNameId(authentication.getName())
                .orElseThrow(() -> new BusinessException(ErrorCode.SAML_USER_NOT_FOUND));
        if (!user.enabled()) {
            getRedirectStrategy().sendRedirect(request, response, failureUrl);
            return;
        }
        TokenSession session = authTokenService.create(user);
        Duration maxAge = Duration.between(Instant.now(), session.expiresAt());
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(TOKEN_COOKIE, session.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build()
                .toString());
        getRedirectStrategy().sendRedirect(request, response, determineTargetUrl(request, response));
    }
}
