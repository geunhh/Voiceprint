package com.voiceprint.backend.diary.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.chat.domain.ChatMessage;
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

@ExtendWith(MockitoExtension.class)
class DiaryServiceTest {

    @Mock
    private DiaryRepositoryPort diaryRepositoryPort;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private DiaryService diaryService;

    private Integer testUserId;
    private Integer testDiaryId;
    private Diary testDiary;
    private Emotion testEmotion;

    @BeforeEach
    void setUp() {
        testUserId = 1;
        testDiaryId = 100;
        testEmotion = Emotion.builder().id((byte)1).name("Happy").color("#FF0000").build();

        testDiary = Diary.builder()
                .id(testDiaryId)
                .userId(testUserId)
                .title("Test Diary Title")
                .content("Test Diary Content")
                .thumbnail("test_thumb.png")
                .prompt("Test Prompt")
                .messages(Arrays.asList(ChatMessage.builder().role("user").content("hi hi").build(),
                                        ChatMessage.builder().role("assistant").content("hi there").build()))
                .isDeleted(false)
                .emotion(testEmotion)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getDiaryDetail_success() {
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        DiaryDetailResponseDTO result = diaryService.getDiaryDetail(testUserId, testDiaryId);

        assertThat(result).isNotNull();
        assertThat(result.getDiaryId()).isEqualTo(testDiaryId);
        assertThat(result.getTitle()).isEqualTo("Test Diary Title");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getDiaryDetail_diaryNotFound() {
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.empty());

        assertThrows(DiaryNotFoundException.class, () -> {
            diaryService.getDiaryDetail(testUserId, testDiaryId);
        });
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getDiaryDetail_unauthorized() {
        Integer anotherUserId = 2;
        when(diaryRepositoryPort.findDetailById(testDiaryId)).thenReturn(Optional.of(testDiary));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            diaryService.getDiaryDetail(anotherUserId, testDiaryId);
        });
        assertThat(exception.getMessage()).isEqualTo("권한이 없습니다.");
        verify(diaryRepositoryPort, times(1)).findDetailById(testDiaryId);
    }

    @Test
    void getUserDiaries_successWithPagination() {
        Diary diary1 = Diary.builder().id(101).userId(testUserId).title("D1").content("C1").createdAt(LocalDateTime.now().minusDays(2)).build();
        Diary diary2 = Diary.builder().id(102).userId(testUserId).title("D2").content("C2").createdAt(LocalDateTime.now().minusDays(1)).build();
        Diary diary3 = Diary.builder().id(103).userId(testUserId).title("D3").content("C3").createdAt(LocalDateTime.now()).build();

        when(diaryRepositoryPort.findMyDiaries(eq(testUserId), any(), eq(3)))
                .thenReturn(Arrays.asList(diary3, diary2, diary1));

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(testUserId, null, 2);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(2);
        assertThat(result.getDiaries().get(0).getDiaryId()).isEqualTo(103);
        assertThat(result.getDiaries().get(1).getDiaryId()).isEqualTo(102);
        assertThat(result.getNextCursor()).isEqualTo(102);
        verify(diaryRepositoryPort, times(1)).findMyDiaries(eq(testUserId), any(), eq(3));
    }

    @Test
    void getUserDiaries_successWithoutPagination() {
        Diary diary1 = Diary.builder().id(101).userId(testUserId).title("D1").content("C1").createdAt(LocalDateTime.now().minusDays(2)).build();
        Diary diary2 = Diary.builder().id(102).userId(testUserId).title("D2").content("C2").createdAt(LocalDateTime.now().minusDays(1)).build();

        when(diaryRepositoryPort.findMyDiaries(eq(testUserId), any(), eq(3)))
                .thenReturn(Arrays.asList(diary2, diary1));

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(testUserId, null, 2);

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(2);
        assertThat(result.getNextCursor()).isNull();
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
        assertThat(result).hasSize(testDiary.getMessages().size());
        assertThat(result.get(0).getRole()).isEqualTo(testDiary.getMessages().get(0).getRole());
        assertThat(result.get(0).getContent()).isEqualTo(testDiary.getMessages().get(0).getContent());

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
        verifyNoInteractions(objectMapper);
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
