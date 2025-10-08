package com.voiceprint.backend.chat.adapter.out.persistence;

import com.voiceprint.backend.chat.domain.Chatbot;
import org.springframework.stereotype.Component;

@Component
public class ChatbotMapper {

    public Chatbot toDomain(ChatbotJPAEntity entity) {
        if (entity == null) {
            return null;
        }

        return Chatbot.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .initMent(entity.getInitMent())
                .prompt(entity.getPrompt())
                .isDeleted(entity.getIsDeleted())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ChatbotJPAEntity toEntity(Chatbot domain) {
        // Todo:사용할 때 만들기
        return null;
    }
}
