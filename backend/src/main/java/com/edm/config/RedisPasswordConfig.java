package com.edm.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class RedisPasswordConfig {

    @Bean
    static BeanPostProcessor redisPasswordBeanPostProcessor() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) {
                if (bean instanceof RedisProperties properties
                        && !StringUtils.hasText(properties.getPassword())) {
                    properties.setPassword(null);
                }
                return bean;
            }
        };
    }
}
