package com.voiceprint.backend.chat.application.port.out;

import com.voiceprint.backend.chat.domain.Chatbot;

import java.util.List;
import java.util.Optional;

public interface ChatbotRepositoryPort {
    List<Chatbot> findAll();
    Optional<Chatbot> findById(Byte chatbotId);
}