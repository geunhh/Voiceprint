package com.voiceprint.backend.user.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.global.event.UserEvent;
import com.voiceprint.backend.global.outbox.OutboxEventJpaEntity;
import com.voiceprint.backend.global.outbox.OutboxEventRepository;
import com.voiceprint.backend.user.adapter.in.web.dto.CustomOAuth2User;
import com.voiceprint.backend.user.adapter.in.web.dto.GoogleResponse;
import com.voiceprint.backend.user.adapter.in.web.dto.KakaoResponse;
import com.voiceprint.backend.user.adapter.in.web.dto.OAuth2Response;
import com.voiceprint.backend.global.exception.user.ProfileImageNotFoundException;
import com.voiceprint.backend.user.adapter.out.persistence.ProfileImageJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.ProfileImageRepository;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import com.voiceprint.backend.global.kafka.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository;
    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;

    private final EventPublisher eventPublisher;

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
            oAuth2Response = new KakaoResponse(oAuth2User.getAttributes());
        }
        else {
            return null; // 지원하지 않는 OAuth 제공자일 경우 null 반환
        }


        UserJPAEntity.AuthProvider provider = UserJPAEntity.AuthProvider.valueOf(providerName);
        String providerId = oAuth2Response.getProviderId();
        String name = oAuth2Response.getName();
        ProfileImageJPAEntity profileImage = profileImageRepository.findById((byte)1)
                .orElseThrow(() -> new ProfileImageNotFoundException("프로필 이미지를 찾을 수 없습니다."));;
        UserJPAEntity user = userRepository.findByProviderId(providerId)
                .orElseGet(() -> {
                    UserJPAEntity newUser = UserJPAEntity.builder()
                            .profileImage(profileImage)
                            .providerId(providerId)
                            .nickname(name)
                            .authProvider(provider)
                            .isDeleted(false)
                            .createdAt(LocalDateTime.now())  // not null 설정으로 인한 명시적 설정
                            .updatedAt(LocalDateTime.now())
                            .alarmTime(LocalTime.of(21, 0))  // ✅ 명시적으로 default 지정
                            .build();

                    UserJPAEntity savedUser = userRepository.save(newUser);

                    // Outbox 이벤트를 생성하고 저장
                    try {
                        UserEvent eventPayload = new UserEvent();
                        eventPayload.setEventType("USER_REGISTERED");
                        eventPayload.setUserId(savedUser.getId());

                        String payloadJson = objectMapper.writeValueAsString(eventPayload);

                        OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                            .eventId(UUID.randomUUID().toString())
                            .aggregateType("User")
                            .aggregateId(savedUser.getId().toString())
                            .eventType("USER_REGISTERED")
                            .payload(payloadJson)
                            .occurredAt(LocalDateTime.now())
                            .partitionKey(savedUser.getId().toString())
                            .build();

                        outboxEventRepository.save(outboxEvent);
                    } catch (Exception e) {
                        log.error("Outbox event creation failed for new user: {}", e.getMessage());
                    }

                    return savedUser;
                });

        return new CustomOAuth2User(user);
    }
}
