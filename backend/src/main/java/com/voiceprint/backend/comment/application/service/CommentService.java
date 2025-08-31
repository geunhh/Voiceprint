package com.voiceprint.backend.comment.application.service;

import com.voiceprint.backend.comment.adapter.in.web.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.comment.adapter.in.web.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.comment.adapter.in.web.dto.CommentGetResponseDTO;
import com.voiceprint.backend.comment.adapter.in.web.dto.CommentListWithCursorDTO;
import com.voiceprint.backend.comment.application.port.in.CommentUseCase;
import com.voiceprint.backend.comment.application.port.out.CommentRepositoryPort;
import com.voiceprint.backend.comment.domain.Comment;
import com.voiceprint.backend.global.exception.comment.CommentNotFoundException;
import com.voiceprint.backend.global.exception.comment.UnauthorizedCommentAccessException;
import com.voiceprint.backend.global.exception.user.UserNotFoundException;
import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupDiaryRepository;
import com.voiceprint.backend.group.adapter.out.persistence.GroupJpaEntity;
import com.voiceprint.backend.group.adapter.out.persistence.GroupDiaryJpaEntity;
import com.voiceprint.backend.group.adapter.out.persistence.jparepository.GroupUserRepository;
import com.voiceprint.backend.notification.application.port.out.NotificationRepositoryPort;
import com.voiceprint.backend.notification.domain.Notification;
import com.voiceprint.backend.notification.application.service.NotificationService;
import com.voiceprint.backend.user.adapter.out.persistence.UserJPAEntity;
import com.voiceprint.backend.user.adapter.out.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 애플리케이션 서비스
 * - CommentUseCase 구현ㄴ
 */
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CommentService implements CommentUseCase {
    private final CommentRepositoryPort commentRepository;
    private final UserRepository userRepository; //Todo : 수정 요망
    private final GroupDiaryRepository groupDiaryRepository;
    private final NotificationService notificationService;
    private final NotificationRepositoryPort notificationPort;
    private final GroupUserRepository groupUserRepository;

    // 댓글 작성
    @Override
    public CommentCreateResponseDTO saveComment (Integer userId, Integer groupDiaryId, CommentCreatRequestDTO commentCreatRequestDTO) {

        UserJPAEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User를 찾을 수 없습니다."));

        GroupDiaryJpaEntity groupDiary = groupDiaryRepository.getById(groupDiaryId);
        GroupJpaEntity group = groupDiary.getGroup();

        Comment comment = Comment.builder()
                .userId(userId)
                .groupDiaryId(groupDiaryId)
                .content(commentCreatRequestDTO.getContent())
                .isDeleted(false)
                .build();

        commentRepository.save(comment);

        // 댓글 작성자 제외한 그룹 유저들에게 알림 생성
        List<UserJPAEntity> users = groupUserRepository.findUsersByGroupId(group.getId());
        List<Notification> notifications = new ArrayList<>();

        for (UserJPAEntity target : users) {
            if (Objects.equals(target.getId(), userId)) continue;

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("groupId", group.getId());
            metadata.put("diaryId", groupDiary.getDiary().getId());

            Notification notification = Notification.create(
                    target.getId(),
                    "newComment",
                    user.getNickname() + "님이 " + group.getName() + " 그룹의 공유된 일기에 댓글을 남겼습니다. 확인해보세요!!",
                    metadata
            );
            notifications.add(notification);
        }

        List<Notification> savedNotifications = notificationPort.saveAll(notifications);

        // 트랜잭션 커밋 이후 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // Metadata에 알림Id 추가.
                for (Notification notification : savedNotifications) {
                    log.debug("notification : {}, id : {}",notification, notification.getId());
                    Map<String, Object> metadata = new HashMap<>(notification.getMetadata());
                    metadata.put("notificationId", notification.getId());
                }

                // 메타데이터 DB 반영
                // 이 부분은 재평가되어야 합니다. 메타데이터를 DB에서 업데이트해야 한다면,
                // NotificationPort에 updateMetadata(List<Notification> notifications)와 같은 메서드가 필요합니다.
                // 일단 현재 NotificationPort에서 직접 지원되지 않으므로 이 호출은 주석 처리합니다.
                // notificationService.updateNotificationMetadata(savedNotifications);

                // SSE 발행
                notificationService.publishAllNotifications(savedNotifications);
            }
        });

        // 응답 반환
        return new CommentCreateResponseDTO(comment.getContent());
    }

    @Override
    @Transactional(readOnly = true)
    // 댓글 조회
    public CommentListWithCursorDTO getComments(long groupDiaryId, Integer cursorId, int limit) {
        // 1. PageRequest 생성
        Pageable page = PageRequest.of(0, limit + 1);

        // 2. cursor 유무에 따라 적절한 Repository 메서드 호출
        List<Comment> comments = (cursorId == null)
                ? commentRepository.findFirstComment(groupDiaryId, page)
                : commentRepository.findAfterFirstComment(groupDiaryId, cursorId, page);

        // 3.limit+1개를 가져왔의니, 진짜 반환할 개수가 limit를 초과하면 다음 페이지 있음
        boolean hasNext = comments.size() > limit;

        // 4. 댓글 리스트 자르기
        List<Comment> slice = hasNext
                ? comments.subList(0,limit)
                : comments;

        // 5. 다음 요청에 쓸 cursorId: 잘라낸 마지막 댓글 id
        Integer nextCursor = hasNext
                ? slice.get(slice.size() - 1).getId()
                : null;

        Set<Integer> userIds = slice.stream().map(Comment::getUserId).collect(Collectors.toSet());
        Map<Integer, UserJPAEntity> userMap = userIds.isEmpty()
                ? Collections.emptyMap()
                : userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserJPAEntity::getId, u -> u));


        // 6. entity -> dto 매핑
        List<CommentGetResponseDTO> dtoList = slice.stream().map(c -> {
            UserJPAEntity u = userMap.get(c.getUserId());
            String nickname = (u != null) ? u.getNickname() : "탈퇴회원";
            String profileUrl = (u != null && u.getProfileImage() != null) ? u.getProfileImage().getImageUrl() : null;

            return new CommentGetResponseDTO(
                    c.getUserId(),
                    nickname,
                    profileUrl,
                    c.getId(),
                    c.getCreatedAt(),
                    c.getContent()
            );
        }).toList();

        // 7. 최종 응답 객체 생성
        CommentListWithCursorDTO responseDTOS = new CommentListWithCursorDTO();
        responseDTOS.setCode(200);
        responseDTOS.setMessage("댓글 조회 성공");
        responseDTOS.setComments(dtoList);
        responseDTOS.setNextCursor(hasNext ? nextCursor : null);

        return responseDTOS;

    }


    // 댓글 삭제
    @Transactional
    @Override
    public void deleteComment (Integer commentId, Integer userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글이 없습니다."));

        // 댓글 삭제 요청자와 댓글 작성자가 같은지 확인
        if (!Objects.equals(comment.getUserId(), userId)) {
            throw new UnauthorizedCommentAccessException("본인의 댓글이 아닙니다.");
        }
        Comment deleted = comment.deleteComment();
        commentRepository.delete(deleted);
    }
}