package com.edm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisTokenSessionStoreTest {

    private final RedissonClient redissonClient = mock(RedissonClient.class);
    private final RBucket<String> bucket = mock(RBucket.class);
    private final RedisTokenSessionStore store = new RedisTokenSessionStore(
            redissonClient,
            new ObjectMapper()
    );

    @Test
    void savesUserJsonWithTtlUnderHashKey() {
        TokenSession session = new TokenSession("token", "hash", Instant.now().plusSeconds(120));
        LoginUser user = new LoginUser(1L, "admin", "系统管理员", true, Set.of("dashboard:read"));
        when(redissonClient.<String>getBucket("edm:auth:token:hash")).thenReturn(bucket);

        store.save(session, user);

        verify(bucket).set(anyString(), argThat(duration -> !duration.isNegative() && !duration.isZero()));
    }

    @Test
    void readsAndDeletesUserJson() throws Exception {
        LoginUser user = new LoginUser(1L, "admin", "系统管理员", true, Set.of("dashboard:read"));
        String json = new ObjectMapper().writeValueAsString(user);
        when(redissonClient.<String>getBucket("edm:auth:token:hash")).thenReturn(bucket);
        when(bucket.get()).thenReturn(json);

        Optional<LoginUser> found = store.find("hash");

        assertThat(found).contains(user);
        store.delete("hash");
        verify(bucket).delete();
    }
}
