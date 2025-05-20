package com.voiceprint.backend.common.config;

import com.voiceprint.backend.api.alarm.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private Integer redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

//    @Value("${spring.data.redis.ssl.enabled}")
//    private boolean sslEnabled;

    @PostConstruct
    public void printRedisHost() {
        log.debug("🔍 Redis 연결 호스트: " + redisHost);
    }


    @Bean
    public RedisConnectionFactory redisConnectFactory() {
        // Lettuce 기반 커넥션 팩토리
        // RedisPool 관리.

        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
//                .useSsl() // Upstash Redis는 SSL 사용
                .build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // [Key] 문자열로 처리
        template.setKeySerializer(new StringRedisSerializer());

        // [Value] JSON 형태로 객체 저장(직렬화)
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());

        // [Hash Key/Value]도 동일하게 설정
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        return template;
    }

    /**
     * Redis에서 수신한 메시지를 우리가 만든 RedisSubscriber 객체의 onMessage()로 연결
     * Redis 메시지 -> Java 객체 -> 전달
     * 내부적으로 JSON 파싱, 메시지 변환 등 수행..
     *
     * RedisSubscriber의 implements MessageListener와 연결됨.
     */
    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber);
    }

    /**
     * Redis Pub/Sub 채널을 지속적으로 구독하는 리스터 루프 역할.
     * "notifiaction-channel"
     * 메시지를 받으면, 등록된 MessageListenerAdapter로 전달
     *
     * redis의 특정 채널을 지속적으로 구독하며, MessageListenerAdapter로 연결하여
     * 비동기 스레드 풀로 동작함.
     */
    @Bean
    public RedisMessageListenerContainer redisContainer(
            RedisConnectionFactory factory, MessageListenerAdapter adapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(adapter, new PatternTopic("notification-channel"));
        return container;
    }


}
