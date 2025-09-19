package com.voiceprint.backend.diary.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiarySummaryResponseDTO;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.application.port.in.DiaryUseCase;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class DiaryService implements DiaryUseCase {

    private final DiaryRepositoryPort diaryRepositoryPort;
    private final ObjectMapper objectMapper; // JSON 파싱을 위해 주입

    @Override
    public DiaryDetailResponseDTO getDiaryDetail(Integer userId, Integer diaryId) {
        Diary diary = diaryRepositoryPort.findDetailById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다.")); // Custom Exception으로 교체

        if (!diary.getUserId().equals(userId.intValue())) {
            throw new RuntimeException("권한이 없습니다."); // Custom Exception으로 교체
        }
        DiaryDetailResponseDTO dto = DiaryDetailResponseDTO.builder()
                .diaryId(diary.getId())
                .title(diary.getTitle())
                .content(diary.getContent())
                .emotion(null)
                .createdAt(diary.getCreatedAt().toString())
                .authorNickname(null)
                .thumbnail(diary.getThumbnail())
                .build();
        log.info("dto : {}", dto);
//        return new DiaryDetailResponseDTO(diary.getId(), diary.getTitle(), diary.getContent(), null, diary.getCreatedAt().toString(), null, diary.getThumbnail());
        return dto;
    }

    @Override
    public DiaryListWithCursorDTO getUserDiaries(Integer userId, Integer cursor, Integer size) {
        // 페이지네이션 로직은 Application Service의 책임
        List<Diary> diaries = diaryRepositoryPort.findMyDiaries(userId, cursor, size + 1);
        boolean hasNext = diaries.size() > size;
        if (hasNext) {
            diaries = diaries.subList(0, size);
        }
        Integer nextCursor = hasNext ? diaries.get(diaries.size() - 1).getId() : null;

        List<DiarySummaryResponseDTO> responseDtos = diaries.stream()
            .map(d -> new DiarySummaryResponseDTO(d.getId(), d.getTitle(), d.getContent(), null, d.getCreatedAt().toString(), d.getThumbnail()))
            .collect(Collectors.toList());

        return new DiaryListWithCursorDTO(responseDtos, nextCursor);
    }

    @Override
    public DiaryMontlyListDTO getMonthlyDiaries(Integer userId, int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.withDayOfMonth(startDate.lengthOfMonth()).atTime(LocalTime.MAX);

        List<Diary> diaries = diaryRepositoryPort.findByUserIdAndDateRange(userId, start, end);
        
        List<DiarySummaryResponseDTO> dtos = diaries.stream()
            .map(d -> new DiarySummaryResponseDTO(d.getId(), d.getTitle(), d.getContent(), null, d.getCreatedAt().toString(), d.getThumbnail()))
            .collect(Collectors.toList());

        return new DiaryMontlyListDTO(dtos);
    }

    @Override
    public List<ChatMessageResponseDTO> getChatRecordFromDiary(Integer userId, Integer diaryId) {
        Diary diary = diaryRepositoryPort.findDetailById(diaryId)
                .orElseThrow(() -> new RuntimeException("일기를 찾을 수 없습니다."));
        log.info("diary : {}, {}",diary,diary.getMessages());

        if (!diary.getUserId().equals(userId)) {
            throw new RuntimeException("권한이 없습니다.");
        }
        // ChatMessageListConverter로 간소화.
        return diary.getMessages().stream()
                .map(chatMessage -> new ChatMessageResponseDTO(
                        chatMessage.getRole(), chatMessage.getContent()
                ))
                .collect(Collectors.toList());
    }
}