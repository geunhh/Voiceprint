import { useNavigate } from "react-router-dom";
import { useState } from "react";
import Button from "../../components/common/Button";
import GroupCard from "../../components/group/GroupCard";

// 임시 데이터
// 프로필사진 이미지
import profile1 from "../../assets/temp/profile1.png";
import profile2 from "../../assets/temp/profile2.png";
import profile3 from "../../assets/temp/profile3.png";
import profile4 from "../../assets/temp/profile4.png";
import profile5 from "../../assets/temp/profile5.png";
import profile6 from "../../assets/temp/profile6.png";

// 소속 그룹 정보
const myGroups = [
  {
    groupId: 1,
    groupName: "아이스크림 조아 모임",
    groupImage:
      "https://i.pinimg.com/736x/a4/d2/b9/a4d2b9a45a2083eb4118f4ef7421cc14.jpg",
    groupUsers: [
      {
        userId: 1,
        userName: "민태홍",
        userImage: profile1,
      },
      {
        userId: 2,
        userName: "김근휘",
        userImage: profile2,
      },
      {
        userId: 3,
        userName: "이지은",
        userImage: profile3,
      },
    ],
    describtion: "아이스크림을 조아하는 사람들의 모임",
    createdAt: "2025-06-26T14:22:30",
  },
  {
    groupId: 2,
    groupName: "둔산동 할리스",
    groupImage:
      "https://i.pinimg.com/736x/dd/6a/b4/dd6ab4ae676eb3e168ea89e6d2c991c8.jpg",
    groupUsers: [
      {
        userId: 1,
        userName: "민태홍",
        userImage: profile1,
      },
      {
        userId: 5,
        userName: "김혜민",
        userImage: profile5,
      },
    ],
    describtion: "대전 서구의 자랑 둔산동 할리스",
    createdAt: "2024-07-30T14:22:30",
  },
  {
    groupId: 3,
    groupName: "개발자 모임",
    groupImage:
      "https://i.pinimg.com/736x/63/13/fd/6313fd2e2b18a73f31fd7969622c5e99.jpg",
    groupUsers: [
      {
        userId: 1,
        userName: "민태홍",
        userImage: profile1,
      },
      {
        userId: 2,
        userName: "김근휘",
        userImage: profile2,
      },
      {
        userId: 3,
        userName: "이지은",
        userImage: profile3,
      },
      {
        userId: 4,
        userName: "조기흠",
        userImage: profile4,
      },
      {
        userId: 5,
        userName: "김혜민",
        userImage: profile5,
      },
      {
        userId: 6,
        userName: "정다인",
        userImage: profile6,
      },
    ],
    describtion: "열심히 개발하는 모임",
    createdAt: "2025-04-10T14:22:30",
  },
];

export default function GroupMainPage() {
  const navigate = useNavigate();
  const [currentIndex, setCurrentIndex] = useState(0);
  const currentGroup = myGroups[currentIndex];

  return (
    <div>
      {/* 그룹 다이어리 소개 */}
      <div className="flex-row text-center mt-10">
        <p className="font-bold text-3xl">그룹 다이어리</p>
        <p className="font-semibold">친구와 함께 공유하기</p>
      </div>

      {/* 그룹 카드 */}
      <div className="relative mt-4 flex items-center justify-center">
        <button
          onClick={() => setCurrentIndex((prev) => Math.max(prev - 1, 0))}
          className="absolute left-4 text-3xl text-gray-400 disabled:opacity-20"
          disabled={currentIndex === 0}
        >
          &lt;
        </button>

        <GroupCard
          groupName={currentGroup.groupName}
          groupImage={currentGroup.groupImage}
          groupUsers={currentGroup.groupUsers}
          createdAt={currentGroup.createdAt}
          describtion={currentGroup.describtion}
          onClick={() => {
            navigate(`/group/${currentGroup.groupId}`);
          }}
        />

        <button
          onClick={() =>
            setCurrentIndex((prev) => Math.min(prev + 1, myGroups.length - 1))
          }
          className="absolute right-4 text-3xl text-gray-400 disabled:opacity-20"
          disabled={currentIndex === myGroups.length - 1}
        >
          &gt;
        </button>
      </div>

      {/* 그룹 생성 버튼 */}
      <div className="flex justify-center mt-7">
        <Button
          text="그룹 만들기"
          type="fill"
          size="L"
          color="mint"
          onClick={() => {
            navigate("/group/create");
          }}
        />
      </div>
    </div>
  );
}
