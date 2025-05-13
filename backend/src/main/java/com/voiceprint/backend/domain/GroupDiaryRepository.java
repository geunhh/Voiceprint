package com.voiceprint.backend.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupDiaryRepository extends JpaRepository<GroupDiary, Long> {
    // 그룹 ID를 기준으로 최신 순으로 그룹 다이어리 가져오기 (페이지네이션)
    Page<GroupDiary> findByGroupIdOrderBySharedAtDesc(Long groupId, Pageable pageable);
}
