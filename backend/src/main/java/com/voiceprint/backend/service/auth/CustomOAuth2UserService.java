package com.voiceprint.backend.service.auth;

import com.voiceprint.backend.api.auth.dto.CustomOAuth2User;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String email = oAuth2User.getAttribute("email");
        String providerName = userRequest.getClientRegistration().getRegistrationId();

        User.AuthProvider provider = User.AuthProvider.valueOf(providerName);

        User user = userRepository.findByAuthProviderAndEmail(provider, email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .nickname("user_" + UUID.randomUUID().toString().substring(0, 8))
                            .authProvider(provider)
                            .isDeleted(false)
                            .build();
                    return userRepository.save(newUser);
                });

        return new CustomOAuth2User(user);
    }
}
