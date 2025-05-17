import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import axiosInstance from "../../api/axiosInstance";
import CommentInput from "../../components/diary/CommentInput";
import CommentList from "../../components/diary/CommentList";
import CommentButton from "../../components/diary/CommetButton";
import DiaryContent from "../../components/diary/DiaryContent";
import { RootState } from "../../store/store";

import profileTmp from "../../assets/temp/profile3.png";

// 다이어리 상세 정보
const groupDiaryInfo = {
  groupDiaryId: 15,
  groupId: 19,
  groupName: "아이스크림을 좋아하는 사람들",
  diaryId: 19,
  userId: 1,
  userName: "임시 유저",
  userImage: profileTmp,
  createdAt: "2025-04-27T14:22:30",
  title: "혜민의 서울나들이",
  content:
    "오늘은 친구들이랑 한강 공원에 놀러갔다.바람이 솔솔 불고 햇빛도 따뜻해서 걷기만 해도 기분이 좋아졌다. 엽떡에 유부 추가 3번, 허니콤보랑 시원한 맥주까지… 진짜 완벽한 조합! 잔디밭에 앉아 수다 떨고 먹는 그 시간이 참 좋았다. 해가 지고 나서는 따릉이를 타고 한강을 달렸다. 밤공기 맞으며 자전거 타는 기분, 요즘 같은 날씨에 최고다. 평범하지만 특별했던 하루. 이런 날, 자주 있었으면 좋겠다.그 시간이 참 좋았다. 해가 지고 나서는 따릉이를 타고 한강을 달렸다. 밤공기 맞으며 자전거 타는 기분, 요즘 같은 날씨에 최고다. 평범하지만 특별했던 하루. 이런 날, 자주 있었으면 좋겠다.",
};

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
  const [showComments, setShowComments] = useState(false); // 댓글 표시 여부부
  const [comments, setComments] = useState<Comments>({
    comments: [],
    nextCursor: null,
  });

  const user = useSelector((state: RootState) => state.user);

  const date = new Date(groupDiaryInfo.createdAt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  // 댓글 목록 불러오기 - 초기
  useEffect(() => {
    const fetchInitialComments = async () => {
      if (!showComments) return;
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
  }, [showComments]);

  const fetchMoreComments = async () => {
    if (!comments.nextCursor) return;

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

  return (
    <div className="mt-5 p-4 space-y-6">
      {/* 그룹명 및 제목 */}
      <div>
        <p className="text-gray-500">{groupDiaryInfo.groupName}</p>
        <p className="font-semibold text-2xl">{groupDiaryInfo.title}</p>
      </div>

      {/* 작성자 정보 + 댓글 보기 버튼 */}
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
        <CommentButton onClick={() => setShowComments((prev) => !prev)} />
      </div>

      {/* 일기 내용 */}
      <DiaryContent content={groupDiaryInfo.content} />

      {/* 댓글 작성 & 목록 */}
      {showComments && (
        <>
          <CommentInput onSubmit={handleAddComment} />
          <CommentList
            comments={comments.comments}
            authorId={groupDiaryInfo.userId}
            onReachBottom={fetchMoreComments}
            hasNext={!!comments.nextCursor}
          />
        </>
      )}
    </div>
  );
}
