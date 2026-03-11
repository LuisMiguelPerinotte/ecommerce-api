package com.java.luismiguel.ecommerce_api.application.auth;

import com.java.luismiguel.ecommerce_api.infrastructure.security.jwt.JwtProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class RefreshTokenService {
    private final RedisTemplate<String, String> redisTemplate;
    private final JwtProperties jwtProperties;

    public RefreshTokenService(RedisTemplate<String, String> redisTemplate, JwtProperties jwtProperties) {
        this.redisTemplate = redisTemplate;
        this.jwtProperties = jwtProperties;
    }

    public void saveRefreshToken(UUID userId, String refreshToken) {
        redisTemplate.opsForValue().set(
                buildKey(userId),
                refreshToken,
                jwtProperties.getRefreshExpiration(),
                TimeUnit.MILLISECONDS
        );
    }

    public String getRefreshToken(UUID userId) {
        return redisTemplate.opsForValue().get(buildKey(userId));
    }

    public void deleteRefreshToken(UUID userId) {
        redisTemplate.delete(buildKey(userId));
    }

    public boolean isValid(UUID userId, String refreshToken) {
        String stored = getRefreshToken(userId);
        return stored != null && stored.equals(refreshToken);
    }

    private String buildKey(UUID userId) {
        return "refresh:" + userId;
    }

    public void blackListAccessToken(String accessToken, long expirationMs) {
        redisTemplate.opsForValue().set(
                "blacklist:" + accessToken,
                "true",
                expirationMs,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isBlacklisted(String accessToken) {
        return Boolean.TRUE.toString().equals(
                redisTemplate.opsForValue().get("blacklist:" + accessToken)
        );
    }
}
