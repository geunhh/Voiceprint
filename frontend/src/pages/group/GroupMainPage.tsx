import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import Button from "../../components/common/Button";
import GroupCard from "../../components/group/GroupCard";

import profile2 from "../../assets/icons/chatPink.png";
import profile3 from "../../assets/icons/chatRed.png";
import profile1 from "../../assets/icons/chatYellow.png";

import bg from "../../assets/icons/groupDefault.png";

interface MyGroup {
  groupId: number;
  groupName: string;
  groupImageUrl: string;
  memberCount: number;
  memberProfileImages: string[];
  createdAt: string;
}

export default function GroupMainPage() {
  const navigate = useNavigate();
  const [groups, setGroups] = useState<MyGroup[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get("/api/v1/group/my");
        setGroups(res.data.data);
      } catch (err) {
        console.error("내 그룹 목록 조회 실패", err);
      }
    })();
  }, []);

  if (!groups.length) {
    return (
      <div>
        <div className="flex-row text-center mt-10">
          <p className="font-bold text-3xl">그룹 다이어리</p>
          <p className="font-semibold">친구와 함께 공유하기</p>
        </div>

        <div className="relative mt-16 flex items-center justify-center">
          <GroupCard
            groupName="새로운 그룹 만들기"
            groupImageUrl={bg}
            memberCount={3}
            memberProfileImages={[profile1, profile2, profile3]}
            onClick={() => {
              navigate("/group/create");
            }}
          />
        </div>
      </div>
    );
  }

  const currentGroup = groups[currentIndex];

  return (
    <div>
      {/* 그룹 다이어리 소개 */}
      <div className="flex-row text-center mt-10">
        <p className="font-bold text-3xl">그룹 다이어리</p>
        <p className="font-semibold">친구와 함께 공유하기</p>
      </div>

      {/* 그룹 카드 */}
      <div className="relative mt-4 flex items-center justify-center">
        {groups.length > 1 && (
          <button
            onClick={() =>
              setCurrentIndex((prev) =>
                prev === 0 ? groups.length - 1 : prev - 1
              )
            }
            className="absolute left-4 text-3xl text-gray-200 transition-colors hover:text-yellow-400"
          >
            &lt;
          </button>
        )}

        <GroupCard
          groupName={currentGroup.groupName}
          groupImageUrl={currentGroup.groupImageUrl}
          memberCount={currentGroup.memberCount}
          memberProfileImages={currentGroup.memberProfileImages}
          createdAt={currentGroup.createdAt}
          onClick={() => {
            navigate(`/group/${currentGroup.groupId}`);
          }}
        />

        {groups.length > 1 && (
          <button
            onClick={() =>
              setCurrentIndex((prev) =>
                prev === groups.length - 1 ? 0 : prev + 1
              )
            }
            className="absolute right-4 text-3xl text-gray-200 transition-colors hover:text-yellow-400"
          >
            &gt;
          </button>
        )}
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
