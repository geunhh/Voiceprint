package com.voiceprint.backend.service.chat.voice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.api.chat.voice.VoiceChatWebSocketHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServerClient {

    private final VoiceChatWebSocketHandler voiceChatHandler;
    private final ObjectMapper objectMapper;

    @Value("${ai-server.url}")
    private String aiServerUrl;

    // AI 서버와의 WebSocket 세션 관리
    private final Map<String, WebSocketSession> aiSessions = new ConcurrentHashMap<>();

    // 클라이언트 세션 ID와 AI 서버 세션 ID 간의 매핑
    private final Map<String, String> clientToAISessionMap = new ConcurrentHashMap<>();

    /**
     * AI 서버와 WebSocket 연결 생성
     */
    public Object connect(Long userId, String clientSessionId) {
        try {
            StandardWebSocketClient client = new StandardWebSocketClient();

            AIServerWebSocketHandler handler = new AIServerWebSocketHandler(clientSessionId);

            // AI 서버에 연결
            String fullUrl = aiServerUrl + "?userId=" + userId;
            WebSocketSession aiSession = client.doHandshake(handler, fullUrl).get();

            // 세션 매핑 저장
            String aiSessionId = aiSession.getId();
            aiSessions.put(aiSessionId, aiSession);
            clientToAISessionMap.put(clientSessionId, aiSessionId);

            log.info("AI 서버 연결 성공 - 사용자 ID: {}, 클라이언트 세션: {}, AI 세션: {}",
                    userId, clientSessionId, aiSessionId);

            return aiSession;
        } catch (Exception e) {
            log.error("AI 서버 연결 실패", e);
            throw new RuntimeException("AI 서버 연결에 실패했습니다.", e);
        }
    }

    /**
     * AI 서버로 텍스트 메시지 전송
     */
    public void sendTextMessage(String clientSessionId, Long userId, String message) {
        String aiSessionId = clientToAISessionMap.get(clientSessionId);
        if (aiSessionId != null) {
            WebSocketSession aiSession = aiSessions.get(aiSessionId);
            if (aiSession != null && aiSession.isOpen()) {
                try {
                    aiSession.sendMessage(new TextMessage(message));
                    log.debug("AI 서버로 텍스트 메시지 전송 - 사용자 ID: {}, 클라이언트 세션: {}", userId, clientSessionId);
                } catch (Exception e) {
                    log.error("AI 서버로 메시지 전송 실패", e);
                }
            }
        }
    }

    /**
     * AI 서버로 바이너리 메시지(오디오 데이터) 전송
     */
    public void sendBinaryMessage(String clientSessionId, Long userId, ByteBuffer buffer) {
        String aiSessionId = clientToAISessionMap.get(clientSessionId);
        if (aiSessionId != null) {
            WebSocketSession aiSession = aiSessions.get(aiSessionId);
            if (aiSession != null && aiSession.isOpen()) {
                try {
                    org.springframework.web.socket.BinaryMessage binaryMessage =
                            new org.springframework.web.socket.BinaryMessage(buffer);
                    aiSession.sendMessage(binaryMessage);
                    log.debug("AI 서버로 바이너리 메시지 전송 - 사용자 ID: {}, 클라이언트 세션: {}, 크기: {}",
                            userId, clientSessionId, buffer.remaining());
                } catch (Exception e) {
                    log.error("AI 서버로 바이너리 메시지 전송 실패", e);
                }
            }
        }
    }

    /**
     * AI 서버에 오디오 처리 완료 알림
     * 메시지 형식을 AI 서버에 맞게 수정
     */
    public void notifyAudioComplete(String clientSessionId, Long userId, Map<String, Object> messageMap) {
        // audio_complete 액션을 포함한 JSON 형식으로 전송
        String completeMessage = "{\"action\":\"audio_complete\"}";
        sendTextMessage(clientSessionId, userId, completeMessage);
    }

    /**
     * AI 서버와 연결 종료
     */
    public void disconnect(String clientSessionId, Long userId) {
        String aiSessionId = clientToAISessionMap.remove(clientSessionId);
        if (aiSessionId != null) {
            WebSocketSession aiSession = aiSessions.remove(aiSessionId);
            if (aiSession != null && aiSession.isOpen()) {
                try {
                    aiSession.close();
                    log.info("AI 서버 연결 종료 - 사용자 ID: {}, 클라이언트 세션: {}, AI 세션: {}",
                            userId, clientSessionId, aiSessionId);
                } catch (Exception e) {
                    log.error("AI 서버 연결 종료 실패", e);
                }
            }
        }
    }

    /**
     * 애플리케이션 종료 시 모든 연결 정리
     */
    @PreDestroy
    public void cleanUp() {
        aiSessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("AI 서버 연결 종료 실패", e);
            }
        });

        aiSessions.clear();
        clientToAISessionMap.clear();
    }

    /**
     * AI 서버의 WebSocket 응답을 처리하는 핸들러
     */
    private class AIServerWebSocketHandler extends TextWebSocketHandler {

        private final String clientSessionId;

        public AIServerWebSocketHandler(String clientSessionId) {
            this.clientSessionId = clientSessionId;
        }

        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            log.debug("AI 서버 웹소켓 연결 설정 완료 - 세션: {}", session.getId());
        }

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            log.debug("AI 서버로부터 텍스트 메시지 수신 - 내용: {}", payload);

            // 클라이언트에게 응답 전달
            voiceChatHandler.handleAIServerResponse(clientSessionId, payload);
        }

        @Override
        protected void handleBinaryMessage(WebSocketSession session, org.springframework.web.socket.BinaryMessage message) throws Exception {
            ByteBuffer buffer = message.getPayload();
            log.debug("AI 서버로부터 바이너리 메시지 수신 - 크기: {}", buffer.remaining());

            // 클라이언트에게 응답 전달
            voiceChatHandler.handleAIServerResponse(clientSessionId, buffer);
        }
    }
}
