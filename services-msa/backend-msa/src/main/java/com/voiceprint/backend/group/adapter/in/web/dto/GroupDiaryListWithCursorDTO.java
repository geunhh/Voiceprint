package com.voiceprint.backend.group.adapter.in.web.dto;

import com.voiceprint.backend.diary.adapter.in.web.dto.GroupDiaryResponseDTO;
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