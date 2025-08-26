package com.voiceprint.backend.user.adapter.out.persistence;

import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserRepositoryPort {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Optional<User> findByProviderId(String providerId) {
        return userRepository.findByProviderId(providerId)
                .map(userMapper::toDomain);
    }

    @Override
    public boolean existsByNicknameAndIdNot(String nickname, Integer userId) {
        return userRepository.existsByNicknameAndIdNot(nickname, userId);
    }

    @Override
    public User save(User user) {
        UserJPAEntity savedEntity = null;
        try {
            UserJPAEntity entity = userMapper.toEntity(user);
            savedEntity = userRepository.save(entity);
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
    public Optional<UserJPAEntity> findJPAById(Integer userId) {
        return userRepository.findById(userId);
    }

    @Override
    public void saveJPA(UserJPAEntity userJPAEntity) {
        userRepository.save(userJPAEntity);
    }
}
