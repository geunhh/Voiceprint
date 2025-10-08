package com.voiceprint.backend.diary.adapter.in.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GroupDiaryResponseDTO {
    private Integer groupId;
    private Integer diaryId;
    private String title;
    private String content;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Seoul")
    private String createdAt;
    private String profileUrl;
    private String nickname;

}
