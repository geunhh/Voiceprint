package com.voiceprint.backend.service.chat.voice;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnOpen;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnError;
import jakarta.websocket.OnClose;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
@RequiredArgsConstructor
@Transactional
@ClientEndpoint
public class AIServerClientEndpoint {
    private final AIServerClient client;
    private final String frontSessionId;      // ← 프론트-백 세션 ID 보관

    @OnOpen
    public void onOpen(Session session) {
        log.info("✅ 백엔드→AI 서버 클라이언트 연결됨");
        client.setSession(session);
    }

    @OnMessage
    public void onText(String message) throws IOException {
        log.info("📨 AI 서버 응답 텍스트 수신: {}", message);
        client.handleServerText(frontSessionId, message);
    }

    @OnMessage
    public void onBinaryMessage(ByteBuffer message, boolean last, Session session) {
        log.info("🔊 바이너리 데이터 수신: " + message.remaining() + " bytes, last: " + last);
        try {
            // 바로 프론트엔드로 중계
//            byte[] data = new byte[message.remaining()];
            client.handleServerBinary(frontSessionId, message);

        } catch (Exception e) {
            log.error("❌ 바이너리 처리 중 오류: " + e.getMessage());
        }
    }
//    @OnMessage
//    public void onBinary(byte[] data) throws IOException {
//        log.info("📨 AI 서버 응답 바이너리 수신 크기: {} 바이트", data.length);
//        client.handleServerBinary(frontSessionId, data);
//    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        log.error("🚨 백엔드→AI 서버 WebSocket 에러", throwable);
    }

    @OnClose
    public void onClose(Session session) {
        log.info("🔌 백엔드→AI 서버 WebSocket 연결 종료");
    }
}
