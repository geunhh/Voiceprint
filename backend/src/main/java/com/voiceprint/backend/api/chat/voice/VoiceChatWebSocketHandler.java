package com.voiceprint.backend.api.chat.voice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.service.chat.voice.AIServerClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceChatWebSocketHandler extends AbstractWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final AIServerClient aiServerClient;

    // 세션 관리를 위한 맵
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 사용자 ID별 세션 관리 (한 사용자가 여러 세션을 가질 수 있음)
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    // 세션과 AI 서버 연결 관리
//    private final Map<String, Object> sessionToAIConnection = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        sessions.put(sessionId, session);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        // 연결만 시도하고 더 이상 리턴값 저장은 필요 없음
        aiServerClient.connect(userId, sessionId);

        log.info("WebSocket 연결 성공 - 사용자 ID: {}, 세션 ID: {}", userId, sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();

        log.debug("텍스트 메시지 수신 - 사용자 ID: {}, 세션 ID: {}, 내용: {}", userId, sessionId, payload);

        try {
            // JSON 메시지 처리 (예: 명령어, 설정 등)
            Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
            String action = (String) messageMap.get("action");

            // 메시지 종류에 따른 처리
            switch (action) {
                case "audio_complete":
                    // 오디오 녹음 완료 시그널 처리
                    handleAudioComplete(session, messageMap);
                    break;
                case "ping":
                    // 연결 유지를 위한 핑 처리
                    session.sendMessage(new TextMessage("{\"action\":\"pong\"}"));
                    break;
                default:
                    // 기타 메시지는 AI 서버로 전달
                    aiServerClient.sendTextMessage(sessionId, userId, payload);
            }
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생", e);
            // 오류 메시지 전송
            session.sendMessage(new TextMessage("{\"error\":\"메시지 처리 중 오류가 발생했습니다.\"}"));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");
        // WebM 형식 확인 (프론트엔드에서 WebM으로 인코딩됐는지 확인)
        ByteBuffer buffer = message.getPayload();
        log.info("📥 바이너리 메시지 수신! 세션: {}, 크기: {}", session.getId(), message.getPayloadLength());

        log.debug("바이너리 메시지 수신 - 사용자 ID: {}, 세션 ID: {}, 크기: {}", userId, sessionId, buffer.remaining());

        // 오디오 데이터를 AI 서버로 전달
        aiServerClient.sendBinaryMessage(sessionId, userId, buffer);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        // 세션 정리
        sessions.remove(sessionId);

        // 사용자별 세션 관리에서 제거
        if (userId != null) {
            Set<String> userSessionIds = userSessions.get(userId);
            if (userSessionIds != null) {
                userSessionIds.remove(sessionId);
                if (userSessionIds.isEmpty()) {
                    userSessions.remove(userId);
                }
            }
        }

        // AI 서버 연결 종료
        aiServerClient.disconnect(sessionId, userId);
//        sessionToAIConnection.remove(sessionId);

        log.info("WebSocket 연결 종료 - 사용자 ID: {}, 세션 ID: {}, 상태: {}", userId, sessionId, status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        log.error("WebSocket 전송 오류 - 사용자 ID: {}, 세션 ID: {}", userId, sessionId, exception);

        // 연결 종료 처리
        if (session.isOpen()) {
            session.close(CloseStatus.SERVER_ERROR);
        }
    }

    // AI 서버로부터 받은 응답을 클라이언트에게 전달
    public void handleAIServerResponse(String sessionId, Object response) throws IOException {
        WebSocketSession session = sessions.get(sessionId);
        if (session != null && session.isOpen()) {
            if (response instanceof String) {
                session.sendMessage(new TextMessage((String) response));
            } else if (response instanceof byte[]) {
                session.sendMessage(new BinaryMessage(ByteBuffer.wrap((byte[]) response)));
            } else if (response instanceof ByteBuffer) {
                session.sendMessage(new BinaryMessage((ByteBuffer) response));
            }
        }
    }

    // 사용자 ID로 모든 세션에 메시지 전송
    public void sendToUser(Long userId, Object message) throws IOException {
        Set<String> sessionIds = userSessions.get(userId);
        if (sessionIds != null) {
            for (String sessionId : sessionIds) {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    if (message instanceof String) {
                        session.sendMessage(new TextMessage((String) message));
                    } else if (message instanceof byte[]) {
                        session.sendMessage(new BinaryMessage(ByteBuffer.wrap((byte[]) message)));
                    } else if (message instanceof ByteBuffer) {
                        session.sendMessage(new BinaryMessage((ByteBuffer) message));
                    }
                }
            }
        }
    }

    // 오디오 완료 시그널 처리
    private void handleAudioComplete(WebSocketSession session, Map<String, Object> messageMap) {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        // AI 서버에 오디오 처리 완료 알림
        aiServerClient.notifyAudioComplete(sessionId, userId, messageMap);
    }
}