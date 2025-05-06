package com.voiceprint.backend.service.chat.voice;

import com.voiceprint.backend.api.chat.voice.VoiceChatWebSocketHandler;
import jakarta.annotation.PreDestroy;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServerClient {

    @Lazy
    private final VoiceChatWebSocketHandler voiceChatHandler;

    @Value("${ai-server.url}")
    private String aiServerUrl;

    // clientSessionId -> Endpoint
    private final Map<String, AIServerEndpoint> aiEndpoints = new ConcurrentHashMap<>();

    /**
     * AI 서버와 WebSocket 연결
     */
    public void connect(Long userId, String clientSessionId) {
        try {
            URI uri = URI.create(aiServerUrl + "?userId=" + userId);

            AIServerEndpoint endpoint = new AIServerEndpoint(uri, new AIServerEndpoint.MessageHandler() {
                @Override
                public void handleText(String message) {
                    try {
                        voiceChatHandler.handleAIServerResponse(clientSessionId, message);
                    } catch (Exception e) {
                        log.error("텍스트 메시지 처리 오류", e);
                    }
                }

                @Override
                public void handleBinary(ByteBuffer buffer) {
                    try {
                        voiceChatHandler.handleAIServerResponse(clientSessionId, buffer);
                    } catch (Exception e) {
                        log.error("바이너리 메시지 처리 오류", e);
                    }
                }
            });

            aiEndpoints.put(clientSessionId, endpoint);
            log.info("AI 서버 연결 성공 - 클라이언트 세션: {}", clientSessionId);
        } catch (Exception e) {
            log.error("AI 서버 연결 실패", e);
        }
    }

    /**
     * 텍스트 메시지 전송
     */
    public void sendTextMessage(String clientSessionId, Long userId, String message) {
        AIServerEndpoint endpoint = aiEndpoints.get(clientSessionId);
        if (endpoint != null) {
            endpoint.sendText(message);
            log.debug("텍스트 메시지 전송 - 사용자: {}, 세션: {}", userId, clientSessionId);
        } else {
            log.warn("AI 서버 세션 없음 - 세션: {}", clientSessionId);
        }
    }

    /**
     * 바이너리 메시지 전송
     */
    public void sendBinaryMessage(String clientSessionId, Long userId, ByteBuffer buffer) {
        AIServerEndpoint endpoint = aiEndpoints.get(clientSessionId);
        if (endpoint != null) {
            endpoint.sendBinary(buffer);
            log.debug("바이너리 메시지 전송 - 사용자: {}, 세션: {}, 크기: {}", userId, clientSessionId, buffer.remaining());
        } else {
            log.warn("AI 서버 세션 없음 - 세션: {}", clientSessionId);
        }
    }

    /**
     * 오디오 처리 완료 통지
     */
    public void notifyAudioComplete(String clientSessionId, Long userId, Map<String, Object> messageMap) {
        String message = "{\"action\":\"audio_complete\"}";
        sendTextMessage(clientSessionId, userId, message);
    }

    /**
     * 연결 종료
     */
    public void disconnect(String clientSessionId, Long userId) {
        AIServerEndpoint endpoint = aiEndpoints.remove(clientSessionId);
        if (endpoint != null) {
            endpoint.close();
            log.info("AI 서버 연결 종료 - 사용자: {}, 세션: {}", userId, clientSessionId);
        }
    }

    /**
     * 서버 종료 시 정리
     */
    @PreDestroy
    public void cleanUp() {
        aiEndpoints.values().forEach(AIServerEndpoint::close);
        aiEndpoints.clear();
    }
}
