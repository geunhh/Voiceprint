// src/pages/diary/DiaryChatPage.tsx
import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import ProgressBar from "../../components/common/ProgressBar";
import ChatList from "../../components/chat/ChatList";
import ChatInput from "../../components/chat/ChatInput";
import Button from "../../components/common/Button";
import { useEffect } from "react";

export default function DiaryChatPage() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<
    { from: "ai" | "user"; text: string }[]
  >([{ from: "ai", text: "안녕~ 오늘 하루는 어땠는지 이야기해줘!" }]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const [limit, setLimit] = useState(0);

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

        const savedMessages = data.data; // [{ role: 'USER'|'SERVER', message: '...' }, ...]
        const formatted = savedMessages.map((msg: any) => ({
          from: msg.role === "USER" ? "user" : "ai",
          text: msg.message,
        }));

        console.log(data);
        setMessages(formatted);
      } catch (err) {
        console.error("이전 대화 불러오기 실패:", err);
      }
    };

    fetchMessages();
  }, []);

  const handleSend = async () => {
    if (!input.trim() || loading) return;

    // 사용자 메시지 추가
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

      // console.log(data);
      const response = data.data.response;
      const limitVal = data.data.limit;

      // 서버 응답 추가
      setMessages((prev) => [...prev, { from: "ai", text: response }]);
      setLimit(limitVal);
    } catch (err) {
      console.error("API 오류:", err);
      setMessages((prev) => [
        ...prev,
        { from: "ai", text: "서버 오류가 발생했어요. 다시 시도해주세요." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    console.log("일기 생성!");
    navigate("/diary/temp");
  };

  return (
    <div className="flex flex-col h-screen bg-white">
      {/* 상단 공간 (Appbar 높이) */}
      <div className="pt-12" />

      {/* ─── 대화량 + 일기 생성 버튼 영역 ─── */}
      <div className="relative w-full max-w-[320px] mx-auto mb-8">
        {/* 레이블 */}
        <div className="flex items-center justify-between text-gray-500 text-sm mb-1">
          대화량{" "}
          <Button
            text="일기 생성"
            type="fill"
            size="S"
            onClick={handleCreate}
          />
        </div>

        {/* 프로그레스 바 */}
        <ProgressBar label="" progress={limit} />
      </div>

      {/* 채팅 리스트 */}
      <div className="flex-1 w-full max-w-[320px] mx-auto overflow-y-auto ">
        <ChatList messages={messages} />
      </div>

      {/* 입력창 고정*/}
      <div className="w-full max-w-[320px] mx-auto py-4 border-gray-200">
        <ChatInput
          value={input}
          onChange={setInput}
          onSend={handleSend}
          loading={loading}
        />
      </div>

      {/* 하단 안전 패딩(Tabbar 높이) */}
      <div className="pb-24" />
    </div>
  );
}
