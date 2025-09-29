package com.example.todo.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
public class EmailTokenService {
    private final StringRedisTemplate redis;

    public EmailTokenService(StringRedisTemplate redis) { this.redis = redis; }

    private String key(String prefix, String token) { return prefix + ":" + token; }

    public String generateAndStore(String prefix, String value, Duration ttl) {
        String token = UUID.randomUUID().toString();
        redis.opsForValue().set(key(prefix, token), value, ttl);
        return token;
    }

    public Optional<String> consume(String prefix, String token) {
        String k = key(prefix, token);
        String value = redis.opsForValue().get(k);
        if (value != null) redis.delete(k);
        return Optional.ofNullable(value);
    }
}


