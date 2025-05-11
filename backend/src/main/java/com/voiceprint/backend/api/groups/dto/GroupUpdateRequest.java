package com.voiceprint.backend.api.groups.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GroupUpdateRequest {
    private String name;
    private String description;
    private Boolean isDeleted;
    private Boolean enableAlarm;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private List<DayOfWeek> alarmDays;
    private LocalTime alarmTime;
    private MultipartFile groupImage;  // 이미지 파일
}
