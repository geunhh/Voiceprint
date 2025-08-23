package com.voiceprint.backend.chat.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 임시다이어리 업데이트 서비스 - 컨트롤러 DTO
 */
@Getter
@AllArgsConstructor
public class UpdateDiaryResult {
    private boolean changed;
    private TempDiaryResponseDTO diary;

}
