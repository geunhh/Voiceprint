import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import { useParams } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import CommentInput from "../../components/diary/CommentInput";
import CommentList from "../../components/diary/CommentList";
import DiaryContent from "../../components/diary/DiaryContent";
import { RootState } from "../../store/store";

interface GroupDiaryInfo {
  groupDiaryId: number;
  groupId: number;
  groupName: string;
  diaryId: number;
  userId: number;
  userName: string;
  userImage: string;
  createdAt: string;
  title: string;
  content: string;
}

interface Comment {
  commentId: number;
  userId: number;
  userName: string;
  userImage: string;
  content: string;
  createdAt: string;
}

interface Comments {
  comments: Comment[];
  nextCursor: number | null;
}

export default function GroupDiaryDetailPage() {
  const { groupId, diaryId } = useParams();
  const [groupDiaryInfo, setGroupDiaryInfo] = useState<GroupDiaryInfo | null>(
    null
  );
  const [comments, setComments] = useState<Comments>({
    comments: [],
    nextCursor: null,
  });

  const user = useSelector((state: RootState) => state.user);

  // 그룹 일기 상세 불러오기
  useEffect(() => {
    const fetchDiaryDetail = async () => {
      if (!groupId || !diaryId) return;
      try {
        const res = await axiosInstance.get(
          `/api/v1/group/${groupId}/${diaryId}`
        );
        setGroupDiaryInfo(res.data.data);
        // console.log("다이어리 상세 정보 불러오기: ", res.data.data);
      } catch (error) {
        console.error("다이어리 상세 정보 불러오기 실패", error);
      }
    };

    fetchDiaryDetail();
  }, [groupId, diaryId]);

  // 댓글 목록 불러오기 - 초기
  useEffect(() => {
    const fetchInitialComments = async () => {
      if (!groupDiaryInfo) return;
      try {
        const res = await axiosInstance.get(
          `/api/v1/comment/${groupDiaryInfo.groupDiaryId}`
        );
        setComments({
          comments: res.data.comments,
          nextCursor: res.data.nextCursor,
        });
        // console.log("댓글 불러오기 확인: ", res.data);
      } catch (error) {
        console.error("댓글 불러오기 실패", error);
      }
    };

    fetchInitialComments();
  }, [groupDiaryInfo]);

  // 댓글 목록 불러오기 - 무한스크롤
  const fetchMoreComments = async () => {
    if (!comments.nextCursor || !groupDiaryInfo) return;

    try {
      const res = await axiosInstance.get(
        `/api/v1/comment/${groupDiaryInfo.groupDiaryId}?cursor=${comments.nextCursor}`
      );

      setComments((prev) => ({
        comments: [...prev.comments, ...res.data.comments], // 최신 댓글을 맨 위에 위치하도록
        nextCursor: res.data.nextCursor,
      }));
      // console.log("추가 댓글 불러오기 확인: ", res.data);
    } catch (error) {
      console.error("추가 댓글 불러오기 실패", error);
    }
  };

  // 댓글 작성
  const handleAddComment = async (newComment: string) => {
    if (!groupDiaryInfo) return;

    try {
      await axiosInstance.post(
        `/api/v1/comment/${groupDiaryInfo.groupDiaryId}`,
        { content: newComment }
      );

      if (user.userId === null) {
        console.error("로그인 정보 없음");
        return;
      }

      // 작성한 댓글 화면에 보여주기 위한 작성한 댓글 comment
      const comment: Comment = {
        commentId: Date.now(), // 임시 ID로 활용
        userId: user.userId,
        userName: user.nickname,
        userImage: user.imageUrl,
        createdAt: new Date().toISOString(),
        content: newComment,
      };

      setComments((prev) => ({
        ...prev,
        comments: [comment, ...prev.comments],
      }));
      // console.log("댓글 작성 성공");
    } catch (error) {
      console.error("댓글 작성 실패", error);
    }
  };

  // 댓글 삭제
  const handleDeleteComment = async (commentId: number) => {
    try {
      await axiosInstance.delete(`/api/v1/comment/${commentId}`);

      setComments((prev) => ({
        ...prev,
        comments: prev.comments.filter((c) => c.commentId !== commentId),
      }));
      // console.log("댓글 삭제 성공");
    } catch (error) {
      console.error("댓글 삭제 실패", error);
    }
  };

  if (!groupDiaryInfo) return;

  const date = new Date(groupDiaryInfo.createdAt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  return (
    <div className="mt-5 p-4 space-y-4">
      {/* 그룹명 및 제목 */}
      <div>
        <p className="text-gray-500">{groupDiaryInfo.groupName}</p>
        <p className="font-semibold text-2xl">{groupDiaryInfo.title}</p>
      </div>

      {/* 작성자 정보 */}
      <div className="flex justify-between items-center">
        <div className="flex items-center gap-2">
          <img
            src={groupDiaryInfo.userImage}
            alt=""
            className="w-10 h-10 rounded-full"
          />
          <div>
            <p className="font-semibold text-lg">{groupDiaryInfo.userName}</p>
            <p className="text-gray-500 text-sm">
              {year}.{month}.{day}
            </p>
          </div>
        </div>
      </div>

      {/* 일기 내용 */}
      <DiaryContent content={groupDiaryInfo.content} />

      {/* 댓글 작성 & 목록 */}

      <CommentInput onSubmit={handleAddComment} />
      <CommentList
        comments={comments.comments}
        authorId={groupDiaryInfo.userId}
        onReachBottom={fetchMoreComments}
        hasNext={!!comments.nextCursor}
        currentUserId={user.userId}
        onDeleteComment={handleDeleteComment}
      />
    </div>
  );
}
