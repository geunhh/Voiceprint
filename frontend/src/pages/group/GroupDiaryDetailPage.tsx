import { useState } from "react";
import CommentButton from "../../components/diary/CommetButton";
import DiaryContent from "../../components/diary/DiaryContent";
import CommentList from "../../components/diary/CommentList";
import CommentInput from "../../components/diary/CommentInput";

import profile1 from "../../assets/temp/profile1.png";
import profile2 from "../../assets/temp/profile2.png";
import profile3 from "../../assets/temp/profile3.png";
import profile4 from "../../assets/temp/profile4.png";
import profile5 from "../../assets/temp/profile5.png";
import profile6 from "../../assets/temp/profile6.png";

// 임시 데이터
// 유저 정보
const user = {
  userId: 1,
  userName: "김혜민",
  userImage: profile1,
};
// 다이어리 상세 정보
const groupDiaryInfo = {
  groupId: 1,
  groupName: "아이스크림을 좋아하는 사람들",
  diaryId: 101,
  userId: 1,
  userName: "김혜민",
  userImage: profile1,
  createdAt: "2025-04-27T14:22:30",
  title: "혜민의 서울나들이",
  content:
    "오늘은 친구들이랑 한강 공원에 놀러갔다.바람이 솔솔 불고 햇빛도 따뜻해서 걷기만 해도 기분이 좋아졌다. 엽떡에 유부 추가 3번, 허니콤보랑 시원한 맥주까지… 진짜 완벽한 조합! 잔디밭에 앉아 수다 떨고 먹는 그 시간이 참 좋았다. 해가 지고 나서는 따릉이를 타고 한강을 달렸다. 밤공기 맞으며 자전거 타는 기분, 요즘 같은 날씨에 최고다. 평범하지만 특별했던 하루. 이런 날, 자주 있었으면 좋겠다.그 시간이 참 좋았다. 해가 지고 나서는 따릉이를 타고 한강을 달렸다. 밤공기 맞으며 자전거 타는 기분, 요즘 같은 날씨에 최고다. 평범하지만 특별했던 하루. 이런 날, 자주 있었으면 좋겠다.",
  comment: [
    {
      userId: 1,
      userName: "김혜민",
      userImage: profile1,
      createdAt: "2025-04-27T14:22:30",
      content: "작성자의 댓글",
    },
    {
      userId: 2,
      userName: "민태홍",
      userImage: profile2,
      createdAt: "2025-04-28T14:22:30",
      content: "우와 너무너무 즐거운 하루였을듯용",
    },
    {
      userId: 3,
      userName: "조기흠",
      userImage: profile3,
      createdAt: "2025-04-29T14:22:30",
      content: "한강라면 저도 진짜 좋아해요@!",
    },
    {
      userId: 4,
      userName: "김근휘",
      userImage: profile4,
      createdAt: "2025-05-02T14:22:30",
      content: "담엔 저도 같이 가요! 넘 넘 넘 즐거운 시간을 보내셨네용",
    },
    {
      userId: 5,
      userName: "이지은",
      userImage: profile5,
      createdAt: "2025-05-03T14:22:30",
      content:
        "엽떡에 유부 추가 이거 진짜 맛잘알들만 주문하는 메뉸데,,, 멀 좀 아시네용?",
    },
    {
      userId: 6,
      userName: "정다인",
      userImage: profile6,
      createdAt: "2025-05-06T14:22:30",
      content: "대전하면 타슈 서울하면 따릉이~~ 나랑도 자전거 타러 가자~~",
    },
  ],
};

export default function GroupDiaryDetailPage() {
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState(groupDiaryInfo.comment); // 댓글 추가를 위한 관리

  const date = new Date(groupDiaryInfo.createdAt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  return (
    <div className="mt-5 p-4">
      {/* 그룹명 및 일기 제목 */}
      <div className="mb-5">
        <p className="text-gray-500">{groupDiaryInfo.groupName}</p>
        <p className="font-semibold text-2xl">{groupDiaryInfo.title}</p>
      </div>

      {/* 작성자 정보 및 작성일, 댓글 보기  */}
      <div className="flex justify-between items-center mb-5">
        <div className="flex items-center gap-2">
          <img src={groupDiaryInfo.userImage} alt="" className="w-12" />
          <div className="flex gap-2 items-center">
            <p className="font-semibold text-lg">{groupDiaryInfo.userName}</p>
            <p className="text-gray-500">
              {year}.{month}.{day}
            </p>
          </div>
        </div>
        <CommentButton onClick={() => setShowComments((prev) => !prev)} />
      </div>

      {/* 일기 또는 댓글 내용 */}
      <div className="mt-4">
        {showComments ? (
          <>
            <CommentList comments={comments} authorId={groupDiaryInfo.userId} />
            <CommentInput
              user={user}
              onSubmit={(newComment: string) => {
                const comment = {
                  userId: user.userId,
                  userName: user.userName,
                  userImage: user.userImage,
                  createdAt: new Date().toISOString(),
                  content: newComment,
                };
                setComments((prev) => [...prev, comment]);
              }}
            />
          </>
        ) : (
          <DiaryContent content={groupDiaryInfo.content} />
        )}
      </div>
    </div>
  );
}
