// src/pages/diary/DiaryFriendPage.tsx
import React, { useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";
import axios from "axios";

import ChatSelector from "../../components/diaryCreate/ChatSelector";
import PageTitle from "../../components/PageTitle";
import Button from "../../components/common/Button";
import ChatExistModal from "../../components/modal/ChatExistModal";

import { setCharacter } from "../../store/characterSlice";

export default function DiaryFriendPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [hovered, setHovered] = useState<"voice" | "chat" | null>("voice");
  const [selectedCharacter, setSelectedCharacter] = useState({
    img: "",
    name: "",
    tag: "",
  });

  // modal open 여부
  const [modalOpen, setModalOpen] = useState(false);

  const getButtonType = (buttonType: "voice" | "chat") => {
    if (hovered) {
      return hovered === buttonType ? "fill" : "line";
    }
    return buttonType === "voice" ? "fill" : "line";
  };

  const handleVoiceStart = () => {
    dispatch(setCharacter(selectedCharacter));
    navigate("/diary/voice");
  };

  const handleChatClick = async () => {
    dispatch(setCharacter(selectedCharacter));

    try {
      const { data } = await axios.get("/api/chat/session/status", {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      });
      const status: string | null = data.data;

      // 상태가 WAITING 또는 null 이면 바로 채팅 시작
      if (status === null || status === "WAITING") {
        navigate("/diary/chat");
      } else {
        // 이미 대화 중이면 modal 띄우기
        setModalOpen(true);
      }
    } catch (err) {
      console.error("세션 상태 조회 오류:", err);
      // 에러나 네트워크 실패 시에도 새 채팅으로 이동
      navigate("/diary/chat");
    }
  };

  const handleContinue = () => {
    setModalOpen(false);
    navigate("/diary/chat");
  };

  const handleRestart = async () => {
    setModalOpen(false);
    try {
      // TODO: 백엔드에서 세션 초기화 API가 있다면 호출
      // await axios.post("/api/chat/session/reset", {}, { headers: { … } });
    } catch (_) {
      // 무시
    }
    navigate("/diary/chat");
  };

  return (
    <>
      <div className="flex flex-col items-center min-h-screen bg-white">
        <div className="w-[95%] mx-auto">
          <PageTitle
            title="대화 친구 선택하기"
            subtitle="오늘 하루 대화 나눌 친구를 선택해주세요"
          />

          {/* 캐릭터 선택 */}
          <ChatSelector onSelect={setSelectedCharacter} />

          {/* 버튼 영역 */}
          <div className="mt-12 flex flex-col items-center gap-4">
            {/* 음성 버튼 */}
            <div
              onMouseEnter={() => setHovered("voice")}
              onMouseLeave={() => setHovered(null)}
            >
              <Button
                text="음성으로 시작하기"
                type={getButtonType("voice")}
                size="L"
                onClick={handleVoiceStart}
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
                onClick={handleChatClick}
              />
            </div>
          </div>
        </div>
      </div>

      {/* 대화 중 모달 */}
      {modalOpen && (
        <ChatExistModal
          onContinue={handleContinue}
          onRestart={handleRestart}
          onClose={() => setModalOpen(false)}
        />
      )}
    </>
  );
}
