package com.voiceprint.notification.application.service;

import com.voiceprint.notification.adapter.in.web.dto.NotificationDTO;
import com.voiceprint.notification.adapter.out.AsyncNotificationPublisher;
import com.voiceprint.notification.adapter.out.RedisPublisher;
import com.voiceprint.notification.adapter.out.persistence.*;
import com.voiceprint.notification.adapter.out.redis.SessionStatusReader;
import com.voiceprint.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.notification.dto.BatchResult;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * NotificationService의 단위 테스트 클래스
 * 
 * @ExtendWith(MockitoExtension.class): Mockito를 사용한 단위 테스트 환경 설정
 * - JUnit 5에서 Mockito 기능을 활성화
 * - @Mock, @InjectMocks 어노테이션이 동작하도록 함
 * 
 * 참고: processBatchWithJdbc 메서드는 TransactionSynchronizationManager를 사용하므로
 * 순수 단위 테스트보다는 통합 테스트에 더 적합합니다.
 * 여기서는 학습 목적으로 Mock을 최대한 활용한 단위 테스트 예제를 작성했습니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService 단위 테스트")
class NotificationServiceTest {

    /**
     * @Mock: Mock 객체를 생성하는 어노테이션
     *        - 실제 객체가 아닌 가짜(Mock) 객체를 생성
     *        - 이 객체의 메서드 호출 시 기본적으로 null 또는 기본값 반환
     *        - when().thenReturn()으로 동작을 정의할 수 있음
     */
    @Mock
    private NotificationRepositoryPort notificationPort;

    @Mock
    private RedisPublisher redisPublisher;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private NotificationMessageFactory notificationMessageFactory;

    @Mock
    private NotificationJdbcRepository notificationJdbcRepository;

    @Mock
    private AsyncNotificationPublisher asyncNotificationPublisher;

    @Mock
    private NotificationPerfMonitor perfMonitor;

    @Mock
    private SessionStatusReader sessionStatusReader;

    @Mock
    private EntityManager em;

    /**
     * @InjectMocks: 테스트 대상 객체를 생성하고 @Mock으로 생성된 객체들을 주입
     *               - NotificationService 객체를 생성
     *               - 위에 선언된 @Mock 객체들을 NotificationService의 필드에 자동 주입
     *               - 실제로 테스트할 대상 클래스에 사용
     */
    @InjectMocks
    private NotificationService notificationService;

    private List<UserNotificationPreferenceJpaEntity> testBatch;
    private Map<Integer, String> mockStatusMap;

    /**
     * @BeforeEach: 각 테스트 메서드 실행 전에 실행되는 초기화 메서드
     *              - 테스트 데이터 준비 및 공통 설정
     */
    @BeforeEach
    void setUp() {
        // Given: 테스트용 사용자 알림 설정 데이터 준비
        testBatch = new ArrayList<>();
        testBatch.add(createUserPreference(1, 1001));
        testBatch.add(createUserPreference(2, 1002));
        testBatch.add(createUserPreference(3, 1003));

        // Given: Redis에서 반환될 세션 상태 맵 준비
        mockStatusMap = new HashMap<>();
        mockStatusMap.put(1001, "ACTIVE");
        mockStatusMap.put(1002, "INACTIVE");
        mockStatusMap.put(1003, "ACTIVE");
    }

    /**
     * 테스트 헬퍼 메서드: UserNotificationPreferenceJpaEntity 생성
     */
    private UserNotificationPreferenceJpaEntity createUserPreference(long id, int userId) {
        return UserNotificationPreferenceJpaEntity.builder()
                .id(id)
                .userId(userId)
                .enableAlarms(true)
                .build();
    }

    @Test
    @DisplayName("배치 처리 성공 - 모든 사용자에게 알림 생성 및 전송")
    void processBatchWithJdbc_Success_SendsNotificationsToAllUsers() {
        // Given: 테스트를 위한 사전 조건 설정
        // - Redis에서 반환될 세션 상태 Mock 설정
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(mockStatusMap);

        // - 각 상태에 대한 NotificationDTO Mock 설정
        NotificationDTO mockNotification = new NotificationDTO(
                "REMINDER",
                "일기를 작성할 시간입니다!",
                Map.of("status", "ACTIVE"));
        given(notificationMessageFactory.createNotification(anyString()))
                .willReturn(mockNotification);

        // - JDBC Repository의 배치 저장은 정상 동작으로 가정 (void 메서드)
        willDoNothing().given(notificationJdbcRepository).saveAllBatch(anyList());

        // - 성능 모니터의 메트릭 추가도 정상 동작으로 가정
        willDoNothing().given(perfMonitor).addRedisGet(anyLong());
        willDoNothing().given(perfMonitor).addDbInsert(anyLong());
        willDoNothing().given(perfMonitor).addRedisPublish(anyLong());

        // When: 실제로 테스트할 메서드 호출
        BatchResult result = notificationService.processBatchWithJdbc(testBatch);

        // Then: 결과 검증
        // AssertJ를 사용한 검증 - 메서드 체이닝 방식으로 가독성이 좋음
        assertThat(result).isNotNull();
        assertThat(result.getSent()).isEqualTo(3); // 3명 모두 전송 성공
        assertThat(result.getSkipped()).isEqualTo(0); // 건너뛴 사용자 없음
        assertThat(result.getErrors()).isEmpty(); // 에러 없음

        // Then: Mock 객체의 메서드 호출 검증
        // - sessionStatusReader.getStatusesWithPipeline()이 1번 호출되었는지 확인
        then(sessionStatusReader).should(times(1))
                .getStatusesWithPipeline(anyList());

        // - notificationMessageFactory.createNotification()이 3번 호출되었는지 확인
        then(notificationMessageFactory).should(times(3))
                .createNotification(anyString());

        // - JDBC Repository의 saveAllBatch가 1번 호출되었는지 확인
        then(notificationJdbcRepository).should(times(1))
                .saveAllBatch(anyList());
    }

