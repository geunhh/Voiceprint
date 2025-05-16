package com.voiceprint.backend.service.chat.voice;

import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import jakarta.websocket.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Base64;

@Slf4j
@ServerEndpoint("/ws/ai")
public class AIServerEndpoint {

    private Session session;
    private MessageHandler messageHandler;

    public interface MessageHandler {
        void handleText(String message);
        void handleBinary(ByteBuffer buffer);
    }

    public AIServerEndpoint(URI uri, MessageHandler handler) {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
            this.messageHandler = handler;
            log.info("✅ WebSocket 연결 성공: {}", uri);
        } catch (Exception e) {
            log.error("🚨 WebSocket 연결 실패", e);
        }
    }

    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
        // 각 세션마다 텍스트·바이너리 버퍼를 2MB로 설정
        session.setMaxTextMessageBufferSize(2 * 1024 * 1024);
        session.setMaxBinaryMessageBufferSize(2 * 1024 * 1024);
        log.info("✅ WebSocket 연결 수립 - 세션 ID: {}", session.getId());
    }
//    public void onOpen(Session session) {
//        log.info("✅ WebSocket 연결 수립 - 세션 ID: {}", session.getId());
//        this.session = session;
//    }

    @OnMessage
    public void onMessage(String message) {
        log.info("📥 수신된 텍스트 메시지: {}", message);
        if (messageHandler != null) {
            messageHandler.handleText(message);
        }
    }

    @OnMessage
    public void onBinaryMessage(ByteBuffer message) {
        log.info("📥 수신된 바이너리 메시지 크기: {}", message.remaining());
        if (messageHandler != null) {
            messageHandler.handleBinary(message);
        }
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        if (throwable != null) {
            log.error("[{}] WebSocket 에러 발생: {}",
                    session.getId(),
                    throwable.getMessage(), throwable);
        } else {
            log.error("[{}] WebSocket 에러 발생: throwable==null",
                    session.getId());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        log.info("[{}] 연결 종료: code={}, reason={}",
                session.getId(),
                reason.getCloseCode().getCode(),
                reason.getReasonPhrase());
    }

    public void sendText(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendText(message);
                log.info("✅ 텍스트 메시지 전송: {}", message);
            }
        } catch (Exception e) {
            log.error("🚨 텍스트 메시지 전송 오류", e);
        }
    }

    public void sendBinary(ByteBuffer buffer) {
        try {
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendBinary(buffer);
                log.info("✅ 바이너리 메시지 전송: {} 바이트", buffer.remaining());
            }
        } catch (Exception e) {
            log.error("🚨 바이너리 메시지 전송 오류", e);
        }
    }

    public Session getSession() {
        return session;
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
                log.info("🔒 WebSocket 세션 종료");
            }
        } catch (Exception e) {
            log.error("🚨 WebSocket 세션 종료 오류", e);
        }
    }
}
