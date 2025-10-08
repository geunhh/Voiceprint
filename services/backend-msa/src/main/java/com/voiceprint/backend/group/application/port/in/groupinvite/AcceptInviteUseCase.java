package com.voiceprint.backend.group.application.port.in.groupinvite;

import com.voiceprint.backend.group.adapter.in.web.dto.InviteAcceptResponseDTO;

public interface AcceptInviteUseCase {
    InviteAcceptResponseDTO acceptInvite(String inviteCode, Integer userId);
}
