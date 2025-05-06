package com.voiceprint.backend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JWTUtil {
    private Key key;

    private final long accessTokenValidity = 1000 * 60 * 10; // 10분
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


    public String createAccessToken(String email) {
        return Jwts.builder()
                .setSubject("access")
                .claim("email", email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenValidity))
                .signWith(key)
                .compact();
    }

    public String createRefreshToken() {
        return Jwts.builder()
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
     * 2. 이메일 존재 여부 확인
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = getAllClaims(token);
            return !isExpired(token) && claims.get("email") != null;
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
}
