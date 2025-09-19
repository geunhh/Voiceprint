package com.voiceprint.backend.chat.adapter.out.persistence;

import com.voiceprint.backend.chat.application.port.out.ChatbotRepositoryPort;
import com.voiceprint.backend.chat.domain.Chatbot;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ChatbotPersistenceAdapter implements ChatbotRepositoryPort {

    private final ChatbotRepository chatbotRepository;
    private final ChatbotMapper chatbotMapper;

    @Override
    public List<Chatbot> findAll() {
        return chatbotRepository.findAll().stream()
                .map(chatbotMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Chatbot> findById(Byte chatbotId) {
        return chatbotRepository.findById(chatbotId).map(chatbotMapper::toDomain);

    }
}
