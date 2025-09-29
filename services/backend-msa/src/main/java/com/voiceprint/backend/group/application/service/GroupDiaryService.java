package com.voiceprint.backend.group.application.service;

import com.voiceprint.backend.diary.adapter.in.web.dto.GroupDiaryResponseDTO;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryDetailResponse;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryListWithCursorDTO;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.global.exception.diary.UnauthorizedDiaryException;
import com.voiceprint.backend.global.exception.group.GroupNotFoundException;
import com.voiceprint.backend.global.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// Import Use Case interfaces
import com.voiceprint.backend.group.application.port.in.groupdiary.SaveSharedDiaryUseCase;
import com.voiceprint.backend.group.application.port.in.groupdiary.GetGroupDiariesUseCase;
import com.voiceprint.backend.group.application.port.in.groupdiary.GetGroupDiaryDetailUseCase;
import com.voiceprint.backend.group.application.port.in.groupdiary.GetAllGroupDiariesUseCase;

// Import Outgoing Ports
import com.voiceprint.backend.group.application.port.out.GroupDiaryRepositoryPort;
import com.voiceprint.backend.group.application.port.out.GroupRepositoryPort;
import com.voiceprint.backend.diary.application.port.out.DiaryRepositoryPort;
import com.voiceprint.backend.user.application.port.out.UserRepositoryPort;
import com.voiceprint.backend.group.application.port.out.GroupUserRepositoryPort;
import com.voiceprint.backend.notification.application.port.out.NotificationRepositoryPort;

// Import Domain Objects
import com.voiceprint.backend.group.domain.Group;
import com.voiceprint.backend.group.domain.GroupDiary;
import com.voiceprint.backend.group.domain.GroupUser;
import com.voiceprint.backend.diary.domain.Diary;
import com.voiceprint.backend.user.domain.User;


