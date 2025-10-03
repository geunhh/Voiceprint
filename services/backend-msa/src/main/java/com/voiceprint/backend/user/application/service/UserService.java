package com.voiceprint.backend.user.application.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.global.event.UserEvent;
import com.voiceprint.backend.global.outbox.OutboxEventJpaEntity;
import com.voiceprint.backend.global.outbox.OutboxEventRepository;
import com.voiceprint.backend.user.adapter.in.web.dto.*;
import com.voiceprint.backend.global.exception.user.*;
import com.voiceprint.backend.user.adapter.out.persistence.RefreshTokenRepository;
import com.voiceprint.backend.user.application.port.in.*;
import com.voiceprint.backend.user.application.port.out.ProfileImageRepositoryPort;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.ProfileImage;
import com.voiceprint.backend.user.domain.User;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class UserService implements GetProfileImagesUseCase, GetProfileUseCase, GetUserUseCase,
        LogoutUseCase, ManageAlarmSettingsUseCase, ReissueTokenUseCase, UpdateProfileUseCase {

    private final UserRepositoryPort userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileImageRepositoryPort profileImageRepository;
    private final DiaryRepositoryPort diaryRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        // 최근 일기 리스트 조회
        // 일기 리스트가 비어있을 경우 빈 리스트로 처리
        List<DiaryResponse> diaries = diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DiaryResponse::new).collect(Collectors.toList());

        ProfileImage profileImage = profileImageRepository.findById(user.getProfileImageId())
                .orElseThrow(() -> new ProfileImageNotFoundException("프로필 이미지를 찾을 수 없음"));
        return new ProfileResponse(user.getId(), user.getNickname(), profileImage.getImageUrl(), diaries);
    }


    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰
     * @return 새로 발급된 토큰 정보
     * @throws RuntimeException 토큰이 유효하지 않거나 Redis에 저장된 토큰과 일치하지 않는 경우
     */
    @Override
    @Transactional
    public TokenResponse reissueToken(String refreshToken) {
        // 1. 리프레시 토큰 유효성 검증
        if (jwtUtil.isExpired(refreshToken)) {
            throw new RuntimeException("만료된 리프레시 토큰입니다.");
        }

        // 2. 클레임에서 이메일 추출 (JWT 구조 변경 필요)
        Claims claims = jwtUtil.getAllClaims(refreshToken);
        String subject = claims.getSubject();
        if (!"refresh".equals(subject)) {
            throw new RuntimeException("유효하지 않은 리프레시 토큰입니다.");
        }

        // 클레임으로부터 이메일을 가져오거나, 토큰 자체를 Redis 키로 사용하는 로직이 필요
        // 여기서는 jwtId를 키로 사용하는 방식으로 가정
        Integer jwtId = Integer.parseInt(claims.getId());
        if (jwtId == null) {
            throw new RuntimeException("토큰에 ID 정보가 없습니다.");
        }

        System.out.printf("jwtId="+jwtId);
        // 3. Redis에 저장된 리프레시 토큰 조회
        String storedToken = refreshTokenRepository.findRefreshToken(jwtId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("저장된 리프레시 토큰이 일치하지 않습니다.");
        }

        // 4. 토큰에 해당하는 사용자 조회 (jwtId가 userId)
        Integer userId = jwtId;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        User user = userOptional.get();

        // 5. 새로운 액세스 토큰과 리프레시 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(user.getProviderId());
        String newRefreshToken = jwtUtil.createRefreshToken(user.getId());

        // 6. Redis에 새로운 리프레시 토큰 저장
        refreshTokenRepository.saveRefreshToken(jwtId, newRefreshToken);

        // 7. 새로운 토큰 정보 반환
        return TokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .userId(user.getId())
                .build();

    }

    /**
     * 로그아웃 처리
     * 리프레시 토큰을 파싱하여 사용자 ID를 추출하고, Redis에서 해당 토큰을 삭제합니다.
     *
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰
     */
    @Override
    @Transactional
    public void logout(String refreshToken) {
        try {
            // 클레임에서 jwtId (userId) 추출
            Claims claims = jwtUtil.getAllClaims(refreshToken);
            Integer jwtId = Integer.parseInt(claims.getId());

            if (jwtId != null) {
                // Redis에서 리프레시 토큰 삭제
                refreshTokenRepository.deleteRefreshToken(jwtId);
            }
        } catch (Exception e) {
            // 토큰 파싱 실패 등의 예외 발생 시 무시
            // 로그아웃은 항상 성공해야 함
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Integer getUserIdFromRequest(HttpServletRequest request){

        return getUserIdFromAuthHeader(request.getHeader("Authorization"));
    }

    /**
     * Authorization 헤더에서 토큰을 추출하고, 토큰 유효성을 검증한 후 사용자 ID를 반환합니다.
     *
     * @param authorizationHeader Bearer 토큰이 포함된 Authorization 헤더 값
     * @return 유효한 토큰이고 해당 providerId의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUserIdFromAuthHeader(String authorizationHeader) {
        // 헤더가 null이거나 Bearer로 시작하지 않으면 null 반환
        String token = jwtUtil.extractTokenFromHeader(authorizationHeader);

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            log.error("토큰유효성실패");
            return null;
        }

        // 토큰에서 providerId 추출
        String providerId = jwtUtil.getProviderId(token);
        if (providerId == null) {
            log.error("providerId null");
            return null;
        }
        log.info("providerId이 정상적으로 추출 : {}",providerId);

        // providerId로 사용자 조회
        Optional<User> userOptional = userRepository.findByProviderId(providerId);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }

    /**
     * 토큰에서 providerId을 추출하고 해당 providerId의 사용자 ID를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 토큰이고 해당 providerId의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUserIdFromToken(String token) {
        // 토큰이 null이면 null 반환
        if (token == null) {
            return null;
        }

        try {
            // 토큰 유효성 검증
            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            // 토큰에서 providerId 추출
            String providerId = jwtUtil.getProviderId(token);
            if (providerId == null) {
                return null;
            }

            // providerId로 사용자 조회
            Optional<User> userOptional = userRepository.findByProviderId(providerId);

            // 사용자가 존재하면 ID 반환, 없으면 null 반환
            return userOptional.map(User::getId).orElse(null);
        } catch (Exception e) {
            // 토큰 처리 중 예외 발생 시 null 반환
            return null;
        }
    }

    /**
     * providerId로 사용자 ID를 조회합니다.
     *
     * @param providerId 사용자 providerId
     * @return 해당 providerId의 사용자가 존재하면 사용자 ID 반환, 없으면 null 반환
     */
    @Override
    @Transactional(readOnly = true)
    public Integer getUserIdByProviderId(String providerId) {
        // providerId이 null이면 null 반환
        if (providerId == null) {
            return null;
        }

        // providerId로 사용자 조회
        Optional<User> userOptional = userRepository.findByProviderId(providerId);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfileImageResponse> getProfileImages() {
        // DB에서 모든 프로필 이미지 정보를 조회
        List<ProfileImage> images = profileImageRepository.findAll();
        // 빈 목록일 경우 빈 리스트 반환
        if (images.isEmpty()) {
            throw new ProfileImageNotFoundException("프로필 이미지가 없습니다.");
        }
        // 이미지 정보를 DTO로 변환하여 반환
        return images.stream()
                .map(image -> new ProfileImageResponse(image.getId(), image.getTitle(),image.getImageUrl()))
                .collect(Collectors.toList());
    }
    @Override
    public ProfileUpdateResponse updateProfile(Integer userId, ProfileUpdateRequest request) {
        // 닉네임 중복과 같은 비즈니스 로직 검증은 서비스 레이어의 책임
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            if (isNicknameDuplicate(request.getNickname(), userId)) {
                throw new NicknameConflictException("중복된 닉네임이 있습니다.");
            }
        }

        // 실제 DB 업데이트는 포트를 통해 어댑터에 위임
        userRepository.updateProfile(userId, request.getNickname(), request.getProfileImageId());

        // 변경된 최종 결과를 반환하기 위해 다시 조회
        User updatedUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

//        // UserEvent 발행
//        UserEvent evt = new UserEvent();
//        evt.setEventType("USER_PROFILE_UPDATED");
//        evt.setUserId(updatedUser.getId());
//        eventPublisher.publishUserEvent(evt);

        // Outbox 이벤트를 생성하고 저장
        try {
            UserEvent eventPayload = new UserEvent();
            eventPayload.setEventType("USER_PROFILE_UPDATE");
            eventPayload.setUserId(updatedUser.getId());

            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("User")
                    .aggregateId(updatedUser.getId().toString())
                    .eventType("USER_PROFILE_UPDATE")
                    .payload(payloadJson)
                    .occurredAt(LocalDateTime.now())
                    .partitionKey(updatedUser.getId().toString())
                    .build();

            outboxEventRepository.save(outboxEvent);

        } catch (Exception e) {
            log.error("Outbox event creation failed for user {}", updatedUser.getId(), e);
        }

        return new ProfileUpdateResponse(updatedUser.getId(),
                updatedUser.getNickname(),
                updatedUser.getProfileImageId());

    }

    // 유저 알림 여부 확인 메서드
    @Override
    @Transactional(readOnly = true)
    public AlarmSettingsResponseDTO isReminderEnabled(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));
        if (user.getEnableAlarm()!=null) {
            return new AlarmSettingsResponseDTO(user.getEnableAlarm().toString(), user.getAlarmTime());
        } else {
            return new AlarmSettingsResponseDTO(null, user.getAlarmTime());
        }
    }

    /**
     * 유저 알람 T/F 수정 메서드
     */
    @Override
    public Boolean updateReminderSetting(Boolean enableAlarms, Integer userId) {
        // DB 업데이트 로직은 포트를 통해 어댑터에 위임
        userRepository.updateEnableAlarm(userId, enableAlarms);

        // Outbox 이벤트를 생성하고 저장
        try {
            UserEvent eventPayload = new UserEvent();
            eventPayload.setEventType("USER_NOTIFICATION_PREFERENCES_UPDATED");
            eventPayload.setUserId(userId);
            eventPayload.setEnableAlarms(enableAlarms);

            String payloadJson = objectMapper.writeValueAsString(eventPayload);

            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("User")
                .aggregateId(userId.toString())
                .eventType("USER_NOTIFICATION_PREFERENCES_UPDATED")
                .payload(payloadJson)
                .occurredAt(LocalDateTime.now())
                .partitionKey(userId.toString())
                .build();

            outboxEventRepository.save(outboxEvent);
        } catch (Exception e) {
            log.error("Outbox event creation failed for user {}", userId, e);
        }

        return enableAlarms;
    }

    /**
     * 유저 알람 시간 수정 메서드
     */
    @Override
    public String updateReminderTime(String alarmTime, Integer userId) {
        // 입력값 파싱 및 검증은 서비스 레이어의 책임
        try {
            LocalTime time = LocalTime.parse(alarmTime); //HH:mm

            if (time.getMinute() != 0 && time.getMinute() !=30) {
                return null;
            }

            // DB 업데이트 로직은 포트를 통해 어댑터에 위임
            userRepository.updateAlarmTime(userId, time);
            // Outbox 이벤트를 생성하고 저장
            try {

                String newEventId = UUID.randomUUID().toString();
                LocalDateTime newNow = LocalDateTime.now();

                UserEvent eventPayload = new UserEvent();
                eventPayload.setEventType("USER_NOTIFICATION_PREFERENCES_UPDATED");
                eventPayload.setEventId(newEventId);
                eventPayload.setUserId(userId);
                eventPayload.setAlarmTime(time.toString()); // "HH:mm[:ss]"
                eventPayload.setOccurredAt(newNow.toString());

                String payloadJson = objectMapper.writeValueAsString(eventPayload);

                OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .eventId(newEventId)
                    .aggregateType("User")
                    .aggregateId(userId.toString())
                    .eventType("USER_NOTIFICATION_PREFERENCES_UPDATED")
                    .payload(payloadJson)
                    .occurredAt(newNow)
                    .partitionKey(userId.toString())
                    .build();

                outboxEventRepository.save(outboxEvent);
            } catch (Exception e) {
                log.error("Outbox event creation failed for user {}", userId, e);
            }

            return time.toString();

        } catch (DateTimeParseException e) {
            return null;
        }

    }


    // 닉네임 중복 체크 메서드
    private boolean isNicknameDuplicate(String nickname, Integer userId) {
        return userRepository.existsByNicknameAndIdNot(nickname, userId);
    }
}