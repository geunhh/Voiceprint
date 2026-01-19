package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.in.web.dto.NotificationListWithCursorDTO;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.domain.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.*;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.times;

/**
 * NotificationService의 알림 조회 기능 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 알림 조회 기능 테스트")
public class NotificationQueryServiceTest {

    //==== Mock 객체 =====
    @Mock
    private NotificationRepositoryPort notificationRepositoryPort;

    // ==== 테스트 대상 : 실제로 테스트할 객체 ====
    @InjectMocks
    private NotificationService notificationService;

    // ==== 테스트 공통 데이터 ====
    private Integer testUserId;
    private List<Notification> testNotifications;

    @BeforeEach
    void setUp() {
        // 모든 test() 직전에 호출되어 리셋
        testUserId = 1001;

        // 테스트 알림 리스트는 각 테스트에서 생성
        testNotifications = new ArrayList<>();
    }

    /**
     * 테스트용 Notification 객체를 생성하는 헬퍼 메서드
     *
     * @param count 생성할 알림 개수
     * @return Notification List.
     */
    private List<Notification> createMockNotifications(int count) {
        List<Notification> notifications = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("groupId", 100 + i);
            metadata.put("diaryId", 200 + i);
            metadata.put("status", "ACTIVE");

            Notification notification = Notification.builder()
                    .id((long) 10)
                    .userId(testUserId)
                    .type("REMINDER")
                    .message("알림메시지 " + i)
                    .metadata(metadata)
                    .isRead(false)
                    .build();

            notifications.add(notification);
        }
        return notifications;
    }

    // ============= 조회 테스트 ==============

    @Test
    @DisplayName("첫 페이지 조회 - cursor null, 다음 페이지 있음")
    void getUnreadNotifications_FirstPage_HasNext() {
        // ========= given
        Long cursor = null;     // 첫 페이지
        Integer size = 10;      // 10개씩 조회

        // Repository에서 반환할 Mock 데이터 생성
        // size + 1 = 11개를 생성 (다음 페이지 있는지 확인하기 위해)
        List<Notification> mockNotifications = createMockNotifications(11);

        // Mock 객체 동작 정의
        // notificationPort.find...() 호출하면, mockNotifications 반환
        given(notificationRepositoryPort.findMyNotificationsV2(testUserId, cursor,size+1))
                .willReturn(mockNotifications);

        // ========= when 실제 동작
        NotificationListWithCursorDTO result = notificationService.getUnreadNotifications(testUserId, cursor,size);


        // ========= then 결과 검증

        assertThat(result).isNotNull();
        assertThat(result.getDiaries()).hasSize(10);
        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextCursor()).isEqualTo(10L);
        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId,cursor,size+1);

    }

    @Test
    @DisplayName("두 번째 페이지 조회 - cursor 값 전달, 다음 페이지 있음")
    void getUnreadNotifications_SecondPage_WithCursor() {
        // ========= given
        Long cursor = 10L;      // 첫 페이지의 마지막 ID
        Integer size = 10;      //

        // Repository에서 반환할 Mock 데이터 생성
        List<Notification> mocNotifications = createMockNotifications(11);

        given(notificationRepositoryPort.findMyNotificationsV2(testUserId,cursor,size+1))
                .willReturn(mocNotifications);

        // ========= when
        NotificationListWithCursorDTO result =
                notificationService.getUnreadNotifications(testUserId, cursor, size);

        // ========= then
        assertThat(result.getDiaries()).hasSize(10);

        assertThat(result.getNextCursor()).isNotNull();
        assertThat(result.getNextCursor()).isEqualTo(10L);

        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId, cursor, size+1);
    }

    @Test
    @DisplayName("정확히 size개 조회 - nextCursor는 null (다음 페이지 없음)")
    void getUnreadNotifications_ExactSize_NoCursor() {
        // ========= given
        Long cursor = null;
        Integer size = 10;      //

        // size + 1 = 11 개 요청하는데, 딱 10개만 있음.
        List<Notification> mocNotifications = createMockNotifications(10);
        given(notificationRepositoryPort.findMyNotificationsV2(testUserId,cursor,size+1))
                .willReturn(mocNotifications);

        // ==========   when
        NotificationListWithCursorDTO result =
                notificationService.getUnreadNotifications(testUserId, cursor, size);

        // ========= then
        assertThat(result.getDiaries()).hasSize(10);
        assertThat(result.getNextCursor()).isNull();

        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId,cursor,size+1);


    }

    @Test
    @DisplayName("DTO 변환 검증 - metadata 필드가 올바르게 매핑됨")
    void getUnreadNotifications_DTOConversion_CorrectMapping() {
        // ========= given
        Long cursor = null;
        Integer size = 3;

        // 특정 metadata를  가진 알림 생성
        List<Notification> mocNotifications = new ArrayList<>();

        // 알림 1 : groupId, diaryId 모두 있음.
        Map<String, Object> metadata1 = new HashMap<>();
        metadata1.put("groupId",100);
        metadata1.put("diaryId",200);
        metadata1.put("status","ACTIVE");

        Notification notification1 = Notification.builder()
                .id(1L)
                .userId(testUserId)
                .type("REMINDER")
                .message("일기를 작성하세요.")
                .metadata(metadata1)
                .build();

        mocNotifications.add(notification1);

        given(notificationRepositoryPort.findMyNotificationsV2(testUserId, cursor, size+1))
                .willReturn(mocNotifications);

        // ========= when
        NotificationListWithCursorDTO result = notificationService.getUnreadNotifications(testUserId, cursor, size);

        // ========= then
        assertThat(result.getDiaries()).hasSize(1);

        // 첫번째 DTO 가져오기
        NotificationDTO dto = result.getDiaries().getFirst();

        assertThat(dto.getType()).isEqualTo("REMINDER");

        assertThat(dto.getMetadata()).isNotNull();
        assertThat(dto.getMessage()).isEqualTo("일기를 작성하세요.");
        assertThat(dto.getMetadata()).containsEntry("groupId",100);
        assertThat(dto.getMetadata()).containsEntry("diaryId",200);
        assertThat(dto.getMetadata()).containsEntry("status","ACTIVE");





    }

    @Test
    @DisplayName("Repository 예외 발생 - 예외가 상위로 전파됨")
    void getUnreadNotifications_RepositoryThrowsException_PropagatesException() {
        // ========= given
        Long cursor = null;     // 첫 페이지
        Integer size = 10;      // 10개씩 조회
        String errorMsg = "DB connection failed";

        given(notificationRepositoryPort.findMyNotificationsV2(testUserId,cursor,size+1))
                .willThrow(new RuntimeException(errorMsg));

        // ========= when & tehn 예외 발생 확인
        assertThatThrownBy(() ->
                notificationService.getUnreadNotifications(testUserId,cursor,size)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining(errorMsg);

        // then : repoistory 호출 확인
        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId,cursor,size+1);
    }

    @Test
    @DisplayName("빈 결과 조회 - 알림이 없을 때 빈 리스트와 null cursor 반환")
    void getUnreadNotifications_EmptyResult_ReturnsEmptyListAndNullCursor() {
        // ========= given
        Long cursor = null;     // 첫 페이지
        Integer size = 10;      //

        List<Notification> emptyList = new ArrayList<>();
        given(notificationRepositoryPort.findMyNotificationsV2(testUserId,cursor,size+1))
                .willReturn(emptyList);

        // ========== when
        NotificationListWithCursorDTO result =
                notificationService.getUnreadNotifications(testUserId, cursor, size);

        // ========== then
        assertThat(result).isNotNull(); // 빈 리스트 객체는 있어야 함.
        assertThat(result.getDiaries()).isEmpty();  // 알림 리스트가 비어있음.
        assertThat(result.getNextCursor()).isNull();

        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId, cursor, size+1);

    }

    @Test
    @DisplayName("마지막 페이지 조회 - size보다 적게 반환되면 nextCursor null")
    void getUnreadNotifications_LastPage_NoCursor() {
        // ========= given
        Long cursor = 50L;
        Integer size = 10;

        List<Notification> mockNotifications = createMockNotifications(5);
        given(notificationRepositoryPort.findMyNotificationsV2(testUserId,cursor,size+1))
                .willReturn(mockNotifications);

        // ======== when
        NotificationListWithCursorDTO result =
                notificationService.getUnreadNotifications(testUserId, cursor, size);

        // ======== then

        assertThat(result.getDiaries()).hasSize(5);
        assertThat(result.getNextCursor()).isNull();

        then(notificationRepositoryPort).should(times(1))
                .findMyNotificationsV2(testUserId,cursor,size+1);



    }

    // ============= 읽음 처리 테스트 ==============
    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    void markNotification_Success() {
        // Given
        Long notificationId = 1L;

        // void 메서드는 return값이 없으므로 willDoNoting() 사용
        willDoNothing()
                .given(notificationRepositoryPort)
                .markAsRead(notificationId, testUserId);
        // When
        notificationService.markNotification(testUserId, notificationId);

        // Then
        then(notificationRepositoryPort).should(times(1))
                .markAsRead(notificationId, testUserId);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 알림 없음 예외")
    void markNotification_NotFound_ThrowsException() {
        // Given
        Long notificationId = 999L;

        willThrow(new RuntimeException("알림 없음"))
                .given(notificationRepositoryPort)
                .markAsRead(notificationId, testUserId);

        // When & Then
        assertThatThrownBy(() ->
                notificationService.markNotification(testUserId, notificationId)
        )
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("알림 없음");

    }

}
