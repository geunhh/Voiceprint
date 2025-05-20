package com.voiceprint.backend.domain.Entity;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id;

    // 이름 : 불안, 행복, 기쁨, 슬픔 ...
    @Column(length = 10)
    private String name;

    //색상 코드
    @Column(length = 10)
    private String color;

    //== 생성 메서드 ==//
    public Emotion(String content) {
        this.name = name;
    }
}


