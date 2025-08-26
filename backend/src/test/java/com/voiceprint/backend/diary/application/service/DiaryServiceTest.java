package com.voiceprint.backend.diary.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.diary.adapter.in.web.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.diary.domain.Emotion;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * DiaryService 단위 테스트 ( 헥사고날 아키텍처의 Application Service 레이어)
 * - 외부 의존성(RepositoryPort, ObjectMapper)을 Mockito로 모킹.
 * -> 순수 JVM 환경에서 서비스 흐름 검증
 */
@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryRepositoryPort diaryRepositoryPort;

    @Mock
    private ObjectMapper objectMapper;

    // 테스트 대상
    @InjectMocks
    private DiaryService diaryService;

    // 공통 태스트 데이터
    private Integer testUserId;
    private Integer testDiaryId;
    private Diary testDiary;
    private Emotion testEmotion;

    @BeforeEach
    void setUp() {
        testUserId = 1;
        testDiaryId = 100;
        testEmotion = Emotion.builder().id((byte)1).name("Happy").color("#FF0000").build();

        // 서비스 레이어에서 사용하는 도메인 Diary
        testDiary = Diary.builder()
                .id(testDiaryId)
                .userId(testUserId)
                .title("Test Diary Title")
                .content("Test Diary Content")
                .thumbnail("test_thumb.png")
                .prompt("Test Prompt")
                .messages(Arrays.asList(new ChatMessageResponseDTO("user","hi hi"),
                                        new ChatMessageResponseDTO("assistant","hi there")))
                .isDeleted(false)
                .emotion(testEmotion)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getDiaryDetail_success() {
        // GIVEN: 리포지토리가 일기를 정상 반환
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        // WHEN: 서비스 호출
        DiaryDetailResponseDTO result = diaryService.getDiaryDetail(testUserId, testDiaryId);

        // THEN: 매핑된 결과/필드 검증 + 리포지토리 호출 검증
        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isEqualTo(testDiaryId);
        assertThat(result.getTitle()).isEqualTo("Test Diary Title");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getDiaryDetail_diaryNotFound() {
        // GIVEN: 일기 미존재
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.empty());

        // WHEN & THEN: 서비스가 도메인 예외를 던져야 함
        assertThrows(DiaryNotFoundException.class, () -> {
            diaryService.getDiaryDetail(testUserId, testDiaryId);
        });
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getDiaryDetail_unauthorized() {
        // GIVEN: 다른 사용자가 조회
        Integer anotherUserId = 2;
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        // WHEN & THEN: 권한 불일치 → 런타임 예외(메시지 확인)
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diaryService.getDiaryDetail(anotherUserId, testDiaryId);
        });
        assertThat(exception.getMessage()).isEqualTo("권한이 없습니다.");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getUserDiaries_successWithPagination() {
        // GIVEN: size+1 전략을 쓰는 페이징을 가정
        // - 서비스에 size=2를 넘기면, 내부에서 repo는 3개(size+1)를 요청한다.
        Diary diary1 = Diary.builder().id(101).userId(testUserId).title("D1").content("C1").createdAt(LocalDateTime.now().minusDays(2)).build();
        Diary diary2 = Diary.builder().id(102).userId(testUserId).title("D2").content("C2").createdAt(LocalDateTime.now().minusDays(1)).build();
        Diary diary3 = Diary.builder().id(103).userId(testUserId).title("D3").content("C3").createdAt(LocalDateTime.now()).build();

        // 리포가 3개 반환(정렬: id desc 가정 → 103, 102, 101)
        when(diaryRepositoryPort.findMyDiaries(eq(testUserId), any(), eq(3)))
                .thenReturn(Arrays.asList(diary3, diary2, diary1)); // Ordered by ID desc

        // WHEN: size=2 요청
        DiaryListWithCursorDTO result = diaryService.getUserDiaries(testUserId, null, 2);

        // THEN: 2개만 노출되고(next page 존재), nextCursor는 마지막 노출 항목의 id(=102)
        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(2);
        assertThat(result.getDiaries().get(0).getDiaryId()).isEqualTo(103);
        assertThat(result.getDiaries().get(1).getDiaryId()).isEqualTo(102);
        assertThat(result.getNextCursor()).isEqualTo(102); // nextCursor should be the ID of the last item in the returned list
        verify(diaryRepositoryPort, times(1)).findMyDiaries(eq(testUserId), any(), eq(3));
    }

    @Test
    void getUserDiaries_successWithoutPagination() {
        // GIVEN: 리포가 정확히 size(=2)만 반환 → 다음 페이지 없음
        Diary diary1 = Diary.builder().id(101).userId(testUserId).title("D1").content("C1").createdAt(LocalDateTime.now().minusDays(2)).build();
        Diary diary2 = Diary.builder().id(102).userId(testUserId).title("D2").content("C2").createdAt(LocalDateTime.now().minusDays(1)).build();

        // Mock repository to return exactly requested size
        when(diaryRepositoryPort.findMyDiaries(eq(testUserId), any(), eq(3)))
                .thenReturn(Arrays.asList(diary2, diary1));

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(testUserId, null, 2);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(2);
        assertThat(result.getNextCursor()).isNull(); // No next page
        verify(diaryRepositoryPort, times(1)).findMyDiaries(eq(testUserId), any(), eq(3));
    }

    @Test
    void getUserDiaries_noDiaries() {
        when(diaryRepositoryPort.findMyDiaries(eq(testUserId), any(), anyInt()))
                .thenReturn(Collections.emptyList());

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(testUserId, null, 5);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).isEmpty();
        assertThat(result.getNextCursor()).isNull();
        verify(diaryRepositoryPort, times(1)).findMyDiaries(eq(testUserId), any(), anyInt());
    }

    @Test
    void getMonthlyDiaries_success() {
        Diary diary1 = Diary.builder().id(101).userId(testUserId).title("M1").content("C1").createdAt(LocalDateTime.of(2024, 1, 15, 10, 0)).build();
        Diary diary2 = Diary.builder().id(102).userId(testUserId).title("M2").content("C2").createdAt(LocalDateTime.of(2024, 1, 20, 14, 0)).build();

        when(diaryRepositoryPort.findByUserIdAndDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(diary1, diary2));

        DiaryMontlyListDTO result = diaryService.getMonthlyDiaries(testUserId, 2024, 1);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(2);
        assertThat(result.getDiaries().get(0).getDiaryId()).isEqualTo(101);
        assertThat(result.getDiaries().get(1).getDiaryId()).isEqualTo(102);
        verify(diaryRepositoryPort, times(1)).findByUserIdAndDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getMonthlyDiaries_noDiaries() {
        when(diaryRepositoryPort.findByUserIdAndDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        DiaryMontlyListDTO result = diaryService.getMonthlyDiaries(testUserId, 2024, 2);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).isEmpty();
        verify(diaryRepositoryPort, times(1)).findByUserIdAndDateRange(eq(testUserId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void getChatRecordFromDiary_success() {
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        List<ChatMessageResponseDTO> result = diaryService.getChatRecordFromDiary(testUserId, testDiaryId);

        assertThat(result).isNotNull();
        assertThat(result).isEqualTo(testDiary.getMessages());

        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getChatRecordFromDiary_diaryNotFound() {
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diaryService.getChatRecordFromDiary(testUserId, testDiaryId);
        });
        assertThat(exception.getMessage()).isEqualTo("일기를 찾을 수 없습니다.");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
        verifyNoInteractions(objectMapper); // ObjectMapper should not be called
    }

    @Test
    void getChatRecordFromDiary_unauthorized() {
        Integer anotherUserId = 2;
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diaryService.getChatRecordFromDiary(anotherUserId, testDiaryId);
        });
        assertThat(exception.getMessage()).isEqualTo("권한이 없습니다.");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
        verifyNoInteractions(objectMapper);
    }

}
