package com.edm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Component
public class RedisTokenSessionStore implements TokenSessionStore {

    private static final String KEY_PREFIX = "edm:auth:token:";

    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    public RedisTokenSessionStore(RedissonClient redissonClient, ObjectMapper objectMapper) {
        this.redissonClient = redissonClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void save(TokenSession session, LoginUser user) {
        String json = toJson(user);
        Duration ttl = Duration.between(Instant.now(), session.expiresAt());
        if (ttl.isNegative() || ttl.isZero()) {
            getBucket(session.tokenHash()).delete();
            return;
        }
        getBucket(session.tokenHash()).set(json, ttl);
    }

    @Override
    public Optional<LoginUser> find(String tokenHash) {
        String json = getBucket(tokenHash).get();
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.readValue(json, LoginUser.class));
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    @Override
    public void delete(String tokenHash) {
        getBucket(tokenHash).delete();
    }

    private String toJson(LoginUser user) {
        try {
            return objectMapper.writeValueAsString(user);
        } catch (Exception exception) {
            throw new IllegalStateException("登录会话序列化失败", exception);
        }
    }

    private org.redisson.api.RBucket<String> getBucket(String tokenHash) {
        return redissonClient.getBucket(KEY_PREFIX + tokenHash);
    }
}
