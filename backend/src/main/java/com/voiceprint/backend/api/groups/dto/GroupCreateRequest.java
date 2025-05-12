package com.voiceprint.backend.api.groups.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private String name;
    private String description;
    private MultipartFile groupImage;  // 이미지 파일
    private Boolean enableAlarm;
    private List<DayOfWeek> alarmDays;  // 요일 목록 (문자열 형태로 받음)
    private LocalTime alarmTime;     // 알림 시간
}

