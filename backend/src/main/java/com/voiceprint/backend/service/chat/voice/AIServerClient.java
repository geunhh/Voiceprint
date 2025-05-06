package com.voiceprint.backend.service.chat.voice;

import com.voiceprint.backend.api.chat.voice.VoiceChatWebSocketHandler;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.WebSocketHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIServerClient {

    @Lazy
    private VoiceChatWebSocketHandler voiceChatHandler;

    @Value("${ai-server.url}")
    private String aiServerUrl;

    // clientSessionId -> AI 서버 세션 (WebFlux)
    private final Map<String, WebSocketSession> aiSessions = new ConcurrentHashMap<>();

    /**
     * AI 서버와 WebSocket 연결
     */
    public void connect(Long userId, String clientSessionId) {
        ReactorNettyWebSocketClient client = new ReactorNettyWebSocketClient();
        String fullUrl = aiServerUrl + "?userId=" + userId;

        client.execute(
                URI.create(fullUrl),
                new WebSocketHandler() {
                    @Override
                    public Mono<Void> handle(WebSocketSession session) {
                        log.info("AI 서버 WebSocket 연결 성공 - 사용자 ID: {}, 세션 ID: {}", userId, session.getId());

                        aiSessions.put(clientSessionId, session);

                        // 수신한 메시지를 프론트엔드로 중계
                        return session.receive().doOnNext(message -> {
                            try {
                                if (message.getType() == WebSocketMessage.Type.TEXT) {
                                    String text = message.getPayloadAsText();
                                    voiceChatHandler.handleAIServerResponse(clientSessionId, text);
                                } else {
                                    ByteBuffer binary = message.getPayload().asByteBuffer();
                                    voiceChatHandler.handleAIServerResponse(clientSessionId, binary);
                                }
                            } catch (Exception e) {
                                log.error("AI 응답 처리 중 오류 발생", e);
                            }
                        }).doOnError(error -> {
                            log.error("AI 서버 수신 중 오류 발생", error);
                        }).then();
                    }
                }
        ).subscribe();
    }

    /**
     * AI 서버로 텍스트 메시지 전송
     */
    public void sendTextMessage(String clientSessionId, Long userId, String message) {
        WebSocketSession session = aiSessions.get(clientSessionId);
        if (session != null && session.isOpen()) {
            try {
                session.send(Mono.just(session.textMessage(message)))
                        .doOnError(err -> log.error("AI 서버로 텍스트 전송 중 오류", err))
                        .subscribe();
                log.debug("AI 서버로 텍스트 메시지 전송 - 사용자 ID: {}, 세션 ID: {}", userId, clientSessionId);
            } catch (Exception e) {
                log.error("텍스트 메시지 전송 예외", e);
            }
        } else {
            log.warn("AI 세션이 유효하지 않음 - 세션 ID: {}", clientSessionId);
        }
    }

    /**
     * AI 서버로 바이너리 메시지 전송
     */
    public void sendBinaryMessage(String clientSessionId, Long userId, ByteBuffer buffer) {
        WebSocketSession session = aiSessions.get(clientSessionId);
        if (session != null && session.isOpen()) {
            try {
                session.send(Mono.just(session.binaryMessage(factory -> factory.wrap(buffer))))
                        .doOnError(err -> log.error("AI 서버로 바이너리 전송 중 오류", err))
                        .subscribe();
                log.debug("AI 서버로 바이너리 메시지 전송 - 사용자 ID: {}, 세션 ID: {}, 크기: {}", userId, clientSessionId, buffer.remaining());
            } catch (Exception e) {
                log.error("바이너리 메시지 전송 예외", e);
            }
        } else {
            log.warn("AI 세션이 유효하지 않음 - 세션 ID: {}", clientSessionId);
        }
    }

    /**
     * 오디오 완료 알림
     */
    public void notifyAudioComplete(String clientSessionId, Long userId, Map<String, Object> messageMap) {
        String completeMessage = "{\"action\":\"audio_complete\"}";
        sendTextMessage(clientSessionId, userId, completeMessage);
    }

    /**
     * 연결 종료
     */
    public void disconnect(String clientSessionId, Long userId) {
        WebSocketSession session = aiSessions.remove(clientSessionId);
        if (session != null && session.isOpen()) {
            session.close().doOnTerminate(() ->
                    log.info("AI 서버 연결 종료 - 사용자 ID: {}, 세션 ID: {}", userId, clientSessionId)
            ).subscribe();
        }
    }

    /**
     * 애플리케이션 종료 시 세션 정리
     */
    @PreDestroy
    public void cleanUp() {
        aiSessions.forEach((sessionId, session) -> {
            if (session != null && session.isOpen()) {
                session.close()
                        .doOnError(e -> log.error("AI 서버 연결 종료 실패", e))
                        .subscribe();
            }
        });
        aiSessions.clear();
    }
}
