package com.voiceprint.notification.adapter.out.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.voiceprint.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class NotificationJdbcRepository {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    // JPAEntity 기준 SQL.
    private static final String INSERT_SQL =
            """
            INSERT INTO notifications
            (user_id, type, message, metadata, is_read, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    public void saveAllBatch(List<NotificationJpaEntity> notifications) {
        long start = System.nanoTime(); // 타이머

        jdbcTemplate.batchUpdate(INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                NotificationJpaEntity n = notifications.get(i);
                ps.setInt(1, n.getUserId());
                ps.setString(2, n.getType());
                ps.setString(3, n.getMessage());
                try {
                    String metadataJson = (n.getMetadata()==null)
                            ? null
                            : objectMapper.writeValueAsString(n.getMetadata());
                    ps.setString(4, metadataJson);
                } catch (JsonProcessingException e) {
                    // 메타데이터 직렬화 실패 시 최소한 빈 JSON이라도 저장
                    log.error("Failed to serialize metadata for notification: {}", n, e);
                    ps.setString(4, "{}");
                }
                ps.setBoolean(5, n.isRead());
                LocalDateTime createdAt = n.getCreatedAt();
                if (createdAt == null) createdAt = LocalDateTime.now();
                ps.setObject(6, createdAt);
            }

            @Override
            public int getBatchSize() {
                return notifications.size();
            }
        });

        long end = System.nanoTime();
        log.info("[NotificationJdbcRepository] batch insert {} rows took {} ms",
                notifications.size(), (end - start) / 1_000_000);
    }
}
