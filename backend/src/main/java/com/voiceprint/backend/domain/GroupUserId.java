package com.voiceprint.backend.domain;

import lombok.*;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Embeddable // 복합키 정의
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode  //equals()와 hashCode() 메서드를 자동으로 생성
public class GroupUserId implements Serializable {

    private Long userId;
    private Long groupId;

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GroupUserId that = (GroupUserId) obj;
        return Objects.equals(userId, that.userId) && Objects.equals(groupId, that.groupId);
    }
}

