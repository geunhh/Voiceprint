package com.voiceprint.backend.domain.Repository;

import com.voiceprint.backend.domain.Entity.DiaryThema;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryThemaRepository extends JpaRepository<DiaryThema, Long> {

    @Query("select d from DiaryThema d where d.user.id IS NULL OR d.user.id = :userId")
    List<DiaryThema> findByUserIdOrDefault(@Param("userId") Long userId);

    Optional<DiaryThema> findByUserId(Long userId);
}
