package com.voiceprint.backend.group.adapter.in.web.dto;

import com.voiceprint.backend.group.domain.Group; // Import Group domain object
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class MyGroupResponse {
    private Integer groupId;
    private String groupName;
    private String groupImageUrl;
    private Integer memberCount;
    private List<String> memberProfileImages; // 최대 3명
    private LocalDateTime createdAt;

    public static MyGroupResponse from(Group group, Integer memberCount, List<String> memberProfileImages) {
        return new MyGroupResponse(
                group.getId(),
                group.getName(),
                group.getGroupImage(), // Assuming groupImage is the URL
                memberCount,
                memberProfileImages,
                group.getCreatedAt()
        );
    }
}