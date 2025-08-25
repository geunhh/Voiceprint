package com.voiceprint.backend.global.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JPA에서 Map<String, Object> 타입을
 * DB의 JSON 문자열로 자동 변환해주는 컨버터 클래스
 */
@Component
@Converter
public class JpaJsonConverter implements AttributeConverter<Map<String, Object>, String> {

    // Jackson의 ObjectMapper를 이용해 JSON 직렬화/역직렬화 수행
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Entity → DB로 저장될 때 호출됨
     * Map<String, Object> → JSON 문자열로 변환
     */
    @Override
    public String convertToDatabaseColumn(Map<String, Object> attribute) {
        if (attribute == null || attribute.isEmpty()) return null;
        try {
            return objectMapper.writeValueAsString(attribute); // Map → JSON 문자열
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("JSON 직렬화 실패", e);
        }
    }

    /**
     * DB → Entity 로딩 시 호출됨
     * JSON 문자열 → Map<String, Object>로 변환
     */
    @Override
    public Map<String, Object> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) return new HashMap<>();
        try {
            return objectMapper.readValue(dbData, new TypeReference<>() {}); // JSON 문자열 → Map
        } catch (IOException e) {
            throw new IllegalArgumentException("JSON 역직렬화 실패", e);
        }
    }
}
