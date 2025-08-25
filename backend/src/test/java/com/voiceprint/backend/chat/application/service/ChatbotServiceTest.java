package com.voiceprint.backend.chat.application.service;

import com.voiceprint.backend.chat.adapter.in.web.dto.ChatbotListResponseDTO;
import com.voiceprint.backend.chat.application.port.out.ChatbotRepositoryPort;
import com.voiceprint.backend.chat.domain.Chatbot;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatbotServiceTest {

    @Mock
    private ChatbotRepositoryPort chatbotRepositoryPort;

    @Mock
    private UserRepository userRepository; // This should be a Port in the future

    @InjectMocks
    private ChatbotService chatbotService;

    private UserJPAEntity testUser;
    private Chatbot testChatbot;
    private Integer testUserId = 1;

    @BeforeEach
    void setUp() {
        // Create a test user
        testUser = UserJPAEntity.builder()
            .id(testUserId)
            .build();

        // Create a test chatbot
        testChatbot = Chatbot.builder()
                .id((byte) 1)
                .name("Test Chatbot")
                .description("A chatbot for testing")
                .build();
    }

    @Test
    @DisplayName("Get Chatbot List - Success")
    void getChatbots_success() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(chatbotRepositoryPort.findAll()).thenReturn(Collections.singletonList(testChatbot));

        // When
        ChatbotListResponseDTO result = chatbotService.getChatbots(testUserId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getChatbots()).hasSize(1);
        assertThat(result.getChatbots().get(0).getName()).isEqualTo("Test Chatbot");
        assertThat(result.getRecentChatbotId()).isNull(); // In this setup, user has no last chatbot

        // Verify that the repository methods were called
        verify(userRepository, times(1)).findById(testUserId);
        verify(chatbotRepositoryPort, times(1)).findAll();
    }
}
