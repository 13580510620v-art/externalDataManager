package com.edm.auth;

import com.edm.auth.dto.CurrentUserResponse;
import com.edm.auth.dto.LoginRequest;
import com.edm.common.ApiResponse;
import com.edm.security.AuthTokenService;
import com.edm.security.LoginUser;
import com.edm.security.TokenSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.time.Instant;

@Tag(name = "认证", description = "用户名密码登录、SAML 状态和当前用户")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String TOKEN_COOKIE = "EDM_TOKEN";

    private final AuthService authService;
    private final AuthTokenService authTokenService;
    private final String cookieSameSite;
    private final boolean cookieSecure;
    private final boolean samlEnabled;

    public AuthController(
            AuthService authService,
            AuthTokenService authTokenService,
            @Value("${edm.cookie.same-site:LAX}") String cookieSameSite,
            @Value("${edm.cookie.secure:false}") boolean cookieSecure,
            @Value("${edm.saml.enabled:false}") boolean samlEnabled
    ) {
        this.authService = authService;
        this.authTokenService = authTokenService;
        this.cookieSameSite = cookieSameSite;
        this.cookieSecure = cookieSecure;
        this.samlEnabled = samlEnabled;
    }

    @Operation(summary = "用户名密码登录")
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<CurrentUserResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginUser user = authService.login(request);
        TokenSession session = authTokenService.create(user);
        CurrentUserResponse response = new CurrentUserResponse(
                user.id(),
                user.username(),
                user.fullName(),
                user.permissions()
        );
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, loginCookie(session).toString())
                .body(ApiResponse.success(response));
    }

    @Operation(summary = "查询 SAML 登录是否启用")
    @GetMapping("/saml/enabled")
    public ApiResponse<Boolean> samlEnabled() {
        return ApiResponse.success(samlEnabled);
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = TOKEN_COOKIE, required = false) String token
    ) {
        authTokenService.logout(token);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, ResponseCookie.from(TOKEN_COOKIE, "")
                        .httpOnly(true)
                        .secure(cookieSecure)
                        .sameSite(cookieSameSite)
                        .path("/")
                        .maxAge(0)
                        .build()
                        .toString())
                .body(ApiResponse.success());
    }

    @Operation(summary = "查询当前用户和权限")
    @GetMapping("/me")
    public ApiResponse<CurrentUserResponse> me() {
        return ApiResponse.success(authService.currentUser());
    }

    private ResponseCookie loginCookie(TokenSession session) {
        Duration maxAge = Duration.between(Instant.now(), session.expiresAt());
        return ResponseCookie.from(TOKEN_COOKIE, session.token())
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
