package com.voiceprint.backend.service.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaCreateResponse;
import com.voiceprint.backend.api.thema.dto.DiaryThemaListResponseDTO;
import com.voiceprint.backend.api.thema.dto.DiaryThemaResponse;
import com.voiceprint.backend.api.thema.dto.UsingDiaryThemaResponseDTO;
import com.voiceprint.backend.common.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.common.exception.diary.DiaryThemaNotFoundException;
import com.voiceprint.backend.common.exception.diary.InvalidPromptException;
import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.common.exception.thema.ThemaNotFoundExceiption;
import com.voiceprint.backend.common.exception.thema.UnauthorizedThemaAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.diary.Diary;
import com.voiceprint.backend.domain.diary.DiaryRepository;
import com.voiceprint.backend.domain.thema.DiaryThema;
import com.voiceprint.backend.domain.thema.DiaryThemaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiaryThemaService {
    private final DiaryThemaRepository diaryThemaRepository;
    private final UserRepository userRepository;
    private final DiaryRepository diaryRepository;
    private final WebClient fastApiWebClient;
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

        //FastAPI API 호출
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("prev_diary", exampleDiary);

        Map<String, Object> fastApiResponse = fastApiWebClient.post()
                .uri("/api/v1/prompt_test")
                .bodyValue(requestBody)
                .retrieve()
                .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                        resp -> resp.bodyToMono(String.class).flatMap(body -> {
                            log.error("FastAPI error [{}]: {}", resp.statusCode(), body);
                            return Mono.error(new RuntimeException("FastAPI 호출 실패"));
                        })
                )
                .bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {
                })
                .block();

        // FastAPi 응답 파싱
        String prompt = fastApiResponse.get("prompt").toString();
        String example = fastApiResponse.get("example").toString();

        // 기존 커스텀 테마 확인 후 업데이트
        DiaryThema existingThema = user.getCustomThema();
        if (existingThema == null) {
            // 기존 커스텀 테마가 없는 경우 생성자 호출
            existingThema = DiaryThema.creatDiaryThema(
                    user, "내 커스텀 테마", "내 일기를 기반으로 작성된 커스텀 테마입니다.", prompt, example
            );
        } // 있으면, 갱신
        else {
            existingThema.setPrompt(prompt);
            existingThema.setExample(example);
        }

        DiaryThema saved = diaryThemaRepository.save(existingThema);
        user.setCustomThema(existingThema);
        userRepository.save(user);

        return new DiaryThemaCreateResponse(saved.getId(), saved.getExample());
    }

    @Transactional(readOnly = false)
    public void updateCustomThemaFromDiary(Long userId, Long diaryId) {
        //1. 일기 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다."));

        //2. 유저 조회 및 확인
        if (!diary.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryAccessException("일기에 접근권한이 없습니다.");
        }

        //3. 프롬프트 추출
        String prompt = diary.getPrompt();
        if (prompt == null || prompt.isBlank() || prompt.isEmpty()) {
            throw new InvalidPromptException("유효하지않은 프롬프트입니다,");
        }

        //4. 내 커스텀 테마 조회
        DiaryThema thema = diaryThemaRepository.findByUserId(userId)
                .orElseThrow(() -> new DiaryThemaNotFoundException("일기 테마를 찾을 수 없습니다."));
        System.out.println("내 커스텀 테마:"+thema.getId()+ ", user:" +thema.getUser().getId());
        //5. 프롬프트 갱신
        thema.setPrompt(prompt);
        diaryThemaRepository.save(thema);

        //6. 내 usingThema로 변경
        User user = diary.getUser();
        user.setUsingThema(thema);
        userRepository.save(user);

        System.out.println("갱신 완료 : "+user.getUsingThema());
    }

    /**
     * 사용중인 테마 조회 메소드
     */
    public UsingDiaryThemaResponseDTO getUsingThema(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        DiaryThema thema = user.getUsingThema();
        Long themaId = (thema != null) ? thema.getId() : null;
        log.debug("{} user의 사용중인 themaId : {}",userId,themaId);
        return new UsingDiaryThemaResponseDTO(themaId);
    }
}
