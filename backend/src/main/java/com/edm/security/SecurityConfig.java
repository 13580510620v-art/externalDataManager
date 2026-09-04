package com.edm.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.saml2.Saml2LoginConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.saml2.provider.service.registration.InMemoryRelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistration;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrationRepository;
import org.springframework.security.saml2.provider.service.registration.RelyingPartyRegistrations;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfFilter;

import java.time.Clock;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Conditional(NonSamlCondition.class)
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthTokenService authTokenService
    ) throws Exception {
        return commonFilterChain(http, authTokenService, false).build();
    }

    @Bean
    @Conditional(SamlEnabledCondition.class)
    public SecurityFilterChain samlSecurityFilterChain(
            HttpSecurity http,
            AuthTokenService authTokenService,
            SamlLoginSuccessHandler samlLoginSuccessHandler
    ) throws Exception {
        commonFilterChain(http, authTokenService, true)
                .saml2Login(saml -> saml
                        .loginProcessingUrl("/login/saml2/sso/{registrationId}")
                        .successHandler(samlLoginSuccessHandler));
        return http.build();
    }

    @Bean
    public SamlLoginSuccessHandler samlLoginSuccessHandler(
            SamlUserService samlUserService,
            AuthTokenService authTokenService,
            @Value("${edm.cookie.same-site:LAX}") String cookieSameSite,
            @Value("${edm.cookie.secure:false}") boolean cookieSecure,
            @Value("${edm.saml.success-url:http://localhost:5173/dashboard}") String successUrl,
            @Value("${edm.saml.failure-url:http://localhost:5173/login?samlError=1}") String failureUrl
    ) {
        return new SamlLoginSuccessHandler(
                samlUserService,
                authTokenService,
                cookieSameSite,
                cookieSecure,
                successUrl,
                failureUrl
        );
    }

    @Bean
    @Conditional(SamlEnabledCondition.class)
    public RelyingPartyRegistrationRepository relyingPartyRegistrationRepository(
            @Value("${edm.saml.entity-id}") String entityId,
            @Value("${edm.saml.idp-metadata-url}") String metadataUrl,
            @Value("${edm.saml.base-url}") String baseUrl
    ) {
        RelyingPartyRegistration registration = RelyingPartyRegistrations
                .fromMetadataLocation(metadataUrl)
                .registrationId("edm")
                .entityId(entityId)
                .assertionConsumerServiceLocation(baseUrl + "/login/saml2/sso/edm")
                .build();
        return new InMemoryRelyingPartyRegistrationRepository(registration);
    }

    private HttpSecurity commonFilterChain(
            HttpSecurity http,
            AuthTokenService authTokenService,
            boolean samlEnabled
    ) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        CsrfTokenRequestAttributeHandler csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfTokenRequestHandler.setCsrfRequestAttributeName(null);
        HttpSecurity configured = http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfTokenRepository)
                        .csrfTokenRequestHandler(csrfTokenRequestHandler))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/saml/enabled",
                                "/login/saml2/**",
                                "/saml2/**",
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(
                        new AuthTokenAuthenticationFilter(authTokenService),
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(new CsrfCookieFilter(), CsrfFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);
        return configured;
    }
}
