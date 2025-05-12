package com.voiceprint.backend.domain.chat;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 채팅 세션의 상태를 명시하기 위한 ENUM
 */
public enum ChatSessionStatus {
    WAITING("대기 상태"),
    IN_PROGRESS("대화 중"),
    DIARY_CREATING("일기 생성 중"),
    DIARY_DONE("일기 생성 완료"),
    DIARY_SAVED("저장 완료"),
    ERROR("에러");

    private final String description;

    ChatSessionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 현재 진행중인가 확인하는 메소드
     */
    public boolean isOngoing() {
        return this == IN_PROGRESS ||
                this == DIARY_CREATING ||
                this == DIARY_DONE ||
                this == DIARY_SAVED;
    }



}
