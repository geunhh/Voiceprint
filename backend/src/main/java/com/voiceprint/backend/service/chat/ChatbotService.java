package com.voiceprint.backend.service.chat;

import com.voiceprint.backend.api.chat.dto.ChatbotListResponseDTO;
import com.voiceprint.backend.api.chat.dto.ChatbotResponseDTO;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.domain.Entity.Chatbot;
import com.voiceprint.backend.domain.Repository.ChatbotRepository;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
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
public class ChatbotService {

    private final ChatbotRepository chatbotRepository;
    private final UserRepository userRepository;
    private final AuthService authService;
    public ChatbotListResponseDTO getChatbots(HttpServletRequest request) {
        //유저 정보 조회
        Integer userId = authService.getUserIdFromRequest(request);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        // 최근 사용 챗봇 조회
        Byte recentChatbotId = user.getLastChatbot() != null
                ? user.getLastChatbot().getId() : null;

        // 챗봇 전체 목록 조회 및 변환
        List<ChatbotResponseDTO> result = chatbotRepository.findAll()
                .stream()
                .peek(chatbot -> log.debug("Loaded chatbot : {}",chatbot)) // 디버깅용 메서드
                .map(ChatbotResponseDTO::from)
                .toList();


        return new ChatbotListResponseDTO(recentChatbotId, result);

    }
}
