package com.voiceprint.notification.global.config.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.serializer.DeserializationException;

@Configuration
@RequiredArgsConstructor
public class KafkaErrorHandlerConfig {

    private final KafkaTemplate<Object, Object> kafkaTemplate;


    @Bean
    public CommonErrorHandler commonErrorHandler(KafkaTemplate<Object, Object> kafkaTemplate) {
        // 실패 메시지를 <원본토픽>.DLT 동일 파티션으로 보냄
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
        );

        /**
         * BackOff 전략 : 1s - 2s - 4s ... 최대 60초,
         */
        ExponentialBackOffWithMaxRetries backoff = new ExponentialBackOffWithMaxRetries(5);
        backoff.setInitialInterval(1_000L);
        backoff.setMultiplier(2.0);
        backoff.setMaxInterval(60_000L);

        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backoff);

        // 비재시도 : 역직렬화/JSON 파싱 실패는 재시도하지 말고 즉시 DLT
        handler.addNotRetryableExceptions(
                DeserializationException.class,
                JsonProcessingException.class,  // 스키마 에러
                IllegalArgumentException.class  // eventId/userId 누락 등 검증 실패
                // 그외.. 권한 정도?
        );
        return handler;
    }
}