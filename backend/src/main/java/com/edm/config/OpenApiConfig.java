package com.edm.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI edmOpenApi() {
        return new OpenAPI()
                .openapi("3.0.1")
                .info(new Info()
                        .title("外部数据管理平台 Web 管理端 API")
                        .description("外部数据管理平台认证、供应商、任务、Dashboard 和系统管理接口")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes("CookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("EDM_TOKEN"))
                        .addSecuritySchemes("CsrfHeader", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .name("X-XSRF-TOKEN")))
                .security(List.of(new SecurityRequirement()
                        .addList("CookieAuth")
                        .addList("CsrfHeader")));
    }

    @Bean
    public GroupedOpenApi managementApiGroup() {
        return GroupedOpenApi.builder()
                .group("management")
                .pathsToMatch("/api/**")
                .build();
    }
}
