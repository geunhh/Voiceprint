package com.voiceprint.backend.service.chat.voice;

import lombok.extern.slf4j.Slf4j;
import jakarta.websocket.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Base64;

@Slf4j
@ClientEndpoint
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
            this.session = container.connectToServer(this, uri);
            this.messageHandler = handler;
            System.out.printf("WebSocket 연결 성공: {}", uri);
        } catch (Exception e) {
            System.out.printf("WebSocket 연결 실패", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        System.out.printf("WebSocket 연결 수립 - 세션 ID: {}", session.getId());
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        System.out.printf("수신된 텍스트 메시지: {}", message);
        if (messageHandler != null) {
            messageHandler.handleText(message);
        }
    }

    @OnMessage
    public void onBinaryMessage(byte[] message) {
        try {
            // Base64 디코딩
            byte[] decodedData = Base64.getDecoder().decode(message);
            ByteBuffer buffer = ByteBuffer.wrap(decodedData);
            System.out.printf("수신된 바이너리 메시지 크기: {}", buffer.remaining());
            if (messageHandler != null) {
                messageHandler.handleBinary(buffer);
            }
        } catch (IllegalArgumentException e) {
            System.out.printf("Base64 디코딩 오류: {}", e.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.printf("WebSocket 연결 종료 - 세션 ID: {}, 이유: {}", session.getId(), reason);
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.printf("WebSocket 오류 - 세션 ID: {}, 오류: {}", session.getId(), throwable.getMessage());
    }

    public void sendText(String message) {
        try {
            if (session != null && session.isOpen()) {
                session.getAsyncRemote().sendText(message);
                System.out.printf("텍스트 메시지 전송: {}", message);
            }
        } catch (Exception e) {
            System.out.printf("텍스트 메시지 전송 오류", e);
        }
    }

    public void sendBinary(ByteBuffer buffer) {
        try {
            if (session != null && session.isOpen()) {
                // Base64 인코딩하여 전송
                String base64Data = Base64.getEncoder().encodeToString(buffer.array());
                session.getAsyncRemote().sendText(base64Data);
                System.out.printf("바이너리 메시지 전송: {} 바이트", buffer.remaining());
            }
        } catch (Exception e) {
            System.out.printf("바이너리 메시지 전송 오류", e);
        }
    }

    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close();
            }
        } catch (Exception e) {
            System.out.printf("WebSocket 세션 종료 오류", e);
        }
    }
}


