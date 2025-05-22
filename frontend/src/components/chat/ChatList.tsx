// src/components/chat/ChatList.tsx
import React, { useEffect, useRef } from "react";
import ChatBubble, { ChatBubbleProps } from "./ChatBubble";

interface ChatListProps {
  messages: ChatBubbleProps[];
}

export default function ChatList({ messages }: ChatListProps) {
  const containerRef = useRef<HTMLDivElement>(null);

  // 메시지 변경 시 맨 아래로 스크롤
  useEffect(() => {
    const el = containerRef.current;
    if (el) el.scrollTop = el.scrollHeight;
  }, [messages]);

  return (
    <div
      ref={containerRef}
      className="
        h-full      /* 부모 flex-1 높이를 꽉 채움 */
        w-full
        overflow-y-auto
        space-y-4
        px-4
      "
    >
      {messages.map((m, i) => (
        <ChatBubble key={i} {...m} />
      ))}
    </div>
  );
}
