package com.voiceprint.notification.global.config;

import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * QueryDSL 설정 클래스
 * 
 * JPAQueryFactory를 Bean으로 등록하여
 * "타입 안전"한 동적 쿼리 작성을 가능하게 함
 */
@Configuration
public class QueryDslConfig {

    // JPA EntityManager 주입
    // 트랜잭션별로 별도의 EntityManager 제공
    @PersistenceContext
    private EntityManager entityManager; // JPA 엔티티 매니저

    /**
     * QueryDSL 쿼리 생성을 위한 JPAQueryFactory Bean 등록
     * 
     * @return JPAQueryFactory - QueryDSL 쿼리 빌더
     */
    @Bean
    public JPAQueryFactory jpaQueryFactory() {
        return new JPAQueryFactory(entityManager);
    }
}
