package com.voiceprint.backend.api.groups.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class InviteInfoReponseDTO {
    private String groupName;
    private String inviterName;
    private LocalDateTime expiredAt;
    private boolean alreadyJoined;
}
