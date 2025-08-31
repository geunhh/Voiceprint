package com.voiceprint.backend.group.adapter.out.persistence;

import com.voiceprint.backend.diary.adapter.out.persistence.DiaryEntity;
import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupDiaryRepository;
import com.voiceprint.backend.group.adapter.out.persistence.mapper.GroupDiaryMapper;
import com.voiceprint.backend.group.application.port.out.GroupDiaryRepositoryPort;
import com.voiceprint.backend.group.domain.GroupDiary;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GroupDiaryPersistenceAdapter implements GroupDiaryRepositoryPort {

    private final GroupDiaryRepository groupDiaryRepository;
    private final GroupDiaryMapper groupDiaryMapper;
    private final EntityManager em;

    /**
     * @deprecated Use link() instead for better performance and clarity.
     */
    @Override
    @Deprecated
    public GroupDiary save(GroupDiary groupDiary, com.voiceprint.backend.diary.domain.Diary diary) {
        throw new UnsupportedOperationException("This method is deprecated. Use link() instead.");
    }

    @Override
    public GroupDiary link(Integer diaryId, Integer groupId, LocalDateTime sharedAt) {
        DiaryEntity diaryRef = em.getReference(DiaryEntity.class, diaryId);
        GroupJpaEntity groupRef = em.getReference(GroupJpaEntity.class, groupId);

        GroupDiaryJpaEntity entity = GroupDiaryJpaEntity.builder()
                .diary(diaryRef)
                .group(groupRef)
                .sharedAt(sharedAt)
                .build();

        GroupDiaryJpaEntity saved = groupDiaryRepository.save(entity);
        return groupDiaryMapper.toDomain(saved);
    }

    @Override
    public List<GroupDiary> findGroupDiariesWithCursor(Integer groupId, LocalDateTime cursor, Pageable pageable) {
        return groupDiaryRepository.findGroupDiariesWithCursor(groupId, cursor, pageable).stream()
                .map(groupDiaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<GroupDiary> findByGroupIdsWithCursorExcludeUser(List<Integer> groupIds, LocalDateTime cursor, Integer userId, Pageable pageable) {
        return groupDiaryRepository.findByGroupIdsWithCursorExcludeUser(groupIds, cursor, userId, pageable).stream()
                .map(groupDiaryMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<GroupDiary> findByGroupIdAndDiaryId(Integer groupId, Integer diaryId) {
        return groupDiaryRepository.findByGroupIdAndDiaryId(groupId, diaryId)
                .map(groupDiaryMapper::toDomain);
    }

    @Override
    public boolean existsByGroupIdAndDiaryId(Integer groupId, Integer diaryId) {
        return groupDiaryRepository.existsByGroupIdAndDiaryId(groupId, diaryId);
    }
}
