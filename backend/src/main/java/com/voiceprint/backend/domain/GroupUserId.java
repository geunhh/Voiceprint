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

}

