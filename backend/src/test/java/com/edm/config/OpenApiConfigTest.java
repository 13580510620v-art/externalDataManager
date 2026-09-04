package com.edm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiConfigTest {

    private final OpenApiConfig config = new OpenApiConfig();

    @Test
    void buildsOpenApi3WithCookieAndCsrfSchemes() {
        OpenAPI openAPI = config.edmOpenApi();
        Components components = openAPI.getComponents();
        SecurityScheme cookie = components.getSecuritySchemes().get("CookieAuth");
        SecurityScheme csrf = components.getSecuritySchemes().get("CsrfHeader");

        assertThat(openAPI.getOpenapi()).startsWith("3.");
        assertThat(openAPI.getInfo().getTitle()).isEqualTo("外部数据管理平台 Web 管理端 API");
        assertThat(cookie.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(cookie.getIn()).isEqualTo(SecurityScheme.In.COOKIE);
        assertThat(cookie.getName()).isEqualTo("EDM_TOKEN");
        assertThat(csrf.getType()).isEqualTo(SecurityScheme.Type.APIKEY);
        assertThat(csrf.getIn()).isEqualTo(SecurityScheme.In.HEADER);
        assertThat(csrf.getName()).isEqualTo("X-XSRF-TOKEN");
        assertThat(openAPI.getSecurity()).hasSize(1);
        assertThat(openAPI.getSecurity().get(0).keySet())
                .containsExactlyInAnyOrder("CookieAuth", "CsrfHeader");
    }

    @Test
    void createsManagementApiGroup() {
        assertThat(config.managementApiGroup().getGroup()).isEqualTo("management");
    }
}
