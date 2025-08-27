package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.global.exception.user.ProfileImageNotFoundException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final ProfileImageRepository profileImageRepository; // Injected
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId)
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public boolean existsByNicknameAndIdNot(String nickname, Integer userId) {
        return userRepository.existsByNicknameAndIdNot(nickname, userId);
    }

    @Override
    public User save(User user) {
        UserJPAEntity savedEntity = null;
        try {
            UserJPAEntity entity = userMapper.toEntity(user);
            savedEntity = userRepository.save(entity);
            log.info("저장이 안된거같은데??");
        } catch(Exception e) {
            log.info("error {}",e.getMessage());
        }
        log.info("savedEntity : {}",savedEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public List<User> findUsersWithAlarmEnabledAt(LocalTime alarmTime) {
        return userRepository.findByEnableAlarmIsTrueAndAlarmTime(alarmTime)
                .stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<User> findUserWithUsingThema(Integer userId) {
        return userRepository.findUserWithUsingThema(userId)
                .map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findById(Integer userId) {
        return userRepository.findById(userId).map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public void updateEnableAlarm(Integer userId, Boolean enable) {
        UserJPAEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));
        userEntity.setEnableAlarm(enable);
    }

    @Override
    @Transactional
    public void updateAlarmTime(Integer userId, LocalTime alarmTime) {
        UserJPAEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));
        userEntity.setAlarmTime(alarmTime);
    }

    @Override
    @Transactional
    public void updateProfile(Integer userId, String newNickname, Byte newProfileImageId) {
        UserJPAEntity userEntity = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보가 없습니다."));

        if (newNickname != null) {
            userEntity.setNickname(newNickname);
        }

        if (newProfileImageId != null) {
            ProfileImageJPAEntity profileImageEntity = profileImageRepository.findById(newProfileImageId)
                    .orElseThrow(() -> new ProfileImageNotFoundException("프로필 이미지를 찾을 수 없습니다."));
            userEntity.setProfileImage(profileImageEntity);
        }
    }
}
