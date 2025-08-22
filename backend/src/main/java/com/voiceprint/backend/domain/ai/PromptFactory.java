package com.voiceprint.backend.domain.ai;

import org.springframework.ai.chat.prompt.Prompt;

public interface PromptFactory {

    /**
     * 채팅 프롬프트 생성
     */
    default Prompt buildChatPrompt(String userId, String userText) {
        throw new UnsupportedOperationException("buildChatPrompt not supported");
    }

    /**
     * 다이어리 생성 프롬프트 생성
     */
    default Prompt buildDiaryPrompt(String userId) {
        throw new UnsupportedOperationException("buildDiaryPrompt not supported");
    }
}

// 인터페이스에 추상 메서드를 추가하면, 구현체에서 반드시 구현해야하지만,
// default 메서드의 경우, 필수가 아니게 됨.
// 그래서 OCP 면에서 더 좋은 코드를 설계할 수 있음.