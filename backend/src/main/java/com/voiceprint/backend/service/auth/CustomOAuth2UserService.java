package com.voiceprint.backend.service.auth;

import com.voiceprint.backend.api.auth.dto.CustomOAuth2User;
import com.voiceprint.backend.api.auth.dto.GoogleResponse;
import com.voiceprint.backend.api.auth.dto.OAuth2Response;
import com.voiceprint.backend.common.exception.user.ProfileImageNotFoundException;
import com.voiceprint.backend.domain.Entity.ProfileImage;
import com.voiceprint.backend.domain.Repository.ProfileImageRepository;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        System.out.println(oAuth2User);
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        OAuth2Response oAuth2Response = null;
        if (providerName.equals("google")) {

            oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
        }
        else if (providerName.equals("kakao")) {
            // 카카오 DTo
        }
        else {

            return null;
        }

        User.AuthProvider provider = User.AuthProvider.valueOf(providerName);
        String email = oAuth2Response.getEmail();
        String name = oAuth2Response.getName();
        ProfileImage profileImage = profileImageRepository.findById(1L)
                .orElseThrow(() -> new ProfileImageNotFoundException("프로필 이미지를 찾을 수 없습니다."));;
        User user = userRepository.findByAuthProviderAndEmail(provider, email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .profileImage(profileImage)
                            .email(email)
                            .nickname(name)
                            .authProvider(provider)
                            .isDeleted(false)
                            .createdAt(LocalDateTime.now())  // not null 설정으로 인한 명시적 설정
                            .updatedAt(LocalDateTime.now())
                            .build();
                    return userRepository.save(newUser);
                });

        return new CustomOAuth2User(user);
    }
}
