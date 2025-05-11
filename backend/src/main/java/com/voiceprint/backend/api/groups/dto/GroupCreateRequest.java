package com.voiceprint.backend.api.groups.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateRequest {
    private String name;
    private String description;
    private MultipartFile groupImage;  // 이미지 파일
    private Boolean enableAlarm;
    private LocalDateTime alarm;
}

