package com.voiceprint.backend.api.groups.dto;

import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupDiaryListWithCursorDTO {
    private List<DiarySummaryResponseDTO> diaries;
    private LocalDateTime nextCursor;
}