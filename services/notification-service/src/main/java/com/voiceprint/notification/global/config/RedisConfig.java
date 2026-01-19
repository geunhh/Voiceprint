package com.voiceprint.notification.global.config;

import com.voiceprint.notification.adapter.in.RedisSubscriber;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
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

    @PostConstruct
    public void printRedisHost() {
        log.debug("🔍 [SESSION REDIS] 연결 호스트: " + redisHost);
    }

    /**
     * 세션/H_GET 전용 Redis (spring.data.redis 기준)
     */
    @Bean
    @Primary
    public RedisConnectionFactory sessionRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(redisHost);
        config.setPort(redisPort);
        config.setPassword(redisPassword);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean
    @Primary
    public RedisTemplate<String, Object> sessionRedisTemplate(
            RedisConnectionFactory sessionRedisConnectionFactory
    ) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(sessionRedisConnectionFactory);

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
     * PUBLISH용 StringRedisTemplate (단일 Redis 인스턴스 - Lettuce Pool 적용)
     */
    @Bean
    @Primary
    public org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory sessionRedisConnectionFactory) {
        return new org.springframework.data.redis.core.StringRedisTemplate(sessionRedisConnectionFactory);
    }
}
