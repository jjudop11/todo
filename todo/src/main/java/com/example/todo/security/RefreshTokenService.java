package com.example.todo.security;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    public RefreshTokenService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String key(String username) {
        return "refresh:" + username;
    }

    public void storeRefreshToken(String username, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(key(username), refreshToken, ttl);
    }

    public Optional<String> getRefreshToken(String username) {
        String token = redisTemplate.opsForValue().get(key(username));
        return Optional.ofNullable(token);
    }

    public void deleteRefreshToken(String username) {
        redisTemplate.delete(key(username));
    }
}


