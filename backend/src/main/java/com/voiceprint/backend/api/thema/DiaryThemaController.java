package com.voiceprint.backend.api.thema;

import com.voiceprint.backend.api.thema.dto.DiaryThemaListResponseDTO;
import com.voiceprint.backend.common.dto.CommonResponse;
import com.voiceprint.backend.domain.thema.DiaryThemaRepository;
import com.voiceprint.backend.service.thema.DiaryThemaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/thema")
public class DiaryThemaController {

    private final DiaryThemaRepository diaryThemaRepository;
    private final DiaryThemaService diaryThemaService;

    @GetMapping("/all")
    public ResponseEntity<CommonResponse<DiaryThemaListResponseDTO>> getThmeas(
            HttpServletRequest request    ) {
        Long userId = 1L;
        DiaryThemaListResponseDTO response = diaryThemaService.getThemasForUser(userId);
        return ResponseEntity.ok(
                new CommonResponse<>(200,"일기 테마 조회 성공",response));
    }
}
