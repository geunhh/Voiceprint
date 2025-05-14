import { useSelector } from "react-redux";
import { RootState } from "../../store/store";

import userAvatar from "../../assets/icons/chatBlue.png";
import defaultAI from "../../assets/icons/chatYellow.png"; // fallback

export interface ChatBubbleProps {
  from: "ai" | "user";
  text: string;
}

export default function ChatBubble({ from, text }: ChatBubbleProps) {
  // Redux에 저장된 캐릭터 이미지 사용
  const aiAvatar =
    useSelector((state: RootState) => state.character.img) || defaultAI;

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
