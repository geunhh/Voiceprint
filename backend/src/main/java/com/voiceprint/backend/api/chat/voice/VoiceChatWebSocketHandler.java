package com.voiceprint.backend.api.chat.voice;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.service.chat.voice.AIServerClient;
import com.voiceprint.backend.service.chat.voice.VoiceChatService;
import jakarta.websocket.OnMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceChatWebSocketHandler extends AbstractWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final AIServerClient aiServerClient;
    private final VoiceChatService voiceChatService;

    // 세션 관리를 위한 맵
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    // 사용자 ID별 세션 관리 (한 사용자가 여러 세션을 가질 수 있음)
    private final Map<Long, Set<String>> userSessions = new ConcurrentHashMap<>();
    // 세션과 AI 서버 연결 관리
    // private final Map<String, Object> sessionToAIConnection = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        // 2. WebSocket 세션 등록
        sessions.put(sessionId, session);
        userSessions.computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet()).add(sessionId);

        // 3. AI 서버와 WebSocket 연결
        // connect 할 때, “이 세션”에 응답이 오면 handleAIServerResponse로 넘겨 달라고 콜백 전달
        aiServerClient.connect(userId, sessionId, response -> {
            try {
                handleAIServerResponse(sessionId, response);
            } catch (IOException e) {
                log.error("콜백 중 에러", e);
            }
        });

        // 4. AI 서버에 유저 정보 전달 (user_id: Long)
        Map<String, Object> initPayload = new HashMap<>();
        initPayload.put("user_id", userId);
        String initMessage = objectMapper.writeValueAsString(initPayload);
        aiServerClient.sendTextMessage(sessionId, userId, initMessage);

        log.info("✅ WebSocket 연결 성공 - 사용자 ID: {}, 세션 ID: {}", userId, sessionId);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        } else if (message instanceof BinaryMessage) {
            handleBinaryMessage(session, (BinaryMessage) message);
        } else {
            log.warn("❓ 지원되지 않는 WebSocket 메시지 타입 수신: {}", message.getClass().getSimpleName());
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");
        String payload = message.getPayload();
        log.debug("📩 텍스트 메시지 수신 - 사용자 ID: {}, 세션 ID: {}, 내용: {}", userId, sessionId, payload);

        try {
            Map<String, String> messageMap = objectMapper.readValue(payload, Map.class);
            String action = messageMap.get("action");
            switch (action) {
                case "audio_complete":
                    handleAudioComplete(session);
                    break;
                case "ping":
                    session.sendMessage(new TextMessage("{action:pong}"));
                    break;
                default:
                    aiServerClient.sendTextMessage(sessionId, userId, payload);
            }
        } catch (Exception e) {
            log.error("🚨 텍스트 메시지 처리 중 오류 발생", e);
            session.sendMessage(new TextMessage("{\"error\":\"메시지 처리 중 오류가 발생했습니다.\"}"));
        }
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");
        ByteBuffer buffer = message.getPayload();
        log.info("📥 바이너리 메시지 수신 - 사용자 ID: {}, 세션 ID: {}, 크기: {}", userId, sessionId, buffer.remaining());

        try {
            // 바이너리 데이터를 AI 서버로 직접 전달
            aiServerClient.sendBinaryMessage(sessionId, userId, buffer);
            log.info("🔗 AI 서버로 바이너리 데이터 전송 완료");
        } catch (Exception e) {
            log.error("🚨 바이너리 메시지 처리 중 오류 발생", e);
        }
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
    @OnMessage
    public void handleAIServerResponse(String sessionId, Object response) throws IOException {
        WebSocketSession session = sessions.get(sessionId);
        if (session == null || !session.isOpen()) return;

        Long userId = (Long) session.getAttributes().get("userId");

        if (response instanceof String) {
            String json = (String) response;
            Map<String, Object> map = objectMapper.readValue(json, Map.class);

            int totalToken = 2000;  // ✅ 하드코딩한 토큰 최대값 (필요시 상수로 빼도 됨)
            int token = 0;
            String assistantContent = null;

            if (map.containsKey("chatting")) {
                List<Map<String, String>> messages = (List<Map<String, String>>) map.get("chatting");

                for (Map<String, String> msg : messages) {
                    String role = msg.get("role");
                    String content = msg.get("content");
                    if (role != null && content != null) {
                        voiceChatService.saveMessage(userId, role, content);
                        if ("assistant".equals(role)) {
                            assistantContent = content;
                        }
                    }
                }

                Object tokenObj = map.get("token");
                if (tokenObj instanceof Number) {
                    token = ((Number) tokenObj).intValue();
                    voiceChatService.accumulateToken(userId, token);
                }

            } else if (map.containsKey("role") && map.containsKey("content")) {
                String role = (String) map.get("role");
                String content = (String) map.get("content");
                voiceChatService.saveMessage(userId, role, content);
                if ("assistant".equals(role)) {
                    assistantContent = content;
                }
                token = content.length();
                voiceChatService.accumulateToken(userId, token);
            } else {
                log.warn("❗ 알 수 없는 메시지 구조 수신: {}", json);
                return;
            }

            // ✅ 프론트로 보낼 형식 구성
            int usageRate = Math.min((int) Math.round(((double) token / totalToken) * 100), 100);
            Map<String, Object> frontendResponse = Map.of(
                    "transcription", assistantContent,
                    "limit", usageRate,
                    "totalToken", totalToken
            );

            String resultJson = objectMapper.writeValueAsString(frontendResponse);
            session.sendMessage(new TextMessage(resultJson));
            log.info("📤 프론트로 전송한 응답: {}", resultJson);
        } else if (response instanceof byte[]) {
            session.sendMessage(new BinaryMessage(ByteBuffer.wrap((byte[]) response)));
            log.info("📤 프론트로 바이너리(byte[]) 메시지 전송 - 세션 ID: {}, 크기: {}바이트", sessionId, ((byte[]) response).length);
        } else if (response instanceof ByteBuffer) {
            session.sendMessage(new BinaryMessage((ByteBuffer) response));
            log.info("📤 프론트로 바이너리(ByteBuffer) 메시지 전송 - 세션 ID: {}, 크기: {}바이트", sessionId, ((ByteBuffer) response).remaining());
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
    private void handleAudioComplete(WebSocketSession session) {
        String sessionId = session.getId();
        Long userId = (Long) session.getAttributes().get("userId");

        try {
            // AI 서버로 전송할 메시지 형식 구성
            Map<String, Object> aiMessage = new HashMap<>();
            aiMessage.put("type", "websocket.receive");
            aiMessage.put("Done", "");
            aiMessage.put("state", "끝");

            // JSON 직렬화
            String messageJson = objectMapper.writeValueAsString(aiMessage);

            // AI 서버에 오디오 처리 완료 알림 전송
            aiServerClient.sendTextMessage(sessionId, userId, messageJson);
            log.info("AI 서버로 오디오 완료 시그널 전송 - 사용자 ID: {}, 세션 ID: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("오디오 완료 시그널 전송 중 오류 발생", e);
        }
    }

}