package com.voiceprint.backend.group.application.port.in.groupinvite;

import com.voiceprint.backend.group.adapter.in.web.dto.InviteCodeResponseDTO;

public interface CreateInviteUseCase {
    InviteCodeResponseDTO createInvite(Integer groupId, Integer userId);
}
