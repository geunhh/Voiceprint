package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.api.diary.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

    public DiaryListWithCursorDTO getUserDiaries(HttpServletRequest request, Long cursor, Integer size) {
        // 유저 정보 추출 및 확인
        Long userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 확인 불가"));


        // 2. size + 1 개 조회 (다음 커서 존재 여부 확인용)
        PageRequest page = PageRequest.of(0,size+1); // (page Num, page Size)

        List<Diary> diaries = diaryRepository.findMyDiaries(userId, cursor, page);

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
        Long nextCursor = hasNext ? diaries.getLast().getId() : null;

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
        Long userId = authService.getUserIdFromRequest(request);
        log.debug("userId : {}",userId);


        // 날짜 초기화
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        log.debug("startDate : {}, endDate : {}",startDate,endDate);

        List<Diary> diaries = diaryRepository.findByUserIdAndDateRange(
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
}
