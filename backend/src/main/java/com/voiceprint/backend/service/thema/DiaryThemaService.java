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
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.Diary;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.domain.Entity.DiaryThema;
import com.voiceprint.backend.domain.Repository.DiaryThemaRepository;
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

    /**
     * 사용자에게 제공되는 테마 목록을 조회합니다.
     * - 기본 테마(공용) + 사용자가 만든 커스텀 테마로 구분하여 반환
     */
    @Transactional(readOnly = true)
    public DiaryThemaListResponseDTO getThemasForUser(Integer userId) {

        List<DiaryThema> themas = diaryThemaRepository.findByUserIdOrDefault(userId);

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

    /**
     * 사용자가 사용할 테마를 선택합니다.
     * - 기본 테마는 누구나 선택 가능
     * - 커스텀 테마는 본인의 테마만 선택 가능
     */
    public void selectThema(Integer userId, Integer themaId) {
        User user = getUserOrThrow(userId);

        DiaryThema thema = diaryThemaRepository.findById(themaId)
                .orElseThrow(() -> new ThemaNotFoundExceiption("테마 정보 없음"));

        // 기본테마이거나 아닐 경우, 테마에 권한이 있는지 확인.
        if (thema.getUser() != null && !thema.getUser().getId().equals(userId)) {
            throw new UnauthorizedThemaAccessException("권한이 없는 테마입니다.");
        }

        // 유저 테마 갱신
        user.setUsingThema(thema);

    }

    /**
     * FastAPI를 호출하여 사용자의 예시 일기를 기반으로 커스텀 테마를 생성하거나 갱신합니다.
     */
    public DiaryThemaCreateResponse createCustomThema(Integer userId, String exampleDiary) {
        // 유저 정보 확인
        User user = getUserOrThrow(userId);

        // FastAPI API 호출
        Map<String, String> requestBody = Map.of("prev_diary",exampleDiary);
        Map<String, Object> fastApiResponse = callPromptApi(requestBody);

        // FastAPi 응답 파싱
        String prompt = fastApiResponse.get("prompt").toString();
        String example = fastApiResponse.get("example").toString();
        log.debug("response example : {}",example);
        log.debug("response prompt : {}",prompt);

        // 커스텀 테마 생성 또는 갱신
        DiaryThema existingThema = user.getCustomThema();
        if (existingThema == null) {
            existingThema = DiaryThema.creatDiaryThema(
                    user, "내 커스텀 테마", "내 일기를 기반으로 작성된 커스텀 테마입니다.", prompt, example);
        } else {
            existingThema.setPrompt(prompt);
            existingThema.setExample(example);
        }

        DiaryThema saved = diaryThemaRepository.save(existingThema);
        user.setCustomThema(existingThema);
        userRepository.save(user);

        return new DiaryThemaCreateResponse(saved.getId(), saved.getExample());
    }

    /**
     * 특정 일기의 프롬프트를 기반으로 커스텀 테마를 갱신합니다.
     * - 기존 테마가 없으면 생성, 있으면 prompt만 갱신
     */
    public void updateCustomThemaFromDiary(Integer userId, Integer diaryId) {
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

        log.debug("customPrompt : {}", prompt);

        //4. 내 커스텀 테마 조회
        DiaryThema thema = diaryThemaRepository.findByUserId(userId).orElseGet(() -> {
            User user = getUserOrThrow(userId);
            return DiaryThema.creatDiaryThema(
                        user,
                        "내 커스텀 테마",
                        "일기를 기반으로 생성된 커스텀 테마입니다.",
                        prompt,
                        diary.getContent() // 예시로 넣은 내용입니다. 필요에 맞게 수정하세요.

                );
        });
        log.debug("내 커스텀 테마: {}, user: {} ",thema.getId(), thema.getUser().getId());

        //5. 프롬프트 갱신
        thema.setPrompt(prompt);
        diaryThemaRepository.save(thema);

        //6. 내 usingThema로 변경
        User user = diary.getUser();
        user.setUsingThema(thema);
        userRepository.save(user);

        log.debug("갱신 완료 : {} ",user.getUsingThema());
    }

    /**
     * 사용자가 현재 선택한 테마를 조회합니다.
     */
    @Transactional(readOnly = true)
    public UsingDiaryThemaResponseDTO getUsingThema(Integer userId) {
        User user = userRepository.findUserWithUsingThema(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        DiaryThema thema = user.getUsingThema();
        Integer themaId = (thema != null) ? thema.getId() : null;
        log.debug("{} user의 사용중인 themaId : {}",userId,themaId);
        return new UsingDiaryThemaResponseDTO(themaId);
    }


    //========= 유틸 메서드 ===========//

    private User getUserOrThrow(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(()-> new UserNotFoundException("유저 정보 없음"));
        return user;
    }

    /**
     * FastAPI를 호출하여 prompt 및 예시를 받아옵니다.
     * 오류 발생 시 로그 출력 후 RuntimeException으로 래핑
     */
    private Map<String, Object> callPromptApi(Map<String, String> requestBody) {
        return fastApiWebClient.post()
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
    }
}
