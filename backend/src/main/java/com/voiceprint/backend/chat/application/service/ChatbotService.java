package com.voiceprint.backend.chat.application.service;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatbotListResponseDTO;
import com.voiceprint.backend.chat.adapter.in.web.dto.ChatbotResponseDTO;
import com.voiceprint.backend.chat.application.port.in.ChatbotUseCase;
import com.voiceprint.backend.chat.application.port.out.ChatbotRepositoryPort;
import com.voiceprint.backend.chat.domain.Chatbot;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatbotService implements ChatbotUseCase {

    private final ChatbotRepositoryPort chatbotRepository;
    private final UserRepository userRepository; //Todo : 반드시 수정할것. 왜 얘를 의존하냐.

    public ChatbotListResponseDTO getChatbots(Integer userId) {
        //유저 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        // 최근 사용 챗봇
        Byte recentChatbotId = null;
        if (user.getLastChatbot() != null) {
            recentChatbotId = user.getLastChatbot().getId();
        }

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
