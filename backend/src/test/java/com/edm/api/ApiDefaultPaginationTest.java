package com.edm.api;

import com.edm.TestRedissonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class ApiDefaultPaginationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listEndpointsUseDefaultPaginationWhenParametersAreMissing() throws Exception {
        var authorities = List.of(
                "task:read",
                "supplier:read",
                "user:read"
        ).stream().map(SimpleGrantedAuthority::new).toArray(SimpleGrantedAuthority[]::new);

        for (String endpoint : List.of("/api/tasks", "/api/suppliers", "/api/users")) {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .get(endpoint)
                            .with(user("tester").authorities(authorities)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.page").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }
    }
}
