package com.voiceprint.backend.service.chat.voice;

import com.voiceprint.backend.api.chat.voice.VoiceChatWebSocketHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServerClient {

    private VoiceChatWebSocketHandler voiceChatHandler;
    @Value("${ai-server.url}")
    private String aiServerUrl;

    // clientSessionId -> Endpoint
    private final Map<String, AIServerEndpoint> aiEndpoints = new ConcurrentHashMap<>();
    public void setVoiceChatHandler(VoiceChatWebSocketHandler handler) {
        this.voiceChatHandler = handler;
    }
    /**
     * AI 서버와 WebSocket 연결
     */
    public void connect(Long userId, String clientSessionId) {
        try {
            String fullUrl = aiServerUrl + "?userId=" + userId;
            log.info("🚀 AI 서버 WebSocket 연결 시도: {}", fullUrl);

            URI uri = URI.create(fullUrl);
            AIServerEndpoint endpoint = new AIServerEndpoint(uri, new AIServerEndpoint.MessageHandler() {
                @Override
                public void handleText(String message) {
                    try {
                        if (voiceChatHandler != null) {
                            voiceChatHandler.handleAIServerResponse(clientSessionId, message);
                        } else {
                            log.warn("⚠️ voiceChatHandler가 null입니다. 텍스트 메시지를 처리할 수 없습니다.");
                        }
                    } catch (IOException e) {
                        log.error("텍스트 메시지 처리 중 IOException 발생: {}", e.getMessage());
                    }

                }

                @Override
                public void handleBinary(ByteBuffer buffer) {
                    try {
                        if (voiceChatHandler != null) {
                            voiceChatHandler.handleAIServerResponse(clientSessionId, buffer);
                        } else {
                            log.warn("⚠️ voiceChatHandler가 null입니다. 바이너리 메시지를 처리할 수 없습니다.");
                        }
                    } catch (IOException e) {
                        log.error("바이너리 메시지 처리 중 IOException 발생: {}", e.getMessage());
                    }
                }
            });

            aiEndpoints.put(clientSessionId, endpoint);
            log.info("✅ AI 서버 연결 성공 - 클라이언트 세션: {}", clientSessionId);
        } catch (Exception e) {
            log.error("🚨 AI 서버 연결 실패", e);
        }
    }

    /**
     * 텍스트 메시지 전송
     */
    public void sendTextMessage(String clientSessionId, Long userId, String message) {
        AIServerEndpoint endpoint = aiEndpoints.get(clientSessionId);
        if (endpoint != null) {
            endpoint.sendText(message);
            log.info("✅ 텍스트 메시지 전송 - 사용자: {}, 세션: {}", userId, clientSessionId);
        } else {
            log.warn("❌ AI 서버 세션 없음 - 세션: {}", clientSessionId);
        }
    }

    /**
     * 바이너리 메시지 전송
     */
    public void sendBinaryMessage(String clientSessionId, Long userId, ByteBuffer buffer) {
        AIServerEndpoint endpoint = aiEndpoints.get(clientSessionId);
        if (endpoint != null) {
            endpoint.sendBinary(buffer);
            log.info("✅ 바이너리 메시지 전송 - 사용자: {}, 세션: {}, 크기: {}", userId, clientSessionId, buffer.remaining());
        } else {
            log.warn("❌ AI 서버 세션 없음 - 세션: {}", clientSessionId);
        }
    }

    /**
     * 연결 종료
     */
    public void disconnect(String clientSessionId, Long userId) {
        AIServerEndpoint endpoint = aiEndpoints.remove(clientSessionId);
        if (endpoint != null) {
            endpoint.close();
            log.info("🔌 AI 서버 연결 종료 - 사용자: {}, 세션: {}", userId, clientSessionId);
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
     * 서버 종료 시 정리
     */
    @PreDestroy
    public void cleanUp() {
        aiEndpoints.values().forEach(AIServerEndpoint::close);
        aiEndpoints.clear();
    }
}
