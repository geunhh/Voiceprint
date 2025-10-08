package com.voiceprint.backend.group.application.port.in.groupuser;

import com.voiceprint.backend.global.dto.CommonResponse;
import org.springframework.http.ResponseEntity;

public interface PromoteToAdminUseCase {
    ResponseEntity<CommonResponse<String>> promoteToAdmin(Integer groupId, Integer currentAdminId, Integer newAdminUserId);
}
