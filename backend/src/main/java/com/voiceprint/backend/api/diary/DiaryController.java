package com.voiceprint.backend.api.diary;

import com.voiceprint.backend.api.chat.dto.ChatMessageResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryDetailResponseDTO;
import com.voiceprint.backend.api.diary.dto.DiaryListWithCursorDTO;
import com.voiceprint.backend.api.diary.dto.DiaryMontlyListDTO;
import com.voiceprint.backend.api.diary.dto.SharedDiaryRequest;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.Entity.Notification;
import com.voiceprint.backend.service.alarm.NotificationService;
import com.voiceprint.backend.service.auth.AuthService;
import com.voiceprint.backend.service.diary.DiaryService;
import com.voiceprint.backend.service.diary.GroupDiaryService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/diaries")
public class DiaryController {
    private final DiaryService diaryService;
    private final AuthService authService;
    private final GroupDiaryService groupDiaryService;
    private final NotificationService notificationService;

    /**
     * diaryId를 기반으로 일기 상세정보를 조회하는 API
     */
    @GetMapping("/diary/{diaryId}")
    public ResponseEntity<CommonResponse<DiaryDetailResponseDTO>> getDiaryDetail(
            @PathVariable Long diaryId,
            HttpServletRequest request    ){
        log.info("다이어리 상세보기 호출 diaryId: {}",diaryId);

        DiaryDetailResponseDTO response = diaryService.getDiaryDetail(request, diaryId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "조회 성공", response
        ));
    }

    @GetMapping("/diary/{diaryId}/chat")
    public ResponseEntity<CommonResponse<List<ChatMessageResponseDTO>>> getDiaryChat(
            @PathVariable Long diaryId,
            HttpServletRequest request    ) {
        log.info("id:{ }  다이어리 채팅 내역 조회 API 호출");

        List<ChatMessageResponseDTO>resposne = diaryService.getChatRecordFromDiary(request,diaryId);

        return ResponseEntity.ok(new CommonResponse<>(
                200, "채팅 내역 조회 성공", resposne
        ));


    }

    /**
     * 내가 작성한 일기를 조회하는 API
     * @param size
     * @param request
     */
    @GetMapping("/me/all")
    public ResponseEntity<CommonResponse<DiaryListWithCursorDTO>> getMyDiaries(
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "7") int size,
            HttpServletRequest request
    ) {

        DiaryListWithCursorDTO result = diaryService.getUserDiaries(request, cursor, size);
        return ResponseEntity.ok(new CommonResponse<>(200, "성공", result));
    }

    @GetMapping("/monthly")
    public ResponseEntity<CommonResponse<DiaryMontlyListDTO>> getMonthlyDiaries(
            @RequestParam int year,
            @RequestParam int month,
            HttpServletRequest request
    ) {

        DiaryMontlyListDTO response = diaryService.getMonthlyDiaries(request, year, month);
        return ResponseEntity.ok(new CommonResponse<>(
                200, "조회 성공", response
        ));
    }

    /**
     * 일기를 그룹에 공유하고 해당 그룹의 유저들에게 알림을 보내는 API
     */
    @PostMapping("/shared/{diaryId}")
    public ResponseEntity<CommonResponse<String>> shareDiary(
            @PathVariable Long diaryId,
            @RequestBody SharedDiaryRequest request,
            HttpServletRequest httpRequest) {

        Long userId = authService.getUserIdFromRequest(httpRequest);
//        Long userId = 1L;
        List<Notification> notifications = groupDiaryService.saveSharedDiary(diaryId, userId, request.getGroupIds());

        notificationService.publishAllNotifications(notifications);

        return ResponseEntity.ok(new CommonResponse<>(200,"다이어리 공유 성공",null));
    }
}