@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class GroupDiaryService implements SaveSharedDiaryUseCase, GetGroupDiariesUseCase, GetGroupDiaryDetailUseCase, GetAllGroupDiariesUseCase {

    private final GroupDiaryRepositoryPort groupDiaryRepositoryPort;
    private final GroupRepositoryPort groupRepositoryPort;
    private final DiaryRepositoryPort diaryRepositoryPort;
    private final UserRepositoryPort userRepositoryPort;
    private final GroupUserRepositoryPort groupUserRepositoryPort;
    private final NotificationRepositoryPort notificationPort;


    /**
     * 일기 공유 메서드
     * 사용자가 선택한 일기를 선택한 그룹들에 공유함
     * 선택한 그룹에 포함된 유저들에게 알림 생성 후 전송.
     */
    @Override
    @Transactional(readOnly = false)
    public List<Notification> saveSharedDiary(Integer diaryId, Integer userId, List<Integer> groupIds) {
        // Diary 찾기.
        Diary diary = diaryRepositoryPort.findById(diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("일기를 찾을 수 없습니다."));

        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        if (!diary.getUserId().equals(userId)) { // Use pure domain object
            throw new UnauthorizedDiaryException("해당 일기에 권한이 없습니다.");
        }

        List<Notification> toSaveNotifications = new ArrayList<>();

        log.debug("###권한 검사 통과 & 그룹별 알람 생성 시작");

        for (Integer groupId : groupIds) {
            Group group = groupRepositoryPort.findById(groupId)
                    .orElseThrow(() -> new GroupNotFoundException("그룹을 찾을 수 없습니다."));

            log.debug("공유 대상 그룹 : {} - {} ", group, group.getName());

            boolean isExist = groupDiaryRepositoryPort.existsByGroupIdAndDiaryId(groupId, diaryId);
            if (isExist) continue;

            // GroupDiary 도메인 객체 생성
            GroupDiary groupDiary = GroupDiary.create(diary, group);
//            groupDiaryRepositoryPort.save(groupDiary, diary);
            groupDiaryRepositoryPort.link(diary.getId(), group.getId(), LocalDateTime.now());

            // 1. 그룹 유저 조회
            List<GroupUser> usersInGroup = groupUserRepositoryPort.findAllByGroupId(groupId);

            // 2. 알림 생성 및 전송
            for (GroupUser memberGroupUser : usersInGroup) {
                User member = memberGroupUser.getUser();
                if (member.getId().equals(userId)) continue;

                //메타 데이터 구성
                Map<String, Object> metadata = Map.of(
                        "groupId", group.getId(),
                        "diaryId", diary.getId()
                );

                String message = user.getNickname() + "님이 " + group.getName() + " 그룹에 일기를 공유했어요!";

                Notification notification = Notification.create(
                        member.getId(),
                        "newDiary",
                        message,
                        metadata
                );
                toSaveNotifications.add(notification);
            }
        }
        // 알림 저장 & flush
        notificationPort.saveAll(toSaveNotifications);
        log.debug("## 그룹일기 및 Notificaiton 저장 완료");

        return toSaveNotifications;
    }


    /**
     * 공유 일기 목록 조회 메서드
     * 사용자가 속한 특정 그룹에 공유 일기 목록 조회
     */
    @Override
    public GroupDiaryListWithCursorDTO getGroupDiaries(Integer groupId, LocalDateTime cursor, Integer size, Integer userId) {
        // 1. 로그인한 사용자 ID 추출 (컨트롤러에서 이미 처리)

        // 2. 사용자 유효성 검사
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 확인 불가"));

        // 3. 페이징을 위한 PageRequest 객체 생성 (size + 1 로 다음 페이지 유무 판단)
        PageRequest page = PageRequest.of(0, size + 1);

        // 4. 특정 그룹에서 공유된 일기들을 sharedAt 기준으로 최신순 정렬하여 가져옴 (cursor가 있으면 필터링)
        List<GroupDiary> groupDiaries = groupDiaryRepositoryPort.findGroupDiariesWithCursor(groupId, cursor, page);

        // 5. 다음 페이지 존재 여부 판단
        boolean hasNext = groupDiaries.size() > size;
        if (hasNext) {
            groupDiaries = groupDiaries.subList(0, size);
        }

        // 6. 다음 커서 값 설정 (마지막 sharedAt 기준)
        LocalDateTime nextCursor = hasNext ? groupDiaries.get(groupDiaries.size() - 1).getSharedAt() : null;

        // 7. Diary 정보를 DTO로 매핑하여 응답 리스트 구성
        List<GroupDiaryResponseDTO> response = groupDiaries.stream()
                .map(gd -> {
                    Diary d = gd.getDiary();
                    User diaryUser = userRepositoryPort.findById(d.getUserId())
                            .orElseThrow(() -> new UserNotFoundException("일기 작성 유저 정보 없음"));
                    return new GroupDiaryResponseDTO(
                            gd.getGroup().getId(),
                            d.getId(),
                            d.getTitle(),
                            d.getContent(),
                            gd.getSharedAt().toString(),
                            "https://temp.image.url/" + diaryUser.getProfileImageId(), // FIXED
                            diaryUser.getNickname()
                    );
                }).collect(Collectors.toList());

        // 8. 응답 DTO 반환
        return new GroupDiaryListWithCursorDTO(response, nextCursor);
    }


    @Override
    public GroupDiaryDetailResponse getGroupDiaryDetail(Integer userId, Integer groupId, Integer diaryId) {
        // ✅ 그룹 가입 여부 확인
        boolean isMember = groupUserRepositoryPort.existsByUserIdAndGroupId(userId, groupId);
        if (!isMember) {
            throw new UnauthorizedGroupAccessException("해당 그룹에 속해있지 않은 사용자입니다.");
        }

        // ✅ 그룹 일기 존재 확인
        GroupDiary groupDiary = groupDiaryRepositoryPort.findByGroupIdAndDiaryId(groupId, diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("해당 그룹 일기를 찾을 수 없습니다."));

        Diary diary = groupDiary.getDiary();
        User user = userRepositoryPort.findById(diary.getUserId())
                .orElseThrow(() -> new UserNotFoundException("일기 작성 유저 정보 없음"));
        Group group = groupRepositoryPort.findById(groupDiary.getGroup().getId())
                .orElseThrow(() -> new GroupNotFoundException("그룹 정보 없음"));

        return new GroupDiaryDetailResponse(
                groupDiary.getId(),
                group.getId(),
                group.getName(),
                diary.getId(),
                user.getId(),
                user.getNickname(),
                "https://temp.image.url/" + user.getProfileImageId(), // FIXED
                diary.getCreatedAt(),
                diary.getTitle(),
                diary.getContent()
        );
    }


    @Override
    public GroupDiaryListWithCursorDTO getAllGroupDiaries(LocalDateTime cursor, int size, Integer userId) {
        // 1. 유저 정보 확인
        User user = userRepositoryPort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 없음"));

        // 2. 유저가 속한 그룹 ID 조회
        List<Integer> groupIds = groupUserRepositoryPort.findGroupIdsByUserId(userId);
        if (groupIds.isEmpty()) {
            log.warn("유저가 속한 그룹 없음");
            return new GroupDiaryListWithCursorDTO(Collections.emptyList(), null);
        }

        // 3. size + 1개 조회로 다음 페이지 여부 확인
        PageRequest pageRequest = PageRequest.of(0, size * 2);
        List<GroupDiary> groupDiaries = groupDiaryRepositoryPort.findByGroupIdsWithCursorExcludeUser(
                groupIds, cursor, userId, pageRequest);

        if (groupDiaries.isEmpty()) {
            log.warn("공유 일기 없음");
            return new GroupDiaryListWithCursorDTO(Collections.emptyList(), null);
        }

        // 4. diaryId 기준으로 중복 제거
        List<GroupDiary> finalList = groupDiaries.stream()
                .collect(Collectors.groupingBy(gd -> gd.getDiary().getId(),
                        Collectors.reducing((a, b) -> a))) // Take first one if duplicate diaryId
                .values().stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .limit(size) // Limit to requested size
                .collect(Collectors.toList());


        // 5. 다음 커서 설정
        LocalDateTime nextCursor = finalList.size() == size
                ? finalList.get(finalList.size() - 1).getSharedAt()
                : null;

        // 6. DTO 변환
        List<GroupDiaryResponseDTO> response = finalList.stream()
                .map(gd -> {
                    Diary d = gd.getDiary();
                    User diaryUser = userRepositoryPort.findById(d.getUserId())
                            .orElseThrow(() -> new UserNotFoundException("일기 작성 유저 정보 없음"));
                    return new GroupDiaryResponseDTO(
                            gd.getGroup().getId(),
                            d.getId(),
                            d.getTitle(),
                            d.getContent(),
                            gd.getSharedAt().toString(),
                            "https://temp.image.url/" + diaryUser.getProfileImageId(), // FIXED
                            diaryUser.getNickname()
                    );
                }).collect(Collectors.toList());

        return new GroupDiaryListWithCursorDTO(response, nextCursor);
    }
}
