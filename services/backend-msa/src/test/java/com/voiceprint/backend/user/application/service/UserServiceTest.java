package com.voiceprint.backend.user.application.service;

import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.user.adapter.out.persistence.RefreshTokenRepository;
import com.voiceprint.backend.global.exception.user.ProfileImageNotFoundException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.user.adapter.in.web.dto.ProfileResponse;
import com.voiceprint.backend.user.application.port.out.ProfileImageRepositoryPort;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.ProfileImage;
import com.voiceprint.backend.user.domain.User;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.domain.Emotion;
import com.voiceprint.common.auth.JWTUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepositoryPort userRepository;
    @Mock
    private JWTUtil jwtUtil;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private ProfileImageRepositoryPort profileImageRepository;
    @Mock
    private DiaryRepositoryPort diaryRepositoryPort; // Corrected name

    @InjectMocks
    private UserService userService;

    private User testUser;
    private ProfileImage testProfileImage;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1)
                .nickname("testuser")
                .profileImageId((byte) 1)
                .build();

        testProfileImage = ProfileImage.builder()
                .id((byte) 1)
                .imageUrl("http://example.com/image1.png")
                .build();
    }

    @Test
    @DisplayName("getProfile - 유저 프로필 조회 성공")
    void getProfile_Success() {
        // Given
        Integer userId = 1;
        Emotion emotion = Emotion.builder().id((byte)1).name("행복").color("#FFFFFF").build();
        Diary diary1 = Diary.builder()
                .id(1)
                .title("일기1")
                .content("내용1")
                .createdAt(LocalDateTime.now())
                .emotion(emotion)
                .build();
        Diary diary2 = Diary.builder()
                .id(2)
                .title("일기2")
                .content("내용2")
                .createdAt(LocalDateTime.now().minusDays(1))
                .emotion(emotion)
                .build();
        List<Diary> diaries = Arrays.asList(diary1, diary2);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileImageRepository.findById(testUser.getProfileImageId())).thenReturn(Optional.of(testProfileImage));
        when(diaryRepositoryPort.findTop5ByUserIdOrderByCreatedAtDesc(userId)).thenReturn(diaries);

        // When
        ProfileResponse response = userService.getProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getNickname(), response.getNickname());
        assertEquals(testProfileImage.getImageUrl(), response.getImageUrl());
        assertEquals(diaries.size(), response.getDiaries().size());
        assertEquals(diary1.getId(), response.getDiaries().get(0).getDiaryId());
        assertEquals(diary2.getId(), response.getDiaries().get(1).getDiaryId());

        verify(userRepository, times(1)).findById(userId);
        verify(profileImageRepository, times(1)).findById(testUser.getProfileImageId());
        verify(diaryRepositoryPort, times(1)).findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    @DisplayName("getProfile - 유저를 찾을 수 없을 때 UserNotFoundException 발생")
    void getProfile_UserNotFound() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(UserNotFoundException.class, () -> userService.getProfile(userId));
        verify(userRepository, times(1)).findById(userId);
        verifyNoInteractions(profileImageRepository); // Should not be called
        verifyNoInteractions(diaryRepositoryPort); // Should not be called
    }

    @Test
    @DisplayName("getProfile - 프로필 이미지를 찾을 수 없을 때 ProfileImageNotFoundException 발생")
    void getProfile_ProfileImageNotFound() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileImageRepository.findById(testUser.getProfileImageId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ProfileImageNotFoundException.class, () -> userService.getProfile(userId));
        verify(userRepository, times(1)).findById(userId);
        verify(profileImageRepository, times(1)).findById(testUser.getProfileImageId());
    }

    @Test
    @DisplayName("getProfile - 일기 목록이 비어있을 때 빈 리스트 반환")
    void getProfile_EmptyDiaries() {
        // Given
        Integer userId = 1;
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(profileImageRepository.findById(testUser.getProfileImageId())).thenReturn(Optional.of(testProfileImage));
        when(diaryRepositoryPort.findTop5ByUserIdOrderByCreatedAtDesc(userId)).thenReturn(Collections.emptyList());

        // When
        ProfileResponse response = userService.getProfile(userId);

        // Then
        assertNotNull(response);
        assertEquals(testUser.getId(), response.getUserId());
        assertEquals(testUser.getNickname(), response.getNickname());
        assertEquals(testProfileImage.getImageUrl(), response.getImageUrl());
        assertTrue(response.getDiaries().isEmpty());

        verify(userRepository, times(1)).findById(userId);
        verify(profileImageRepository, times(1)).findById(testUser.getProfileImageId());
        verify(diaryRepositoryPort, times(1)).findTop5ByUserIdOrderByCreatedAtDesc(userId);
    }
}