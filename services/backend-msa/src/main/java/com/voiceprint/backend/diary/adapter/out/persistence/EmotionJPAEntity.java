package com.voiceprint.backend.diary.adapter.out.persistence;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity(name = "Emotion")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmotionJPAEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Byte id;

    // 이름 : 불안, 행복, 기쁨, 슬픔 ...
    @Column(length = 10)
    private String name;

    //색상 코드
    @Column(length = 10)
    private String color;

    //== 생성 메서드 ==//

    public static EmotionJPAEntity create(String name, String color) {
        EmotionJPAEntity e = new EmotionJPAEntity();
        e.name = name;
        e.color = color;
        return e;
    }
}


