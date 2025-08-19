import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import closeIcon from "../../assets/icons/close.png";
import { ChatBubbleProps } from "../chat/ChatBubble";
import ChatList from "../chat/ChatList";

interface ChatHistoryModalProps {
  onClose: () => void;
}

function ChatHistoryModal({ onClose }: ChatHistoryModalProps) {
  const { diaryId } = useParams<{ diaryId: string }>();
  const [messages, setMessages] = useState<ChatBubbleProps[]>([]);

  useEffect(() => {
    const fetchChat = async () => {
      try {
        const res = await axiosInstance.get(
          `/api/diaries/diary/${diaryId}/chat`
        );

        const rawMessages = res.data.data;

        const converted: ChatBubbleProps[] = rawMessages.map((m: any) => ({
          from: m.role === "assistant" ? "ai" : "user",
          text: m.content,
        }));

        setMessages(converted);
        console.log("채팅 기록 불러오기 성공!", rawMessages);
      } catch (err) {
        console.error("채팅 기록 불러오기 실패", err);
      }
    };

    if (diaryId) fetchChat();
  }, [diaryId]);

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center">
      <div className="w-4/5 max-w-[320px] rounded-xl bg-white flex flex-col py-6 relative max-h-[90vh] overflow-y-auto">
        {/* 닫기 버튼 */}
        <img
          src={closeIcon}
          alt="닫기버튼"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />

        {/* 제목 */}
        <p className="text-xl font-bold text-center mb-4">대화 기록</p>

        {/* 채팅 리스트 */}
        <div className="h-[60vh] px-4">
          <ChatList messages={messages} />
        </div>
      </div>
    </div>
  );
}

export default ChatHistoryModal;
