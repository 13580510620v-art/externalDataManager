package com.edm;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@AutoConfigureTestDatabase
@SpringBootTest
@Import(TestRedissonConfiguration.class)
class ExternalDataManagerApplicationTests {

    @Test
    void contextLoads() {
    }
}
