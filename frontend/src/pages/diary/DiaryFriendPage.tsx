// src/pages/diary/DiaryFriendPage.tsx
import { useEffect, useState } from "react";
import { useDispatch } from "react-redux";
import { useNavigate } from "react-router-dom";

import Button from "../../components/common/Button";
import PageTitle from "../../components/common/PageTitle";
import ChatSelector from "../../components/diaryCreate/ChatSelector";
import AlertModal from "../../components/modal/AlertModal";
import ChatExistModal from "../../components/modal/ChatExistModal";

import axios from "axios";
import { setCharacter } from "../../store/characterSlice";

export default function DiaryFriendPage() {
  const dispatch = useDispatch();
  const navigate = useNavigate();

  const [hovered, setHovered] = useState<"voice" | "chat" | null>("voice");
  const [selectedCharacter, setSelectedCharacter] = useState({
    id: 0,
    img: "",
    name: "",
    tag: "",
  });

  const [modalOpen, setModalOpen] = useState(false);
  const [alert, setAlert] = useState<{
    message: string;
    type: "info";
    callback?: () => void;
  } | null>(null);

  const BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const api = axios.create({
    baseURL: BASE_URL,
    headers: {
      Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
      "Content-Type": "application/json",
    },
  });

  useEffect(() => {
    const checkSessionStatus = async () => {
      try {
        const res = await api.get("/api/chat/session/status");
        const status: string | null = res.data.data;
        console.log("현재 세션 상태:", status);

        if (status === "DIARY_DONE") {
          navigate("/diary/temp");
        } else if (status === "DIARY_CREATING") {
          setAlert({
            message: "잠시만요! 일기 생성 중",
            type: "info",
            callback: () => navigate("/main"),
          });
        } else if (status === "DIARY_SAVED") {
          setAlert({
            message: "이미 오늘 일기를 생성했습니다!",
            type: "info",
            callback: () => navigate("/my"),
          });
        } else if (status === "IN_PROGRESS") {
          setModalOpen(true);
        }
      } catch (err) {
        console.error("세션 상태 확인 실패:", err);
      }
    };

    checkSessionStatus();
  }, []);

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
      const res = await api.get("/api/chat/session/status");
      const status: string | null = res.data.data;

      if (status === null || status === "WAITING") {
        await api.post("/api/chat/session/start", {
          chatbotId: selectedCharacter.id,
        });
        navigate("/diary/chat");
      }
    } catch (err) {
      console.error("세션 시작 실패:", err);
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
      await api.post("/api/chat/session/start", {
        chatbotId: selectedCharacter.id,
      });
    } catch (err) {
      console.error("세션 시작 실패:", err);
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
          <ChatSelector onSelect={setSelectedCharacter} />
          <div className="mt-12 flex flex-col items-center gap-4">
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

      {modalOpen && (
        <ChatExistModal
          onContinue={handleContinue}
          onRestart={handleRestart}
          onClose={() => setModalOpen(false)}
        />
      )}
      {alert && (
        <AlertModal
          message={alert.message}
          type={"success"}
          onClose={() => {
            setAlert(null);
            alert.callback?.();
          }}
        />
      )}
    </>
  );
}
