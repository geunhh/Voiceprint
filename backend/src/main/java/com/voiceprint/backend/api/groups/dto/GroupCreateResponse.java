package com.voiceprint.backend.api.groups.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GroupCreateResponse {
    private Long groupId;
    private String name;
    private String description;
    private String groupImage;
    private String role;
}
