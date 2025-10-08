package com.voiceprint.backend.group.application.port.in.groupinvite;

import com.voiceprint.backend.notification.domain.Notification;

import java.util.List;

public interface SaveAndSendNewMemberUseCase {
    List<Notification> saveAndSendNewMember(Integer groupId, Integer userId);
}
