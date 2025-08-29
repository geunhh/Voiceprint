package com.voiceprint.backend.diary.application.service;

import com.voiceprint.backend.diary.adapter.in.web.dto.GroupDiaryResponseDTO;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryEntity;
import com.voiceprint.backend.diary.adapter.out.persistence.DiaryRepository;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryDetailResponse;
import com.voiceprint.backend.group.adapter.in.web.dto.GroupDiaryListWithCursorDTO;
import com.voiceprint.backend.global.dto.CommonResponse;
import com.voiceprint.backend.global.exception.diary.DiaryNotFoundException;
import com.voiceprint.backend.global.exception.diary.UnauthorizedDiaryException;

import com.voiceprint.backend.global.exception.group.UnauthorizedGroupAccessException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.*;
import com.voiceprint.backend.domain.Repository.*;
import com.voiceprint.backend.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.backend.notification.domain.Notification;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import com.voiceprint.backend.user.application.service.UserService;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class GroupDiaryService {
    private final GroupDiaryRepository groupDiaryRepository;
    private final GroupRepository groupRepository;
    private final DiaryRepository diaryRepository;
    private final UserService authService;
    private final UserRepository userRepository;
    private final GroupUserRepository groupUserRepository;
    private final NotificationRepositoryPort notificationPort;

    /**
     * 일기 공유 메서드
     * 사용자가 선택한 일기를 선택한 그룹들에 공유함
     * 선택한 그룹에 포함된 유저들에게 알림 생성 후 전송.
     */

    public List<Notification> saveSharedDiary(Integer diaryId, Integer userId, List<Integer> groupIds) {
        // Diary 찾기.
        DiaryEntity diaryEntity = diaryRepository.findById(diaryId)
                .orElseThrow();

        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow();
        if (!diaryEntity.getUser().getId().equals(userId)) {
            throw new UnauthorizedDiaryException("해당 일기에 권한이 없습니다.");
        }

        List<GroupDiary> groupDiaries = new ArrayList<>();
        List<Notification> toSaveNotifications = new ArrayList<>();

        log.debug("###권한 검사 통과 & 그룹별 알람 생성 시작");

        for (Integer groupId : groupIds) {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow();

            log.debug("공유 대상 그룹 : {} - {} ", group, group.getName());

            boolean isExist = groupDiaryRepository.existsByGroupIdAndDiaryId(groupId,diaryId);
            if (isExist) continue;

            groupDiaries.add(new GroupDiary(null, diaryEntity, group, LocalDateTime.now()));

            // 1. 그룹 유저 조회
            List<UserJPAEntity> users = groupUserRepository.findUsersByGroupId(groupId);

            // 2. 알림 생성 및 전송
            for (UserJPAEntity member : users) {
                if (member.getId().equals(userId)) continue; //작성자 제외

                //메타 데이터 구성
                Map<String, Object> metadata = Map.of(
                        "groupId", group.getId(),
                        "diaryId", diaryEntity.getId()
                );

                String message = user.getNickname() + "님이 " + group.getName() + " 그룹에 일기를 공유했어요!";

                // 알림 Entity 직접 생성
                Notification notification = Notification.create(
                        member.getId(),
                        "newDiary",
                        message,
                        metadata
                );
                toSaveNotifications.add(notification);
            }

            //1. 그룹 일기 저장
            groupDiaryRepository.saveAll(groupDiaries);


            //2. 알림 저장 & flush
            notificationPort.saveAll(toSaveNotifications);
            log.debug("## 그룹일기 및 Notificaiton 저장 완료");

        }
        return toSaveNotifications;
    }


    /**
     * 공유 일기 목록 조회 메서드
     * 사용자가 속한 특정 그룹에 공유 일기 목록 조회
     */
    public GroupDiaryListWithCursorDTO getGroupDiaries(HttpServletRequest request, Integer groupId, LocalDateTime cursor, Integer size) {
        // 1. 로그인한 사용자 ID 추출
        Integer userId = authService.getUserIdFromRequest(request);

        // 2. 사용자 유효성 검사
        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("유저 정보 확인 불가"));

        // 3. 페이징을 위한 PageRequest 객체 생성 (size + 1 로 다음 페이지 유무 판단)
        PageRequest page = PageRequest.of(0, size + 1);

        // 4. 특정 그룹에서 공유된 일기들을 sharedAt 기준으로 최신순 정렬하여 가져옴 (cursor가 있으면 필터링)
        List<GroupDiary> groupDiaries = groupDiaryRepository.findGroupDiariesWithCursor(groupId, cursor, page);

        // 5. 다음 페이지 존재 여부 판단
        boolean hasNext = groupDiaries.size() > size;
        if (hasNext) {
            groupDiaries = groupDiaries.subList(0, size);
        }

        // 6. 다음 커서 값 설정 (마지막 sharedAt 기준)
        LocalDateTime nextCursor = hasNext ? groupDiaries.getLast().getSharedAt() : null;

        // 7. Diary 정보를 DTO로 매핑하여 응답 리스트 구성
        List<GroupDiaryResponseDTO> response = groupDiaries.stream()
                .map(gd -> {
                    DiaryEntity d = gd.getDiary();
                    return new GroupDiaryResponseDTO(
                            groupId,
                            d.getId(),
                            d.getTitle(),
                            d.getContent(),
                            gd.getSharedAt().toString(),
                            d.getUser().getProfileImage().getImageUrl(),
                            d.getUser().getNickname()
                    );
                }).toList();

        // 8. 응답 DTO 반환
        return new GroupDiaryListWithCursorDTO(response, nextCursor);
    }


    public GroupDiaryDetailResponse getGroupDiaryDetail(Integer userId, Integer groupId, Integer diaryId) {
        // ✅ 그룹 가입 여부 확인
        boolean isMember = groupUserRepository.existsByUserIdAndGroupId(userId, groupId);
        if (!isMember) {
            throw new UnauthorizedGroupAccessException("해당 그룹에 속해있지 않은 사용자입니다.");
        }

        // ✅ 그룹 일기 존재 확인
        GroupDiary groupDiary = groupDiaryRepository.findByGroupIdAndDiaryId(groupId, diaryId)
                .orElseThrow(() -> new DiaryNotFoundException("해당 그룹 일기를 찾을 수 없습니다."));

        DiaryEntity diaryEntity = groupDiary.getDiary();
        UserJPAEntity user = diaryEntity.getUser();
        Group group = groupDiary.getGroup();

        return new GroupDiaryDetailResponse(
                groupDiary.getId(),
                group.getId(),
                group.getName(),
                diaryEntity.getId(),
                user.getId(),
                user.getNickname(),
                user.getProfileImage().getImageUrl(),
                diaryEntity.getCreatedAt(),
                diaryEntity.getTitle(),
                diaryEntity.getContent()
        );
    }


    public CommonResponse<GroupDiaryListWithCursorDTO> getAllGroupDiaries(HttpServletRequest request, LocalDateTime cursor, int size) {
        Integer userId = authService.getUserIdFromRequest(request);

        // 1. 유저 정보 확인
        UserJPAEntity user = userRepository.findById(userId)
                .orElse(null);
        if (user == null) {
            log.warn("유저 정보 없음");
            return new CommonResponse<>(404,"유저 정보 없음",new GroupDiaryListWithCursorDTO(Collections.emptyList(), null));
        }

        log.debug("유저정보 확인");

        // 2. 유저가 속한 그룹 ID 조회
        List<Integer> groupIds = groupUserRepository.findGroupIdsByUserId(userId);
        if (groupIds.isEmpty()) {
            log.warn("유저가 속한 그룹 없음");
            return new CommonResponse<>(404,"유저가 속한 그룹 없음",new GroupDiaryListWithCursorDTO(Collections.emptyList(), null));
        }

        log.debug("그룹 id 조회");

        // 3. size + 1개 조회로 다음 페이지 여부 확인
        PageRequest pageRequest = PageRequest.of(0, size * 2);
        List<GroupDiary> groupDiaries = groupDiaryRepository.findByGroupIdsWithCursorExcludeUser(
                groupIds, cursor, userId, pageRequest);

        log.debug("그룹 공유일기 조회");

        if (groupDiaries.isEmpty()) {
            log.warn("공유 일기 없음");
            return new CommonResponse<>(404,"공유 일기 없음",new GroupDiaryListWithCursorDTO(Collections.emptyList(), null));
        }

        log.debug("그룹 공유일기 마지막!");

        // 4. diaryId 기준으로 중복 제거
        LinkedHashMap<Integer, GroupDiary> distinctDiaries = new LinkedHashMap<>();
        for (GroupDiary gd : groupDiaries) {
            Integer diaryId = gd.getDiary().getId();
            if (!distinctDiaries.containsKey(diaryId)) {
                distinctDiaries.put(diaryId, gd);
            }
            if (distinctDiaries.size() >= size) break;
        }

        log.debug("그룹 공유일기 마지막!");

        List<GroupDiary> finalList = new ArrayList<>(distinctDiaries.values());

        // 5. 다음 커서 설정
        LocalDateTime nextCursor = finalList.size() == size
                ? finalList.get(finalList.size() - 1).getSharedAt()
                : null;

        // 6. DTO 변환
        List<GroupDiaryResponseDTO> response = finalList.stream()
                .map(gd -> {
                    DiaryEntity d = gd.getDiary();
                    return new GroupDiaryResponseDTO(
                            gd.getGroup().getId(),
                            d.getId(),
                            d.getTitle(),
                            d.getContent(),
                            gd.getSharedAt().toString(),
                            d.getUser().getProfileImage().getImageUrl(),
                            d.getUser().getNickname()
                    );
                }).toList();

        return new CommonResponse<>(200,"모든 그룹의 공유일기 조회 성공",new GroupDiaryListWithCursorDTO(response, nextCursor));
    }



}