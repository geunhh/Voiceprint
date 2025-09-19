package com.voiceprint.backend.group.application.port.in.groupinvite;

import com.voiceprint.backend.group.adapter.in.web.dto.InviteInfoReponseDTO;

public interface GetInviteInfoUseCase {
    InviteInfoReponseDTO getInviteInfo(String code, Integer userId);
}
