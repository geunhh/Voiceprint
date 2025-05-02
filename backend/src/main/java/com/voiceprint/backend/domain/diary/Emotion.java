package com.voiceprint.backend.domain.diary;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Emotion {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id;

    private String content;

    //== 생성 메서드 ==//
    public Emotion(String content) {
        this.content = content;
    }
}


