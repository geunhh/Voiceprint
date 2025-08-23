package com.voiceprint.backend.service.diary;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiarySummaryResponseDTO;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.global.exception.diary.UnauthorizedDiaryAccessException;
import com.voiceprint.backend.domain.Entity.DiaryEntity;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

//@Service // TODO: 제거 대상
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    public DiaryDetailResponseDTO getDiaryDetail(HttpServletRequest request, Integer diaryId) {
        // 유저 정보 추출 및 확인
        Integer userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);

        DiaryEntity diaryEntity = diaryRepository.findDetailById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("다이어리 정보가 없습니다."));

        // 일기의 user FK와 비교
        if (!diaryEntity.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryAccessException("다이어리의 userId와 일치하지 않습니다.");
        }

        return new DiaryDetailResponseDTO(
                diaryEntity.getId(),
                diaryEntity.getTitle(),
                diaryEntity.getContent(),
                diaryEntity.getEmotion() != null ? diaryEntity.getEmotion().getName() : null,
                diaryEntity.getCreatedAt().toString(),
                diaryEntity.getUser().getNickname(),
                diaryEntity.getThumbnail() != null ? diaryEntity.getThumbnail() : null
        );
    }

    public DiaryListWithCursorDTO getUserDiaries(HttpServletRequest request, Integer cursor, Integer size) {
        // 유저 정보 추출 및 확인
        Integer userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);

        // 2. size + 1 개 조회 (다음 커서 존재 여부 확인용)
        PageRequest page = PageRequest.of(0,size+1); // (page Num, page Size)

        List<DiaryEntity> diaries = diaryRepository.findMyDiaries(userId, cursor, page);

        // 다음 페이지가 있다면?? : size보다 크면 있음.
        boolean hasNext = diaries.size() > size;

        // 실제 반환할 목록은 size 개
        if (hasNext) {
            diaries = diaries.subList(0,size);
            log.info("다음 게시물이 존재합니다.");
        } else {
            log.info("마지막 게시물입니다.");
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

    public DiaryMontlyListDTO getMonthlyDiaries(HttpServletRequest request, int year, int month) {
        // 유저 정보 추출 및 확인
        Integer userId = authService.getUserIdFromRequest(request);

        log.debug("userId : {}",userId);


        // 날짜 초기화
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        log.debug("startDate : {}, endDate : {}",startDate,endDate);

        List<DiaryEntity> diaries = diaryRepository.findByUserIdAndDateRange(
                userId,
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX) );

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

    public List<ChatMessageResponseDTO> getChatRecordFromDiary(HttpServletRequest request, Integer diaryId) {
        // 유저 정보 조회
        Integer userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);

        // 일기 정보 조회
        DiaryEntity diaryEntity = diaryRepository.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("해당 Id의 Diary를 찾을 수 없습니다."));

        // 일기의 유저id와 사용자의 id가 일치하는가??
        if (!diaryEntity.getUser().getId().equals(userId)) {
            log.debug("유저 id : {} 와 일기의 유저 id : {} 가 일치하지 않습니다.",userId, diaryEntity.getUser().getId());
            throw new UnauthorizedDiaryAccessException("diary에 권한이 없습니다.");
        }
        log.debug("일기의 userID와 일치합니다. {}", diaryEntity.getUser().getId());

        // messsages -> List로 매핑하기.
        String messagesJson = diaryEntity.getMessages();
        System.out.println("messages : "+messagesJson);

        ObjectMapper mapper = new ObjectMapper();
        List<ChatMessageResponseDTO> result;

        try {
            result = mapper.readValue(messagesJson, new TypeReference<List<ChatMessageResponseDTO>>() {
            });
        }catch (JsonProcessingException e ) {
            log.error("메시지 json 파싱 실패. {}",e.getMessage());
            throw new RuntimeException("채팅 메시지 파싱 실패",e);
        }

        return result;
    }
}
