package com.voiceprint.backend.notification.adapter.out.persistence;

import com.voiceprint.backend.global.util.JpaJsonConverter;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Entity(name = "Notification")
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 수신 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJPAEntity user;            // 수신자.

    // 알림 타입 (ex. reminder, new_comment, new_post 등)
    @Column(length = 20, nullable = false)
    private String type;

    // 표시할 메시지
    @Column(length = 100, nullable = false)
    private String message;

    // 추가 데이터 (예: groupId, diaryId)
    @Convert(converter = JpaJsonConverter.class)
    @Column(columnDefinition = "json")
    private Map<String, Object> metadata;  // 추가 필드


    // 읽음 여부 : 읽지 않음 => false
    @Column(name = "is_read", nullable = false)
    private boolean isRead = false;

    // 생성 시각
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    //== 생성 메서드 ==//

    public static NotificationJpaEntity create(
            UserJPAEntity user,
            String type,
            String message,
            Map<String, Object> metadata
    ) {
        NotificationJpaEntity notification = new NotificationJpaEntity();
        notification.user = user;
        notification.type = type;
        notification.message = message;
        notification.metadata = metadata;
        notification.createdAt = LocalDateTime.now();
        return notification;
    }

    //== 비즈니스 로직 ==//
    public void markAsRead() {
        this.isRead = true;
    }
}
