package com.voiceprint.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class BatchResult {
    private final int sent;
    private final int skipped;
    private final List<String> errors;
}