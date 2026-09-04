package com.edm.api;

import com.edm.TestRedissonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@SpringBootTest(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true",
        "edm.cors.allowed-origins=http://localhost:5173"
})
@Import(TestRedissonConfiguration.class)
class SwaggerAccessTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesOpenApi3WithCorsCredentials() throws Exception {
        mockMvc.perform(get("/v3/api-docs").header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value(startsWith("3.")))
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:5173"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void exposesSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }
}
