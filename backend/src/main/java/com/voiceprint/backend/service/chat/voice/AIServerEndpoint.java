package com.voiceprint.backend.service.chat.voice;
import jakarta.websocket.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;

@ClientEndpoint
public class AIServerEndpoint {

    private Session userSession = null;

    private final MessageHandler handler;

    public interface MessageHandler {
        void handleText(String message);
        void handleBinary(ByteBuffer buffer);
    }

    public AIServerEndpoint(URI endpointURI, MessageHandler handler) {
        try {
            this.handler = handler;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
        } catch (Exception e) {
            throw new RuntimeException("WebSocket 연결 실패", e);
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        this.userSession = session;
        System.out.println("WebSocket 연결됨: " + session.getId());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        this.userSession = null;
        System.out.println("WebSocket 종료: " + reason);
    }

    @OnMessage
    public void onTextMessage(String message) {
        if (handler != null) handler.handleText(message);
    }

    @OnMessage
    public void onBinaryMessage(ByteBuffer buffer) {
        if (handler != null) handler.handleBinary(buffer);
    }

    public void sendBinary(ByteBuffer data) {
        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendBinary(data);
        }
    }

    public void sendText(String message) {
        if (userSession != null && userSession.isOpen()) {
            userSession.getAsyncRemote().sendText(message);
        }
    }

    public void close() {
        if (userSession != null) {
            try {
                userSession.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

