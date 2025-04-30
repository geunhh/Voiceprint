package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatbotResponseDTO;
import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.domain.chat.ChatbotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotRepository chatbotRepository;
    public List<ChatbotResponseDTO> getChatbots() {
        List<Chatbot> chatbots = chatbotRepository.findAll();
        List<ChatbotResponseDTO> result = new ArrayList<>();

        for (Chatbot chatbot : chatbots) {
            log.info("chatbot : {}",chatbot);

            ChatbotResponseDTO dto = new ChatbotResponseDTO();
            dto.setId(chatbot.getId());
            dto.setName(chatbot.getName());
            dto.setDescription(chatbot.getDescription());
            dto.setImageUrl(chatbot.getImageUrl());
            result.add(dto);
        }

        return result;

    }
}
