package com.edm;

import com.edm.security.LoginUser;
import com.edm.security.TokenSession;
import com.edm.security.TokenSessionStore;
import org.mockito.Mockito;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@TestConfiguration(proxyBeanMethods = false)
public class TestRedissonConfiguration {

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        return Mockito.mock(RedissonClient.class);
    }

    @Bean
    @Primary
    public TokenSessionStore tokenSessionStore() {
        return new InMemoryTokenSessionStore();
    }

    private static class InMemoryTokenSessionStore implements TokenSessionStore {

        private final Map<String, LoginUser> sessions = new ConcurrentHashMap<>();

        @Override
        public void save(TokenSession session, LoginUser user) {
            sessions.put(session.tokenHash(), user);
        }

        @Override
        public Optional<LoginUser> find(String tokenHash) {
            return Optional.ofNullable(sessions.get(tokenHash));
        }

        @Override
        public void delete(String tokenHash) {
            sessions.remove(tokenHash);
        }
    }
}
