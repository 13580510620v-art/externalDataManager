package com.edm.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WebCorsConfigTest {

    private final WebCorsConfig config = new WebCorsConfig("https://admin.example.com,https://console.example.com");
    private final WebCorsConfig localhostConfig = new WebCorsConfig(
            "http://localhost:5173,http://127.0.0.1:5173"
    );

    @Test
    void allowsOnlyConfiguredOriginsWithCredentials() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/tasks");
        request.addHeader("Origin", "https://admin.example.com");

        var configuration = config.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(configuration.getAllowedOrigins())
                .containsExactlyInAnyOrder("https://admin.example.com", "https://console.example.com");
        assertThat(configuration.getAllowedMethods()).containsExactlyInAnyOrder("GET", "POST");
        assertThat(configuration.getAllowCredentials()).isTrue();
        assertThat(configuration.getAllowedHeaders()).containsExactly("*");
    }

    @Test
    void rejectsUnconfiguredOrigin() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/tasks");
        request.addHeader("Origin", "https://evil.example.com");

        assertThat(config.corsConfigurationSource().getCorsConfiguration(request).checkOrigin("https://evil.example.com"))
                .isNull();
    }

    @Test
    void allowsLocalHttpOrigins() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/tasks");

        var source = localhostConfig.corsConfigurationSource();
        assertThat(source.getCorsConfiguration(request).checkOrigin("http://localhost:5173"))
                .isEqualTo("http://localhost:5173");
        assertThat(source.getCorsConfiguration(request).checkOrigin("http://127.0.0.1:5173"))
                .isEqualTo("http://127.0.0.1:5173");
    }
}
