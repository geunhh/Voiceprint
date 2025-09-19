package com.voiceprint.backend.diary.adapter.out.persistence;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryThemaRepository extends JpaRepository<DiaryThemaJpaEntity, Integer> {

    @Query("select d from DiaryThema d where d.user.id IS NULL OR d.user.id = :userId")
    List<DiaryThemaJpaEntity> findByUserIdOrDefault(@Param("userId") Integer userId);

    Optional<DiaryThemaJpaEntity> findByUserId(Integer userId);
}
