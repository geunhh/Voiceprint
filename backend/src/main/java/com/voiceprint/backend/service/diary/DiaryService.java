package com.voiceprint.backend.service.diary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.api.chat.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.api.diary.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
import com.voiceprint.backend.common.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.Diary;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 단일 일기 상세 조회
     */
    public DiaryDetailResponseDTO getDiaryDetail(HttpServletRequest request, Integer diaryId) {
        // 유저 정보 추출 및 확인
        Integer userId = extractUserId(request);

        Diary diary = diaryRepository.findDetailById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다."));

        // 일기의 user FK와 비교
        validateOwnership(userId, diary.getUser().getId());

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

    /**
     * 무한 스크롤 기반 일기 목록 조회
     */
    public DiaryListWithCursorDTO getUserDiaries(HttpServletRequest request, Integer cursor, Integer size) {
        // 유저 정보 추출 및 확인
        Integer userId = extractUserId(request);

        // 2. size + 1 개 조회 (다음 커서 존재 여부 확인용)
        PageRequest page = PageRequest.of(0,size+1); // (page Num, page Size)

        List<Diary> diaries = diaryRepository.findMyDiaries(userId, cursor, page);
        boolean hasNext = diaries.size() > size; // 다음 페이지가 있다면?? : size보다 크면 있음.

        // 실제 반환할 목록은 size 개
        if (hasNext) {
            diaries = diaries.subList(0, size);
            log.debug("다음 페이지 있음");
        } else {
            log.debug("마지막 페이지입니다.");
        }

        //nextCursor 설정 : 없으면 null
        Integer nextCursor = hasNext ? diaries.getLast().getId() : null;

        List<DiarySummaryResponseDTO> response = diaries.stream()
                .map(d -> new DiarySummaryResponseDTO(
                        d.getId(),
                        d.getTitle(),
                        d.getContent(),
                        d.getEmotion() != null ? d.getEmotion().getName() : null,
                        d.getCreatedAt().toString(),
                        d.getThumbnail()
                )).toList();

        return new DiaryListWithCursorDTO(response, nextCursor);
    }


    /**
     * 월별 일기 목록 조회
     */
    public DiaryMontlyListDTO getMonthlyDiaries(HttpServletRequest request, int year, int month) {
        // 유저 정보 추출 및 확인
        Integer userId = extractUserId(request);

        // 날짜 초기화
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        log.debug("startDate : {}, endDate : {}",startDate, endDate);

        List<Diary> diaries = diaryRepository.findByUserIdAndDateRange(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX)
        );

        log.debug("diaries : {}",diaries);

        List<DiarySummaryResponseDTO> result = diaries.stream()
                .map(d -> new DiarySummaryResponseDTO(
                        d.getId(),
                        d.getTitle(),
                        d.getContent(),
                        d.getEmotion() != null? d.getEmotion().getName() : null,
                        d.getCreatedAt().toString(),
                        null
                )).toList();

        return new DiaryMontlyListDTO(result);
    }

    /**
     * 일기에 포함된 채팅 메시지 조회
     */
    public List<ChatMessageResponseDTO> getChatRecordFromDiary(HttpServletRequest request, Integer diaryId) {
        // 유저 정보 조회
        Integer userId = extractUserId(request);

        // 일기 정보 조회
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("해당 Id의 Diary를 찾을 수 없습니다."));

        validateOwnership(userId, diary.getUser().getId());
        return parseMessages(diary.getMessages());
    }

    private List<ChatMessageResponseDTO> parseMessages(String messagesJson) {
        try {
            return  objectMapper.readValue(messagesJson, new TypeReference<>() {});
        }catch (JsonProcessingException e ) {
            log.error("메시지 json 파싱 실패. {}",e.getMessage());
            throw new RuntimeException("채팅 메시지 파싱 실패",e);
        }

    }

    // == 헬퍼 메서드
    private void validateOwnership(Integer requestUserId, Integer diaryOwnerId) {
        if (!requestUserId.equals(diaryOwnerId)) {
            throw new UnauthorizedDiaryAccessException("다이어리에 접근할 권한이 없습니다.");
        }
    }

    private Integer extractUserId(HttpServletRequest request) {
        Integer userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);
        return userId;
    }
}
