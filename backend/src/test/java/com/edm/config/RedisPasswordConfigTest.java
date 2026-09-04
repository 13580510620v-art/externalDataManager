package com.edm.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

import static org.assertj.core.api.Assertions.assertThat;

class RedisPasswordConfigTest {

    @Test
    void treatsBlankRedisPasswordAsNoPassword() {
        RedisProperties properties = new RedisProperties();
        properties.setPassword("");

        Object result = new RedisPasswordConfig()
                .redisPasswordBeanPostProcessor()
                .postProcessAfterInitialization(properties, "redisProperties");

        assertThat(((RedisProperties) result).getPassword()).isNull();
    }

    @Test
    void keepsNonBlankRedisPassword() {
        RedisProperties properties = new RedisProperties();
        properties.setPassword("secret");

        Object result = new RedisPasswordConfig()
                .redisPasswordBeanPostProcessor()
                .postProcessAfterInitialization(properties, "redisProperties");

        assertThat(((RedisProperties) result).getPassword()).isEqualTo("secret");
    }
}
