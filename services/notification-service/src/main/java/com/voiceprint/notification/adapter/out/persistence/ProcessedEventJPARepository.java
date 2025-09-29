package com.voiceprint.notification.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedEventJPARepository extends JpaRepository<ProcessedEvent, String> {
}
