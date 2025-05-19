package com.voiceprint.backend.service.comment;

import com.voiceprint.backend.api.comment.dto.CommentCreatRequestDTO;
import com.voiceprint.backend.api.comment.dto.CommentCreateResponseDTO;
import com.voiceprint.backend.api.comment.dto.CommentGetResponseDTO;
import com.voiceprint.backend.api.comment.dto.CommentListWithCursorDTO;
import com.voiceprint.backend.common.exception.comment.CommentNotFoundException;
import com.voiceprint.backend.common.exception.comment.UnauthorizedCommentAccessException;
import com.voiceprint.backend.common.exception.user.UserNotFoundException;
import com.voiceprint.backend.domain.Entity.*;
import com.voiceprint.backend.domain.Repository.*;
import com.voiceprint.backend.service.alarm.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final GroupDiaryRepository groupDiaryRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final GroupUserRepository groupUserRepository;

    // 댓글 작성
    public CommentCreateResponseDTO saveComment (long userId, long groupDiaryId, CommentCreatRequestDTO commentCreatRequestDTO) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User를 찾을 수 없습니다."));

        GroupDiary groupDiary = groupDiaryRepository.getById(groupDiaryId);
        Group group = groupDiary.getGroup();

        Comment comment = Comment.builder()
                .user(user)
                .groupDiary(groupDiary)
                .content(commentCreatRequestDTO.getContent())
                .isDeleted(false)
                .build();

        commentRepository.save(comment);

        // 댓글 작성자 제외한 그룹 유저들에게 알림 생성
        List<User> users = groupUserRepository.findUsersByGroupId(group.getId());
        List<Notification> notifications = new ArrayList<>();

        for (User target : users) {
            if (target.getId() == userId) continue;

            Map<String, Object> metadata = Map.of(
                    "groupId", groupDiary.getGroup().getId(),
                    "diaryId", groupDiary.getDiary().getId()
            );

            Notification notification = Notification.create(
                    target,
                    "newComment",
                    user.getNickname() + "님이 " + group.getName() + " 그룹의 내 일기에 댓글을 남겼습니다. 확인해보세요!!",
                    null
            );
            notifications.add(notification);
        }

        // 저장
        notificationRepository.saveAll(notifications);

        // 트랜잭션 커밋 이후 실행
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                for (Notification notification : notifications) {
                    Map<String, Object> metadata = Map.of(
                            "notificationId", notification.getId(),
                            "groupId", group.getId(),
                            "diaryId", groupDiary.getDiary().getId()
                    );
                    notification.setMetadata(metadata);  // 메타데이터 주입
                }

                notificationService.publishAllNotifications(notifications);
            }
        });

        // 응답 반환
        return new CommentCreateResponseDTO(comment.getContent());
    }

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
        // 6. entity -> dto 매핑
        List<CommentGetResponseDTO> dtoList = slice.stream()
                .map(c -> new CommentGetResponseDTO(
                        c.getUser().getId(),
                        c.getUser().getNickname(),
                        c.getUser().getProfileImage().getImageUrl(),
                        c.getId(),
                        c.getCreatedAt(),
                        c.getContent()
                ))
                .toList();

        // 7. 최종 응답 객체 생성
        CommentListWithCursorDTO responseDTOS = new CommentListWithCursorDTO();
        responseDTOS.setCode(200);
        responseDTOS.setMessage("댓글 조회 성공");
        responseDTOS.setComments(dtoList);
        responseDTOS.setNextCursor(hasNext ? nextCursor : null);

        return responseDTOS;

    }


    // 댓글 삭제
    public void deleteComment (int commentId, long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("댓글이 없습니다."));

        // 댓글 삭제 요청자와 댓글 작성자가 같은지 확인
        if (comment.getUser().getId() != userId) {
            throw new UnauthorizedCommentAccessException("본인의 댓글이 아닙니다.");
        }
        comment.deleteComment();
    }
}
