package com.voiceprint.backend.user.adapter.in.web.dto;

import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class CustomOAuth2User implements OAuth2User {

    @Getter
    private final UserJPAEntity user;
    private final Collection<? extends GrantedAuthority> authorities;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(UserJPAEntity user) {
        this.user = user;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        this.attributes = Map.of(
                "id", user.getId(),
                "providerId", user.getProviderId(),
                "name", user.getNickname()
        );
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getName() {
        return user.getProviderId();
    }

    // OAuth2User 인터페이스의 getName()은 principal의 name을 반환하도록 되어 있는데,
    // 우리는 providerId을 principal로 사용하기 때문에 getUsername()은 providerId을 반환
    public String getUsername() {
        return user.getProviderId();
    }

    // 사용자 ID 반환 (리프레시 토큰용)
    public Integer getUserId() {
        return user.getId();
    }
}