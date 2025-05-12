package com.voiceprint.backend.service.auth;


import com.voiceprint.backend.api.auth.dto.*;
import com.voiceprint.backend.common.exception.user.NicknameConflictException;
import com.voiceprint.backend.common.exception.user.ProfileImageNotFoundException;
import com.voiceprint.backend.common.util.JWTUtil;
import com.voiceprint.backend.domain.auth.*;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.auth.RefreshTokenRepository;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ProfileImageRepository profileImageRepository;
    private final DiaryRepository diaryRepository;

    public ProfileResponse getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저를 찾을 수 없습니다."));

        // 최근 일기 리스트 조회
        // 일기 리스트가 비어있을 경우 빈 리스트로 처리
        List<DiaryResponse> diaries = diaryRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DiaryResponse::new)  // Diary 객체를 DiaryResponse로 변환
                .collect(Collectors.toList());

        return new ProfileResponse(user.getId(), user.getNickname(), user.getProfileImage().getImageUrl(), diaries);
    }


    /**
     * 리프레시 토큰을 검증하고 새로운 액세스 토큰과 리프레시 토큰을 발급합니다.
     *
     * @param refreshToken 쿠키에서 추출한 리프레시 토큰
     * @return 새로 발급된 토큰 정보
     * @throws RuntimeException 토큰이 유효하지 않거나 Redis에 저장된 토큰과 일치하지 않는 경우
     */
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
        Long jwtId = Long.parseLong(claims.getId());
        if (jwtId == null) {
            throw new RuntimeException("토큰에 ID 정보가 없습니다.");
        }

        System.out.printf("email="+jwtId);
        // 3. Redis에 저장된 리프레시 토큰 조회
        String storedToken = refreshTokenRepository.findRefreshToken(jwtId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new RuntimeException("저장된 리프레시 토큰이 일치하지 않습니다.");
        }

        // 4. 토큰에 해당하는 사용자 조회 (jwtId가 userId)
        Long userId = jwtId;
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자 정보를 찾을 수 없습니다.");
        }
        User user = userOptional.get();

        // 5. 새로운 액세스 토큰과 리프레시 토큰 생성
        String newAccessToken = jwtUtil.createAccessToken(user.getEmail());
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
    @Transactional
    public void logout(String refreshToken) {
        try {
            // 클레임에서 jwtId (userId) 추출
            Claims claims = jwtUtil.getAllClaims(refreshToken);
            Long jwtId = Long.parseLong(claims.getId());

            if (jwtId != null) {
                // Redis에서 리프레시 토큰 삭제
                refreshTokenRepository.deleteRefreshToken(jwtId);
            }
        } catch (Exception e) {
            // 토큰 파싱 실패 등의 예외 발생 시 무시
            // 로그아웃은 항상 성공해야 함
        }
    }
    @Transactional(readOnly = true)
    public Long getUserIdFromRequest(HttpServletRequest request){

        return getUserIdFromAuthHeader(request.getHeader("Authorization"));
    }

    /**
     * Authorization 헤더에서 토큰을 추출하고, 토큰 유효성을 검증한 후 사용자 ID를 반환합니다.
     *
     * @param authorizationHeader Bearer 토큰이 포함된 Authorization 헤더 값
     * @return 유효한 토큰이고 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromAuthHeader(String authorizationHeader) {
        // 헤더가 null이거나 Bearer로 시작하지 않으면 null 반환
        String token = jwtUtil.extractTokenFromHeader(authorizationHeader);

        // 토큰 유효성 검증
        if (!jwtUtil.validateToken(token)) {
            log.error("토큰유효성실패");
            return null;
        }

        // 토큰에서 이메일 추출
        String email = jwtUtil.getEmail(token);
        if (email == null) {
            log.error("이메일 null 출력");
            return null;
        }
        log.info("email이 정상적으로 추출 : {}",email);

        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }

    /**
     * 토큰에서 이메일을 추출하고 해당 이메일의 사용자 ID를 반환합니다.
     *
     * @param token JWT 토큰
     * @return 유효한 토큰이고 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 그렇지 않으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdFromToken(String token) {
        // 토큰이 null이면 null 반환
        if (token == null) {
            return null;
        }

        try {
            // 토큰 유효성 검증
            if (!jwtUtil.validateToken(token)) {
                return null;
            }

            // 토큰에서 이메일 추출
            String email = jwtUtil.getEmail(token);
            if (email == null) {
                return null;
            }

            // 이메일로 사용자 조회
            Optional<User> userOptional = userRepository.findByEmail(email);

            // 사용자가 존재하면 ID 반환, 없으면 null 반환
            return userOptional.map(User::getId).orElse(null);
        } catch (Exception e) {
            // 토큰 처리 중 예외 발생 시 null 반환
            return null;
        }
    }

    /**
     * 이메일로 사용자 ID를 조회합니다.
     *
     * @param email 사용자 이메일
     * @return 해당 이메일의 사용자가 존재하면 사용자 ID 반환, 없으면 null 반환
     */
    @Transactional(readOnly = true)
    public Long getUserIdByEmail(String email) {
        // 이메일이 null이면 null 반환
        if (email == null) {
            return null;
        }

        // 이메일로 사용자 조회
        Optional<User> userOptional = userRepository.findByEmail(email);

        // 사용자가 존재하면 ID 반환, 없으면 null 반환
        return userOptional.map(User::getId).orElse(null);
    }

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

    public ProfileUpdateResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자 정보를 찾을 수 없습니다."));

        // 닉네임이 존재할 경우 업데이트
        // 닉네임 중복 체크
        if (request.getNickname() != null && !request.getNickname().isEmpty()) {
            if (isNicknameDuplicate(request.getNickname(), userId)) {
                throw new NicknameConflictException("중복된 닉네임이 있습니다.");
            }
            user.setNickname(request.getNickname());
        }

        // 프로필 이미지 ID가 존재할 경우 업데이트
        if (request.getProfileImageId() != null) {
            ProfileImage profileImage = profileImageRepository.findById(request.getProfileImageId())
                    .orElseThrow(() -> new ProfileImageNotFoundException("프로필 이미지를 찾을 수 없습니다."));
            user.setProfileImage(profileImage);
        }

        // 변경 사항 저장
        userRepository.save(user);

        return new ProfileUpdateResponse(user.getId(), user.getNickname(), user.getProfileImage().getId());
    }
    // 닉네임 중복 체크 메서드
    private boolean isNicknameDuplicate(String nickname, Long userId) {
        return userRepository.existsByNicknameAndIdNot(nickname, userId);
    }

    // 유저 알림 여부 확인 메서드
    public Boolean isReminderEnabled(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        return user.getEnableAlarm();
    }

    /**
     * 유저 알람 T/F 수정 메서드
     */
    public Boolean updateReminderSetting(Boolean enableAlarms, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));
        log.info("USER id : {}, 알람여부 : {}",userId,enableAlarms);


        user.setEnableAlarm(enableAlarms);
        userRepository.save(user);

        return user.getEnableAlarm();
    }
}