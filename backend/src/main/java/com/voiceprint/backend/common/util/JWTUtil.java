package com.voiceprint.backend.common.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Component
public class JWTUtil {
    private Key key;

    private final long accessTokenValidity = 1000 * 60 * 10000; // 10분
    private final long refreshTokenValidity = 1000 * 60 * 60 * 24 * 1; // 1일

    public JWTUtil(@Value("${spring.jwt.secret}") String secret) {
        key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
    }

    public String getEmail(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("email", String.class);
    }

    public boolean isExpired(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }

    /**
     * 액세스 토큰 생성
     * @param email 사용자 이메일
     * @return 생성된 액세스 토큰
     */
    public String createAccessToken(String email) {
        return Jwts.builder()
                .setSubject("access")
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key)
                .compact();
    }

    /**
     * 사용자 ID를 포함한 리프레시 토큰 생성
     * @param userId 사용자 ID
     * @return 생성된 리프레시 토큰
     */
    public String createRefreshToken(Long userId) {
        return Jwts.builder()
                .setId(userId.toString()) // Redis 키로 사용할 ID 설정
                .setSubject("refresh")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenValidity))
                .signWith(key)
                .compact();
    }

    /**
     * Authorization 헤더에서 토큰을 추출
     * Bearer 토큰 형식을 사용
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    /**
     * 토큰의 유효성을 검증
     * 1. 만료 여부 확인
     * 2. 이메일 존재 여부 확인 (액세스 토큰인 경우)
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            return !isExpired(token) &&
                    (claims.getSubject().equals("refresh") || claims.get("email") != null);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 토큰에서 모든 클레임 정보를 가져옵니다.
     */
    public Claims getAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 토큰에서 사용자 ID를 추출합니다. (리프레시 토큰용)
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            String id = claims.getId();
            return id != null ? Long.parseLong(id) : null;
        } catch (Exception e) {
            return null;
        }
    }
}