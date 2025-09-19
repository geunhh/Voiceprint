package com.voiceprint.backend.group.adapter.in.web.dto;

import com.voiceprint.backend.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInfoDTO {
    private Integer id;
    private String profileImageUrl;
    private String nickname;

    public static UserInfoDTO from(User user, String profileImageUrl) {
        return new UserInfoDTO(
                user.getId(),
                profileImageUrl,
                user.getNickname()
        );
    }
}