    @Test
    @DisplayName("배치 처리 - 일부 사용자의 알림이 null일 경우 skip 처리")
    void processBatchWithJdbc_PartialSkip_WhenSomeNotificationsAreNull() {
        // Given: 테스트를 위한 사전 조건 설정
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(mockStatusMap);

        // - 첫 번째 호출: 정상 알림 반환
        // - 두 번째 호출: null 반환 (알림을 보내지 않아야 함)
        // - 세 번째 호출: 정상 알림 반환
        NotificationDTO validNotification = new NotificationDTO(
                "REMINDER",
                "일기를 작성할 시간입니다!",
                Map.of("status", "ACTIVE"));

        given(notificationMessageFactory.createNotification(anyString()))
                .willReturn(validNotification) // 첫 번째 호출
                .willReturn(null) // 두 번째 호출 - skip 대상
                .willReturn(validNotification); // 세 번째 호출

        willDoNothing().given(notificationJdbcRepository).saveAllBatch(anyList());
        willDoNothing().given(perfMonitor).addRedisGet(anyLong());
        willDoNothing().given(perfMonitor).addDbInsert(anyLong());

        // When: 배치 처리 실행
        BatchResult result = notificationService.processBatchWithJdbc(testBatch);

        // Then: 결과 검증 - 2명만 전송, 1명은 skip
        assertThat(result.getSent()).isEqualTo(2);
        assertThat(result.getSkipped()).isEqualTo(1);
        assertThat(result.getErrors()).isEmpty();

        // Then: saveAllBatch는 여전히 호출되어야 함 (2개의 알림만 저장)
        then(notificationJdbcRepository).should(times(1))
                .saveAllBatch(argThat(list -> list.size() == 2));
    }

    @Test
    @DisplayName("배치 처리 실패 - Redis 조회 실패 시 예외 처리")
    void processBatchWithJdbc_Failure_WhenRedisThrowsException() {
        // Given: Redis 조회 시 예외 발생 시나리오
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willThrow(new RuntimeException("Redis connection failed"));

        // When & Then: 예외가 발생하는지 검증
        assertThatThrownBy(() -> notificationService.processBatchWithJdbc(testBatch))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Redis connection failed");

        // Then: 예외 발생 시 JDBC 저장은 호출되지 않아야 함
        then(notificationJdbcRepository).should(never()).saveAllBatch(anyList());
    }

