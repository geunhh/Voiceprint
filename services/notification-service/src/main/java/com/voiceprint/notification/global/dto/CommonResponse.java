package com.voiceprint.notification.global.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private Integer code;
    private String message;
    private T data;
}