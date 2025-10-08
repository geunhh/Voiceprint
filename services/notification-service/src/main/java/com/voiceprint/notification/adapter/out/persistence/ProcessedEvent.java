package com.voiceprint.notification.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "processed_event")
@Getter
@NoArgsConstructor
@AllArgsConstructor @Builder
public class ProcessedEvent {

    @Id
    @Column(name = "event_id", nullable = false, length = 64)
    private String eventId;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static ProcessedEvent of(String eventId) {
        return ProcessedEvent.builder()
                .eventId(eventId)
                .processedAt(Instant.now())
                .build();
    }
}