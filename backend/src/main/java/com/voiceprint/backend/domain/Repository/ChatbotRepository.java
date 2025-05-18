package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.Chatbot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatbotRepository extends JpaRepository<Chatbot, Long> {

}
