// src/pages/diary/DiaryChatPage.tsx
import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

import ProgressBar from "../../components/common/ProgressBar";
import ChatList from "../../components/chat/ChatList";
import ChatInput from "../../components/chat/ChatInput";
import Button from "../../components/common/Button";

import DiaryCreatingModal from "../../components/modal/DiaryCreatingModal";
import DiaryCreateFailModal from "../../components/modal/DiaryCreateFailModal";
import AlertModal from "../../components/modal/AlertModal";

export default function DiaryChatPage() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<
    { from: "ai" | "user"; text: string }[]
  >([{ from: "ai", text: "안녕~ 오늘 하루는 어땠는지 이야기해줘!" }]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [limit, setLimit] = useState(0);

  // 모달 상태
  const [creatingModalOpen, setCreatingModalOpen] = useState(false);
  const [failModalOpen, setFailModalOpen] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
  } | null>(null);

  // 대화 불러오기
  // 대화 불러오기
  useEffect(() => {
    const fetchMessages = async () => {
      try {
        const { data } = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/api/chat/session/messages`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
              "Content-Type": "application/json",
            },
          }
        );

        const savedMessages = data.data.chatlog;
        const token = data.data.curToken;

        const formatted = savedMessages.map((msg: any) => ({
          from: msg.role === "USER" ? "user" : "ai",
          text: msg.message,
        }));

        setMessages(formatted);
        setLimit(token); // 초기 curToken을 limit으로 설정
      } catch (err) {
        console.error("이전 대화 불러오기 실패:", err);
      }
    };

    fetchMessages();
  }, []);

  // 메시지 전송
  const handleSend = async () => {
    if (!input.trim() || loading) return;
    setMessages((prev) => [...prev, { from: "user", text: input }]);
    setInput("");
    setLoading(true);

    try {
      const { data } = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/text`,
        { message: input },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      const response = data.data.response;
      const limitVal = data.data.limit;

      setMessages((prev) => [...prev, { from: "ai", text: response }]);
      setLimit(limitVal);
    } catch (err) {
      console.error("API 오류:", err);
      setMessages((prev) => [
        ...prev,
        { from: "ai", text: "서버 오류가 발생했어요. 다시 시도해 주세요." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  // 일기 생성 요청
  const handleCreate = async () => {
    if (limit < 60) {
      setAlert({
        message: "일기를 위한 소중한 이야기를 더 들려주세요",
        type: "fail",
      });
      return;
    }

    setCreatingModalOpen(true);
    setShowConfirm(false);

    try {
      await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/end`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      // 1.5초 후 확인 버튼 생성 + 4초 후 임시 저장으로 이동하기
      setTimeout(() => setShowConfirm(true), 1500);
      setTimeout(() => {
        setCreatingModalOpen(false);
        navigate("/diary/temp");
      }, 4000);
    } catch (err) {
      console.error("일기 생성 실패:", err);
      setCreatingModalOpen(false);
      setFailModalOpen(true);
    }
  };

  // 확인 버튼 클릭 시
  const handleConfirm = () => {
    setCreatingModalOpen(false);
    navigate("/diary/temp");
  };

  return (
    <div className="flex flex-col h-screen bg-white">
      <div className="pt-12" />

      <div className="relative w-full max-w-[320px] mx-auto mb-8">
        <div className="flex items-center justify-between text-gray-500 text-sm mb-1">
          대화량 {/* 60 이하이면 버튼 생성 금지 */}
          <Button
            text="일기 생성"
            type="fill"
            size="S"
            onClick={handleCreate}
            disabled={limit < 60}
          />
        </div>
        <ProgressBar label="" progress={limit} />
        {/* 안내 멘트 */}
        {limit >= 90 ? (
          <div className="text-center text-black text-sm mt-2 font-medium">
            충분한 이야기가 모였어요! 일기를 만들어보세요.
          </div>
        ) : limit >= 60 ? (
          <div className="text-center text-gray-500 text-sm mt-2 font-medium">
            이제 곧 일기를 만들어갈 수 있어요!
          </div>
        ) : (
          <div className="text-center text-gray-400 text-sm mt-2 font-medium">
            일기를 위한 소중한 이야기를 더 들려주세요
          </div>
        )}
      </div>

      <div className="flex-1 w-full max-w-[320px] mx-auto overflow-y-auto">
        <ChatList messages={messages} />
      </div>

      <div className="w-full max-w-[320px] mx-auto py-4 border-gray-200">
        <ChatInput
          value={input}
          onChange={setInput}
          onSend={handleSend}
          loading={loading}
        />
      </div>

      {/* 모달들 */}
      {creatingModalOpen && (
        <DiaryCreatingModal
          showConfirm={showConfirm}
          onConfirm={handleConfirm}
        />
      )}
      {failModalOpen && (
        <DiaryCreateFailModal
          onClose={() => setFailModalOpen(false)}
          onRetry={() => {
            setFailModalOpen(false);
            handleCreate();
          }}
        />
      )}

      {alert && (
        <AlertModal
          message={alert.message}
          type={alert.type}
          onClose={() => setAlert(null)}
        />
      )}

      <div className="pb-24" />
    </div>
  );
}
