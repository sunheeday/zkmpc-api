package com.zkrypto.zkmpc_api.infrastructure.cache;

import com.zkrypto.zkmpc_api.domain.member.domain.service.AuthCodeManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

@Component
public class RedisAuthCodeManager implements AuthCodeManager {

    private static final String KEY_PREFIX = "auth_code:";
    private static final Duration EXPIRE_TIME = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public RedisAuthCodeManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String createRedisKey(String email) {
        return KEY_PREFIX + email;
    }

    @Override
    public void save(String email, String code) {
        String key = createRedisKey(email);
        redisTemplate.opsForValue().set(key, code, EXPIRE_TIME);
    }

    @Override
    public Optional<String> get(String email) {
        String key = createRedisKey(email);
        String code = redisTemplate.opsForValue().get(key);
        return Optional.of(code);
    }

    @Override
    public void remove(String email) {
        String key = createRedisKey(email);
        redisTemplate.delete(key);
    }

    @Override
    public String generateCode() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }
}