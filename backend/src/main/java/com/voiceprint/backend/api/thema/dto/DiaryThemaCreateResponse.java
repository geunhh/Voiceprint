package com.voiceprint.backend.api.thema.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DiaryThemaCreateResponse {
    private Integer themaId;
    private String example;
}