    @Test
    @DisplayName("배치 처리 - 빈 배치 리스트 처리")
    void processBatchWithJdbc_Success_WithEmptyBatch() {
        // Given: 빈 배치 리스트
        List<UserNotificationPreferenceJpaEntity> emptyBatch = new ArrayList<>();

        // Redis는 빈 맵 반환
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(new HashMap<>());

        willDoNothing().given(perfMonitor).addRedisGet(anyLong());

        // When: 빈 배치 처리
        BatchResult result = notificationService.processBatchWithJdbc(emptyBatch);

        // Then: 모든 카운트가 0이어야 함
        assertThat(result.getSent()).isEqualTo(0);
        assertThat(result.getSkipped()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();

        // Then: JDBC 저장은 호출되지 않아야 함 (entities가 비어있음)
        then(notificationJdbcRepository).should(never()).saveAllBatch(anyList());
    }

    @Test
    @DisplayName("배치 처리 - 사용자별 예외 발생 시 에러 수집 및 계속 진행")
    void processBatchWithJdbc_CollectsErrors_WhenIndividualUserFails() {
        // Given: Redis 조회는 성공
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(mockStatusMap);

        // - 첫 번째 호출: 정상
        // - 두 번째 호출: 예외 발생
        // - 세 번째 호출: 정상
        NotificationDTO validNotification = new NotificationDTO(
                "REMINDER",
                "일기를 작성할 시간입니다!",
                Map.of("status", "ACTIVE"));

        given(notificationMessageFactory.createNotification(anyString()))
                .willReturn(validNotification)
                .willThrow(new RuntimeException("Failed to create notification"))
                .willReturn(validNotification);

        willDoNothing().given(notificationJdbcRepository).saveAllBatch(anyList());
        willDoNothing().given(perfMonitor).addRedisGet(anyLong());
        willDoNothing().given(perfMonitor).addDbInsert(anyLong());

        // When: 배치 처리 실행
        BatchResult result = notificationService.processBatchWithJdbc(testBatch);

        // Then: 2명은 성공, 1명은 에러 발생
        assertThat(result.getSent()).isEqualTo(2);
        assertThat(result.getSkipped()).isEqualTo(0);

        // 에러 리스트 검증 - AssertJ의 List 검증 방법
        assertThat(result.getErrors())
                .isNotEmpty() // 리스트가 비어있지 않음
                .hasSize(1) // 정확히 1개의 에러
                .anyMatch(error -> error.contains("userId=1002")) // userId=1002가 포함된 에러 존재
                .anyMatch(error -> error.contains("Failed to create notification")); // 에러 메시지 확인
    }

    @Test
    @DisplayName("배치 처리 - 모든 사용자가 skip되는 경우")
    void processBatchWithJdbc_AllSkipped_WhenAllNotificationsAreNull() {
        // Given: Redis 조회는 성공하지만 모든 알림이 null
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(mockStatusMap);

        // 모든 호출에 대해 null 반환
        given(notificationMessageFactory.createNotification(anyString()))
                .willReturn(null);

        willDoNothing().given(perfMonitor).addRedisGet(anyLong());

        // When: 배치 처리 실행
        BatchResult result = notificationService.processBatchWithJdbc(testBatch);

        // Then: 모두 skip, 전송 및 에러 없음
        assertThat(result.getSent()).isEqualTo(0);
        assertThat(result.getSkipped()).isEqualTo(3);
        assertThat(result.getErrors()).isEmpty();

        // Then: entities가 비어있으므로 JDBC 저장 호출 안 됨
        then(notificationJdbcRepository).should(never()).saveAllBatch(anyList());
    }

    @Test
    @DisplayName("배치 처리 - null userId 처리 시 예외 발생")
    void processBatchWithJdbc_ThrowsException_WhenUserIdIsNull() {
        // Given: userId가 null인 사용자 포함
        UserNotificationPreferenceJpaEntity invalidUser = UserNotificationPreferenceJpaEntity.builder()
                .id(99L)
                .userId(null) // null userId
                .enableAlarms(true)
                .build();

        List<UserNotificationPreferenceJpaEntity> invalidBatch = List.of(invalidUser);

        // Redis 조회는 정상 동작 (빈 맵 반환)
        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(new HashMap<>());

        willDoNothing().given(perfMonitor).addRedisGet(anyLong());

        // When: 배치 처리 실행
        BatchResult result = notificationService.processBatchWithJdbc(invalidBatch);

        // Then: 에러가 수집되어야 함
        assertThat(result.getSent()).isEqualTo(0);
        assertThat(result.getErrors()).isNotEmpty();
    }

    @Test
    @DisplayName("배치 처리 - 대용량 배치 처리 성능 검증")
    void processBatchWithJdbc_Performance_WithLargeBatch() {
        // Given: 1000명의 사용자가 포함된 대용량 배치
        List<UserNotificationPreferenceJpaEntity> largeBatch = new ArrayList<>();
        Map<Integer, String> largeStatusMap = new HashMap<>();

        for (int i = 1; i <= 1000; i++) {
            largeBatch.add(createUserPreference(i, 2000 + i));
            largeStatusMap.put(2000 + i, "ACTIVE");
        }

        given(sessionStatusReader.getStatusesWithPipeline(anyList()))
                .willReturn(largeStatusMap);

        NotificationDTO mockNotification = new NotificationDTO(
                "REMINDER",
                "일기를 작성할 시간입니다!",
                Map.of("status", "ACTIVE"));
        given(notificationMessageFactory.createNotification(anyString()))
                .willReturn(mockNotification);

        willDoNothing().given(notificationJdbcRepository).saveAllBatch(anyList());
        willDoNothing().given(perfMonitor).addRedisGet(anyLong());
        willDoNothing().given(perfMonitor).addDbInsert(anyLong());

        // When: 대용량 배치 처리 실행
        long startTime = System.currentTimeMillis();
        BatchResult result = notificationService.processBatchWithJdbc(largeBatch);
        long endTime = System.currentTimeMillis();

        // Then: 결과 검증
        assertThat(result.getSent()).isEqualTo(1000);
        assertThat(result.getSkipped()).isEqualTo(0);
        assertThat(result.getErrors()).isEmpty();

        // Then: 성능 검증 (단위 테스트이므로 매우 빠르게 완료되어야 함)
        long executionTime = endTime - startTime;
        assertThat(executionTime).isLessThan(1000); // 1초 이내 완료

        System.out.println("대용량 배치 처리 시간: " + executionTime + "ms");
    }
}
