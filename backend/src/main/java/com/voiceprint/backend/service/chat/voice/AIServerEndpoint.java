package com.voiceprint.backend.service.chat.voice;

import jakarta.websocket.OnOpen;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnError;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
/**
 * @Componen 사용이유
 * @ServerEndpoint가 스프링 컨텍스트에 빈으로 등록되지 않아서 해당 어노테이션으로
 * AIServerEndpoint클래스를 빈으로 만들어 톰캣의 JSR-356 컨테이너에 등록
 */
@Slf4j
@Component
@ServerEndpoint("/ws/ai")
public class AIServerEndpoint {
    @OnOpen
    public void onOpen(Session session) {
        session.setMaxTextMessageBufferSize(2 * 1024 * 1024);
        session.setMaxBinaryMessageBufferSize(2 * 1024 * 1024);
        log.info("🛠 JSR-356 WebSocket 버퍼 설정 적용: text={} / binary={}",
                session.getMaxTextMessageBufferSize(),
                session.getMaxBinaryMessageBufferSize());
        log.info("✅ AI 서버 WebSocket 열림, 세션ID={}", session.getId());
    }

    @OnMessage
    public void onTextMessage(Session session, String message) {
        log.info("📩 AI 서버가 받은 텍스트: {}", message);
        // TODO: STT, LLM 처리 → JSON 응답
        String responseJson = processChat(message);
        session.getAsyncRemote().sendText(responseJson);
    }

    @OnMessage
    public void onBinaryMessage(Session session, byte[] data) {
        log.info("📩 AI 서버가 받은 바이너리 데이터 크기: {} 바이트", data.length);
        // TODO: TTS 처리 → 바이너리 응답
        byte[] audioBytes = processTTS(data);
        session.getAsyncRemote().sendBinary(java.nio.ByteBuffer.wrap(audioBytes));
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("🚨 AI 서버 WebSocket 에러 - 세션ID={}", session.getId(), throwable);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("🔌 AI 서버 WebSocket 연결 종료, 세션ID={}", session.getId());
    }

    private String processChat(String input) {
        // LLM 호출 로직
        return "{ \"chatting\": [...] }";
    }

    private byte[] processTTS(byte[] wav) {
        // TTS 로직
        return new byte[0];
    }
}