package com.voiceprint.backend.diary.application.service;

import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaCreateResponse;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaListResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.DiaryThemaResponse;
import com.voiceprint.backend.diary.adapter.in.web.dto.thema.UsingDiaryThemaResponseDTO;
import com.voiceprint.backend.diary.application.port.in.DiaryThemaUseCase;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.diary.application.port.out.DiaryThemaRepositoryPort;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.domain.DiaryThema;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.global.exception.diary.InvalidPromptException;
import com.voiceprint.backend.global.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.global.exception.thema.ThemaNotFoundExceiption;
import com.voiceprint.backend.global.exception.thema.UnauthorizedThemaAccessException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DiaryThemaService  implements DiaryThemaUseCase {
    private final DiaryThemaRepositoryPort diaryThemaRepository;
    private final UserRepositoryPort userRepository;
    private final DiaryRepositoryPort diaryRepository;
    private final WebClient fastApiWebClient;

    @Transactional(readOnly = true)
    @Override
    public DiaryThemaListResponseDTO getThemasForUser(Integer userId) {

        List<DiaryThema> themas = diaryThemaRepository.findByUserIdOrDefault(userId);

        // 기본 테마
        List<DiaryThemaResponse> defaultThemas = themas.stream()
                .filter(thema -> thema.getUser() == null)
                .map(DiaryThemaResponse::fromDomain)
                .collect(Collectors.toList());

        // 사용자 커스텀 테마
        List<DiaryThemaResponse> customThemas = themas.stream()
                .filter(thema -> thema.getUser() != null)
                .map(DiaryThemaResponse::fromDomain)
                .collect(Collectors.toList());

        return new DiaryThemaListResponseDTO(defaultThemas, customThemas);
    }

    @Override
    public void selectThema(Integer userId, Integer themaId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저정보X"));
        log.info("user:{}",user);
        DiaryThema thema = diaryThemaRepository.findById(themaId)
                .orElseThrow(() -> new ThemaNotFoundExceiption("테마 정보 없음"));
        log.info("thema_id:{}",thema.getId());
        // 기본테마이거나 아닐 경우, 테마에 권한이 있는지 확인.
        if (thema.getUser() != null && !thema.getUser().getId().equals(userId)) {
            log.info("여기인가?");
            throw new UnauthorizedThemaAccessException("권한이 없는 테마입니다.");
        }

        // 유저 테마 갱신
        User updatedUser = user.withUsingThema(thema);
        log.info("user:{}",updatedUser.getUsingThema());
        userRepository.save(updatedUser);

    }

    /**
     * Todo: 곧 사라질 예정
     */
    @Override
    public DiaryThemaCreateResponse createCustomThema(Integer userId, String exampleDiary) {
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
            existingThema = DiaryThema.builder()
                    .user(user)
                    .title("내 커스텀 테마")
                    .description("내 일기를 기반으로 작성된 커스텀 테마입니다.")
                    .prompt(prompt)
                    .example(example)
                    .build();
        } // 있으면, 갱신
        else {
            existingThema = existingThema.withPromptAndExample(prompt, example);
        }

        DiaryThema saved = diaryThemaRepository.save(existingThema);
        User updatedUser = user.withCustomThema(existingThema);
        userRepository.save(updatedUser);

        return new DiaryThemaCreateResponse(saved.getId(), saved.getExample());
    }

    @Transactional
    @Override
    public void updateCustomThemaFromDiary(Integer userId, Integer diaryId) {
        //1. 일기 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다."));

        //2. 유저 조회 및 확인
        if (!diary.getUserId().equals(userId)) {
            throw new UnauthorizedDiaryAccessException("일기에 접근권한이 없습니다.");
        }

        //3. 프롬프트 추출
        String prompt = diary.getPrompt();

        if (prompt == null || prompt.isBlank() || prompt.isEmpty()) {
            throw new InvalidPromptException("유효하지않은 프롬프트입니다,");
        }

        log.debug("customPrompt : {}", prompt);

        User user = userRepository.findById(userId).orElseThrow(
                () -> new UserNotFoundException("유저 정보 없음"));

        //4. 내 커스텀 테마 조회
        DiaryThema thema = diaryThemaRepository.findByUserId(userId).orElseGet(() ->
            DiaryThema.builder()
                    .user(user)
                    .title("내 커스텀 테마")
                    .description("일기를 기반으로 생성된 커스텀 테마입니다.")
                    .prompt(prompt)
                    .example(diary.getContent())
                    .build()
        );
        log.debug("내 커스텀 테마: {}, user: {} ",thema.getId(), thema.getUser().getId());

        //5. 프롬프트 갱신
        DiaryThema updatedThema = thema.withPrompt(prompt);
        diaryThemaRepository.save(updatedThema);

        //6. 내 usingThema로 변경
        User updatedUser = user.withUsingThema(updatedThema);
        userRepository.save(updatedUser);

        log.info("갱신 완료 : {}",updatedUser.getUsingThema());
    }

    /**
     * 사용중인 테마 조회 메소드
     */
    @Transactional(readOnly = true)
    @Override
    public UsingDiaryThemaResponseDTO getUsingThema(Integer userId) {
        User user = userRepository.findUserWithUsingThema(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        DiaryThema thema = user.getUsingThema();
        Integer themaId = (thema != null) ? thema.getId() : null;
        log.debug("{} user의 사용중인 themaId : {}",userId,themaId);
        return new UsingDiaryThemaResponseDTO(themaId);
    }
}
