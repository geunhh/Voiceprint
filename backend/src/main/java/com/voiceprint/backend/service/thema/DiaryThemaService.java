package com.voiceprint.backend.service.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaCreateResponse;
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
@Transactional
public class DiaryThemaService {
    private final DiaryThemaRepository diaryThemaRepository;
    private final UserRepository userRepository;
    @Transactional(readOnly = true)
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

    public DiaryThemaCreateResponse createCustomThema(Long userId, String exampleDiary) {
        // 유저 정보 확인
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("유저 정보 없음"));
        //AI 서버 호출
//        airesponse =

        String prompt = "임시 프롬프트입니다.";
        String example = "임시 예시 일기입니ffff다.";


        // 기존 커스텀 테마 확인 후 업데이트
        DiaryThema existingThema = user.getCustomThema();
        if (existingThema == null) {
            // 기존 커스텀 테마가 없는 경우 생성자 호출
            existingThema = DiaryThema.creatDiaryThema(
                    user, null, null, prompt, example
            );
        }
        // 있으면, 갱신
        else {
            existingThema.setPrompt(prompt);
            existingThema.setExample(example);
        }

        DiaryThema saved = diaryThemaRepository.save(existingThema);
        user.setCustomThema(existingThema);
        userRepository.save(user);

        return new DiaryThemaCreateResponse(saved.getId(), saved.getExample());

    }
}
