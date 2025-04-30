import React, { useState } from "react";
import ChatSelector from "../../components/diaryCreate/ChatSelector";
import PageTitle from "../../components/PageTitle";
import Button from "../../components/common/Button";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { setCharacter } from "../../store/characterSlice";

export default function DiaryFriendPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const [hovered, setHovered] = useState<"voice" | "chat" | null>("voice");

  const [selectedCharacter, setSelectedCharacter] = useState<{
    img: string;
    name: string;
    tag: string;
  }>({ img: "", name: "", tag: "" });

  const getButtonType = (buttonType: "voice" | "chat") => {
    if (hovered) {
      return hovered === buttonType ? "fill" : "line";
    } else {
      return buttonType === "voice" ? "fill" : "line";
    }
  };

  const handleStart = (mode: "voice" | "chat") => {
    dispatch(setCharacter(selectedCharacter));
    navigate(`/diary/${mode}`);
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      <div className="w-[95%] mx-auto">
        <PageTitle
          title="대화 친구 선택하기"
          subtitle="오늘 하루 대화 나눌 친구를 선택해주세요"
        />
        {/* 캐릭터 선택 */}
        <ChatSelector onSelect={setSelectedCharacter} />

        {/* 버튼 영역 */}
        <div className="mt-10 flex flex-col items-center gap-4">
          {/* 음성 버튼 */}
          <div
            onMouseEnter={() => setHovered("voice")}
            onMouseLeave={() => setHovered(null)}
          >
            <Button
              text="음성으로 시작하기"
              type={getButtonType("voice")}
              size="L"
              onClick={() => handleStart("voice")}
            />
          </div>

          {/* 채팅 버튼 */}
          <div
            onMouseEnter={() => setHovered("chat")}
            onMouseLeave={() => setHovered(null)}
          >
            <Button
              text="채팅으로 시작하기"
              type={getButtonType("chat")}
              size="L"
              onClick={() => handleStart("chat")}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
