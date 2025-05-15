package com.voiceprint.backend.service.diary;

import com.voiceprint.backend.api.diary.dto.DiarySummaryResponseDTO;
import com.voiceprint.backend.api.diary.dto.GroupDiaryResponseDTO;
import com.voiceprint.backend.api.groups.dto.GroupDiaryListWithCursorDTO;
import com.voiceprint.backend.common.exception.diary.UnauthorizedDiaryException;

import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.Diary;
import com.voiceprint.backend.domain.Entity.GroupDiary;
import com.voiceprint.backend.domain.Entity.User;
import com.voiceprint.backend.domain.Repository.DiaryRepository;
import com.voiceprint.backend.domain.Repository.GroupDiaryRepository;
import com.voiceprint.backend.domain.Repository.GroupRepository;
import com.voiceprint.backend.domain.Repository.UserRepository;
import com.voiceprint.backend.service.auth.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class GroupDiaryService {
    private final GroupDiaryRepository groupDiaryRepository;
    private final GroupRepository groupRepository;
    private final DiaryRepository diaryRepository;
    private final AuthService authService;
    private final UserRepository userRepository;

    @Transactional(readOnly = false)
    public void saveSharedDiary(Long diaryId, Long userId, List<Long> groupIds) {
        Diary diary = diaryRepository.findById(diaryId)
                .orElseThrow();
        if (!diary.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryException("해당 일기에 권한이 없습니다.");
        }
        List<GroupDiary> groupDiaries = groupIds.stream()
                .map(groupId -> new GroupDiary(null, diary, groupRepository.findById(groupId).orElseThrow(), LocalDateTime.now()))
                .collect(Collectors.toList());

        groupDiaryRepository.saveAll(groupDiaries);
    }

    public GroupDiaryListWithCursorDTO getGroupDiaries(HttpServletRequest request, Long groupId, LocalDateTime cursor, Integer size) {
        // 1. 로그인한 사용자 ID 추출
        Long userId = authService.getUserIdFromRequest(request);

        // 2. 사용자 유효성 검사
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 확인 불가"));

        // 3. 페이징을 위한 PageRequest 객체 생성 (size + 1 로 다음 페이지 유무 판단)
        PageRequest page = PageRequest.of(0, size + 1); // 항상 첫 페이지부터 size+1 개

        // 4. 특정 그룹에서 공유된 일기들을 sharedAt 기준으로 최신순 정렬하여 가져옴 (cursor가 있으면 필터링)
        List<GroupDiary> groupDiaries = groupDiaryRepository.findGroupDiariesWithCursor(groupId, cursor, page);

        // 5. 다음 페이지 존재 여부 판단
        boolean hasNext = groupDiaries.size() > size;
        if (hasNext) {
            groupDiaries = groupDiaries.subList(0, size); // 응답은 size 개까지만
        }

        // 6. 다음 커서 값 설정 (마지막 sharedAt 기준)
        LocalDateTime nextCursor = hasNext ? groupDiaries.getLast().getSharedAt() : null;

        // 7. Diary 정보를 DTO로 매핑하여 응답 리스트 구성
        List<GroupDiaryResponseDTO> response = groupDiaries.stream()
                .map(gd -> {
                    Diary d = gd.getDiary();
                    return new GroupDiaryResponseDTO(
                            groupId,
                            d.getId(),
                            d.getTitle(),
                            d.getContent(),
                            gd.getSharedAt().toString(),
                            user.getProfileImage().getImageUrl(),
                            user.getNickname()
                    );
                }).toList();

        // 8. 응답 DTO 반환
        return new GroupDiaryListWithCursorDTO(response, nextCursor);
    }


}
