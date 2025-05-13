// src/components/chat/ChatBubble.tsx
// 한 줄 한 줄의 채팅을 그려주는 컴포넌트

import React from "react";
import aiAvatar from "../../assets/icons/chatYellow.png"; // ai 챗봇 선택값
import userAvatar from "../../assets/icons/chatBlue.png"; // user 프로필

export interface ChatBubbleProps {
  from: "ai" | "user";
  text: string;
}

export default function ChatBubble({ from, text }: ChatBubbleProps) {
  return (
    <div
      className={`flex items-end ${from === "ai" ? "justify-start" : "justify-end"}`}
    >
      {from === "ai" && (
        <img src={aiAvatar} alt="AI" className="w-8 h-8 rounded-full mr-2" />
      )}
      <div
        className={`px-4 py-2 rounded-xl max-w-[80%] whitespace-pre-wrap text-sm ${
          from === "ai"
            ? "bg-lightmint text-gray-800"
            : "bg-yellow-100 text-gray-800"
        }`}
      >
        {text}
      </div>
      {from === "user" && (
        <img
          src={userAvatar}
          alt="User"
          className="w-8 h-8 rounded-full ml-2"
        />
      )}
    </div>
  );
}
