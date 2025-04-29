import React, { useState } from "react";
import ChatSelector from "../../components/diaryCreate/ChatSelector";
import PageTitle from "../../components/PageTitle";
import Button from "../../components/common/Button";
import { useNavigate } from "react-router-dom";

export default function DiaryFriendPage() {
  const navigate = useNavigate();
  // hover 선택에 따라 배경 변경
  const [hovered, setHovered] = useState<"voice" | "chat" | null>("voice");

  const getButtonType = (buttonType: "voice" | "chat") => {
    if (hovered) {
      return hovered === buttonType ? "fill" : "line";
    } else {
      return buttonType === "voice" ? "fill" : "line"; // 기본값은 "음성" fill
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      {/* 90퍼센트만 이용 */}
      <div className="w-[95%] mx-auto">
        {/* 상단 타이틀 */}
        <PageTitle
          title="대화 친구 선택하기"
          subtitle="오늘 하루 대화 나눌 친구를 선택해주세요"
        />

        <ChatSelector />

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
              onClick={() => navigate("/diary/voice")}
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
              onClick={() => navigate("/diary/chat")}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
