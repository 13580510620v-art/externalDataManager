package com.edm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AuthTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String TOKEN_COOKIE = "EDM_TOKEN";
    private static final String PERMISSION_PREFIX = "PERM_";

    private final AuthTokenService authTokenService;

    public AuthTokenAuthenticationFilter(AuthTokenService authTokenService) {
        this.authTokenService = authTokenService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Optional.ofNullable(findToken(request))
                    .flatMap(authTokenService::resolve)
                    .filter(LoginUser::enabled)
                    .ifPresent(user -> {
                        List<SimpleGrantedAuthority> authorities = user.permissions().stream()
                                .map(permission -> new SimpleGrantedAuthority(PERMISSION_PREFIX + permission))
                                .toList();
                        UsernamePasswordAuthenticationToken authentication =
                                UsernamePasswordAuthenticationToken.authenticated(user, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
        }
        filterChain.doFilter(request, response);
    }

    private String findToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
