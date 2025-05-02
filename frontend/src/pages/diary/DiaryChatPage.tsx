// src/pages/diary/DiaryChatPage.tsx
import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import ProgressBar from "../../components/common/ProgressBar";
import ChatList from "../../components/chat/ChatList";
import ChatInput from "../../components/chat/ChatInput";
import Button from "../../components/common/Button";

export default function DiaryChatPage() {
  const navigate = useNavigate();
  const [messages, setMessages] = useState<
    { from: "ai" | "user"; text: string }[]
  >([{ from: "ai", text: "안녕~ 오늘 하루는 어땠는지 이야기해줘!" }]);
  const [input, setInput] = useState("");
  const [loading, setLoading] = useState(false);
  const useDummy = true;

  const handleSend = async () => {
    if (!input.trim() || loading) return;
    setMessages((prev) => [...prev, { from: "user", text: input }]);
    setInput("");
    setLoading(true);

    if (useDummy) {
      setTimeout(() => {
        setMessages((prev) => [
          ...prev,
          { from: "ai", text: "더미 응답입니다!" },
        ]);
        setLoading(false);
      }, 500);
      return;
    }

    try {
      const { data } = await axios.post("/api/diary/chat", { message: input });
      setMessages((prev) => [...prev, { from: "ai", text: data.data.reply }]);
    } catch {
      setMessages((prev) => [
        ...prev,
        { from: "ai", text: "서버 오류로 더미 응답입니다." },
      ]);
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    console.log("일기 생성!");
    navigate("/diary/temp"); // 예시
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
        <ProgressBar label="" progress={45} />
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
