package com.voiceprint.backend.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.backend.chat.domain.ChatMessage;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

/**
 * 엔티티에서는 List<ChatMessageResponseDTO>로 다루고 싶을 때 사용하는 변환기
 * DB에는 String(JSON 문자열)로 저장.
 */
@Converter
public class ChatMessageListConverter implements AttributeConverter<List<ChatMessage>, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper().findAndRegisterModules();

    /**
     * 엔티티 → DB 저장 시 호출
     * - List<ChatMessageResponseDTO> → JSON 문자열
     */
    @Override
    public String convertToDatabaseColumn(List<ChatMessage> attribute) {
        // 저장 시엔 항상 "객체 배열" 형태로만 기록 → 앞으로 이중 인코딩 발생 방지
        if (attribute == null || attribute.isEmpty()) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            // 직렬화 실패 시 예외 변환
            throw new IllegalArgumentException("ChatMessage 리스트 → JSON 직렬화 실패", e);
        }
    }

    /**
     * DB → 엔티티 로딩 시 호출
     * - JSON 문자열 → List<ChatMessageResponseDTO>
     *
     * - JSON 배열이 [ {...}, {...} ] 형태면 바로 매핑
     * - 만약 원소가 문자열("{...}") 형태라면 → 이중 인코딩된 것으로 보고 다시 파싱
     */
    @Override
    public List<ChatMessage> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            JsonNode rootNode = MAPPER.readTree(dbData);
            List<ChatMessage> messages = new ArrayList<>();
            for (JsonNode node : rootNode) {
                if (node.isTextual()) {
                    // 원소가 문자열(JSON을 다시 감싼 형태)라면 → 내부 문자열을 다시 파싱
                    try {
                        messages.add(MAPPER.readValue(node.asText(), ChatMessage.class));
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException(
                        "이중 인코딩된 JSON 메시지 파싱 실패: " + node.asText(), e);
                    }
                } else {
                    // 원소가 객체라면 바로 DTO로 매핑
                    messages.add(MAPPER.treeToValue(node, ChatMessage.class));
                }
            }
            return messages;
        } catch (IOException e) {
            throw new IllegalArgumentException("ChatMessage 리스트 → JSON 직렬화 실패", e);
        }
    }
}
