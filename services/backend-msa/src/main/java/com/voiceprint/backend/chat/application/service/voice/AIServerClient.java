    package com.voiceprint.backend.chat.application.service.voice;

    import jakarta.websocket.ContainerProvider;
    import jakarta.websocket.Session;
    import jakarta.websocket.WebSocketContainer;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.slf4j.Slf4j;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.net.URI;
    import java.nio.ByteBuffer;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.function.Consumer;

    @Slf4j
    @Transactional
    @RequiredArgsConstructor
    @Service
    public class AIServerClient {
        // Map<sessionId, Callback> 으로 여러 세션 지원
        private final Map<String, Consumer<Object>> callbacks = new ConcurrentHashMap<>();
        private Session session;
        @Value("${ai-websocket.url}")
        private String aiUrl;

        public void connect(Integer userId, String frontSessionId,
                            Consumer<Object> onResponse) throws Exception {
            // 1) 콜백 저장
            callbacks.put(frontSessionId, onResponse);

            // 2) 실제 연결
            String uri = String.format(aiUrl + "?userId=%d&sessionId=%s", userId, frontSessionId);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            // 생성자에 frontSessionId를 함께 넘겨 줌
            this.session = container.connectToServer(
                    new AIServerClientEndpoint(this, frontSessionId),
                    URI.create(uri)
            );
        }

        void setSession(Session session) {
            this.session = session;
        }

        public void sendTextMessage(String sessionId, Integer userId, String json) throws Exception {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendText(json);
                log.info("🛫 AI 서버로 텍스트 전송: {}", json);
            }
        }

        public void sendBinaryMessage(String sessionId, Integer userId, ByteBuffer buffer) throws Exception {
            if (session != null && session.isOpen()) {
                session.getBasicRemote().sendBinary(buffer);
                log.info("🛫 AI 서버로 바이너리 전송 크기: {} 바이트", buffer.remaining());
            }
        }

        // AI 서버가 텍스트 응답을 주면
        void handleServerText(String sessionId, String text) {
            Consumer<Object> cb = callbacks.get(sessionId);
            if (cb != null) cb.accept(text);
        }

        // 바이너리 응답
        void handleServerBinary(String sessionId, ByteBuffer data) {
            Consumer<Object> cb = callbacks.get(sessionId);
            if (cb != null) cb.accept(data);
        }

        public void disconnect(String sessionId, Integer userId) {
            try {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            } catch (Exception e) {
                log.error("AI 서버 연결 종료 중 오류", e);
            }
        }
    }

