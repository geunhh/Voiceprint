package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatbotListResponseDTO;
import com.voiceprint.backend.api.chat.dto.ChatbotResponseDTO;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.auth.User;
import com.voiceprint.backend.domain.auth.UserRepository;
import com.voiceprint.backend.domain.chat.Chatbot;
import com.voiceprint.backend.domain.chat.ChatbotRepository;
import jakarta.servlet.http.HttpServletRequest;
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
    private final UserRepository userRepository;
    public ChatbotListResponseDTO getChatbots(HttpServletRequest request) {
        //유저 정보 조회
        Long userId = 1L;
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));
        // 최근 사용 챗봇
        Long recentChatbotId = user.getLastChatbot().getId();


        // 챗봇 전체 목록 조회
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

        return new ChatbotListResponseDTO(recentChatbotId, result);

    }
}
