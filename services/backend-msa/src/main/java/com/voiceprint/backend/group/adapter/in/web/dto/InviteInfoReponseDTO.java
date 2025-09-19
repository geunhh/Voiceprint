package com.voiceprint.backend.group.adapter.in.web.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InviteInfoReponseDTO {
    private String groupName;
    private String groupImage;
    private LocalDateTime expiredAt;
    private boolean alreadyJoined;
}
