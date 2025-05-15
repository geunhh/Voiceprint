package com.voiceprint.backend.api.groups.dto;

import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
import com.voiceprint.backend.api.diary.dto.GroupDiaryResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GroupDiaryListWithCursorDTO {
    private List<GroupDiaryResponseDTO> diaries;
    private LocalDateTime nextCursor;
}