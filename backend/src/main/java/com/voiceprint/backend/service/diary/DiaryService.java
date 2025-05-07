package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.common.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.diary.Diary;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    public DiaryDetailResponseDTO getDiaryDetail(HttpServletRequest request, Long diaryId) {
        // 유저 정보 추출 및 확인
        Long userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 확인 불가"));

        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("다이어리 정보가 없습니다."));

        // 일기의 user FK와 비교
        if (!diary.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryAccessException("다이어리의 userId와 일치하지 않습니다.");
        }

        return new DiaryDetailResponseDTO(
                diary.getId(),
                diary.getTitle(),
                diary.getContent(),
                diary.getEmotion() != null ? diary.getEmotion().getName() : null,
                diary.getCreatedAt().toString(),
                diary.getUser().getNickname(),
                diary.getThumbnail() != null ? diary.getThumbnail() : null
        );
    }
}
