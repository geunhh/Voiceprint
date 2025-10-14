package com.voiceprint.notification.global.config.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory,
            CommonErrorHandler errorHandler) {

        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory);        // 컨슈머 생성 전략 : spring.kafka.consumer.* 설정 읽어 주입.
        factory.setCommonErrorHandler(errorHandler);        // 공통 에러 핸들러(DLT/재시도 로직 설정) 주입

        // 원하는 커스터마이징
        factory.setConcurrency(3);                          // 병렬 소비 스레드 (컨슈머 수 <= 파티션 수)
        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD           // 오프셋 커밋 방식
                                                            // AckMode.BATCH : poll()로 가져온 한 배치 전체를 처리한 후 오프셋 커밋.
                                                            // AckMode.RECROD : 레코드 단위로 재시도 고고
        );

        return factory;
    }
}
