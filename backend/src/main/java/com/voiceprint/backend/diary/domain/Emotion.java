package com.voiceprint.backend.diary.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Emotion {
    private final Byte id;
    private final String name;
    private final String color;

}
