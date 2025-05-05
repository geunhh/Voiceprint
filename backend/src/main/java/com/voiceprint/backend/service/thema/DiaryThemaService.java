package com.voiceprint.backend.service.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaListResponseDTO;
import com.voiceprint.backend.api.thema.dto.DiaryThemaResponse;
import com.voiceprint.backend.common.exception.thema.ThemaNotFoundExceiption;
import com.voiceprint.backend.common.exception.thema.UnauthorizedThemaAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.thema.DiaryThema;
import com.voiceprint.backend.domain.thema.DiaryThemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryThemaService {
    private final DiaryThemaRepository diaryThemaRepository;
    private final UserRepository userRepository;
    public DiaryThemaListResponseDTO getThemasForUser(Long userId) {

        List<DiaryThema> themas = diaryThemaRepository.findByUserIdOrDefault(userId);
        System.out.println(themas);

        List<DiaryThemaResponse> defaultThemas = new ArrayList<>();
        List<DiaryThemaResponse> customThemas = new ArrayList<>();

        for (DiaryThema thema : themas) {
            DiaryThemaResponse dto = DiaryThemaResponse.from(thema);
            if (thema.getUser() == null) {
                defaultThemas.add(dto);
            } else {
                customThemas.add(dto);
            }
        }

        return new DiaryThemaListResponseDTO(defaultThemas,customThemas);
    }

    @Transactional
    public void selectThema(Long userId, Long themaId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저정보X"));
        System.out.println("현재 사용중인 테마 :"+user);

        DiaryThema thema = diaryThemaRepository.findById(themaId)
                .orElseThrow(() -> new ThemaNotFoundExceiption("테마 정보 없음"));

        // 기본테마이거나 아닐 경우, 테마에 권한이 있는지 확인.
        if (thema.getUser() != null && !thema.getUser().getId().equals(userId)) {
            throw new UnauthorizedThemaAccessException("권한이 없는 테마입니다.");
        }

        // 유저 테마 갱신
        user.setUsingThema(thema);

    }
}
