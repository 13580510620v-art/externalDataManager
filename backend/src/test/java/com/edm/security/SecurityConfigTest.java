package com.edm.security;

import com.edm.TestRedissonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "edm.admin.username=admin",
        "edm.admin.password=Admin@123456"
})
@Import(TestRedissonConfiguration.class)
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicReadEndpointIssuesCsrfCookie() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/auth/saml/enabled"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value(false))
                .andExpect(cookie().exists("XSRF-TOKEN"));
    }

    @Test
    void writeEndpointWithoutCsrfTokenIsRejected() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void writeEndpointWithCsrfTokenReachesAuthentication() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/logout")
                        .with(user("admin"))
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    @DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
    void acceptsRawCookieCsrfTokenForSpaLogin() throws Exception {
        var result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/auth/saml/enabled"))
                .andExpect(status().isOk())
                .andReturn();
        jakarta.servlet.http.Cookie csrfCookie = result.getResponse().getCookie("XSRF-TOKEN");

        org.assertj.core.api.Assertions.assertThat(csrfCookie).isNotNull();
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .post("/api/auth/login")
                        .cookie(csrfCookie)
                        .header("X-XSRF-TOKEN", csrfCookie.getValue())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"Admin@123456\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("EDM_TOKEN"))
                .andExpect(cookie().httpOnly("EDM_TOKEN", true))
                .andExpect(cookie().secure("EDM_TOKEN", false));
    }
}
