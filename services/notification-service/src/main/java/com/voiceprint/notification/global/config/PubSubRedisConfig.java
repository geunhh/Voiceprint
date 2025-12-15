package com.voiceprint.notification.global.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

@Slf4j
@Configuration
public class PubSubRedisConfig {

    @Value("${notification.redis.pubsub.host}")
    private String pubsubHost;

    @Value("${notification.redis.pubsub.port}")
    private Integer pubsubPort;

    @Value("${notification.redis.pubsub.password}")
    private String pubsubPassword;

    @PostConstruct
    public void printPubSubRedisHost() {
        log.debug("[PUBSUB REDIS] 연결 호스트: {}", pubsubHost);
    }

    @Bean
    public RedisConnectionFactory pubsubRedisConnectionFactory() {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(pubsubHost);
        config.setPort(pubsubPort);
        config.setPassword(pubsubPassword);

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder().build();
        return new LettuceConnectionFactory(config, clientConfig);
    }

    @Bean(name = "pubsubRedisTemplate")
    public StringRedisTemplate pubsubRedisTemplate(
            @Qualifier("pubsubRedisConnectionFactory")
            RedisConnectionFactory connectionFactory
    ) {
        return new StringRedisTemplate(connectionFactory);
    }

    /**
     * pub/sub 전용 ListenerContainer
     * -> 이 컨테이너는 pubsub Redis 인스턴스에만 붙음
     */
    @Bean
    public RedisMessageListenerContainer pubsubRedisContainer(
            @Qualifier("pubsubRedisConnectionFactory")
            RedisConnectionFactory factory,
            MessageListenerAdapter adapter
    ) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(factory);
        container.addMessageListener(adapter, new PatternTopic("notification-channel"));
        return container;
    }
}