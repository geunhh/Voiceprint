// src/components/diaryCreate/ChatSelector.tsx
import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";

// 로컬에 저장된 기본 아이콘들
import chatBlack from "../../assets/icons/chatBlack.png";
import chatBlue from "../../assets/icons/chatBlue.png";
import chatPink from "../../assets/icons/chatPink.png";
import chatRed from "../../assets/icons/chatRed.png";
import chatYellow from "../../assets/icons/chatYellow.png";

interface Chatbot {
  id: number;
  name: string;
  description: string; // e.g. "#시니컬,#로봇바이브"
  imageUrl: string | null;
}

interface ChatSelectorProps {
  onSelect: (character: {
    id: number;
    img: string;
    name: string;
    tag: string;
  }) => void;
}

export default function ChatSelector({ onSelect }: ChatSelectorProps) {
  const [chatbots, setChatbots] = useState<Chatbot[]>([]);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [fade, setFade] = useState(false);

  // 로컬 아이콘 매핑 (챗봇 이름 → 아이콘)
  const localIcons: Record<string, string> = {
    따분이: chatBlack,
    맑음이: chatBlue,
    설렘이: chatPink,
    열정이: chatRed,
    햇살이: chatYellow,
  };

  // 1) 마운트 시 서버에서 챗봇 리스트 가져오기
  useEffect(() => {
    async function fetchChatbots() {
      try {
        const res = await axiosInstance.get("/api/chatbot");

        if (!res.data || !res.data.data) {
          console.error("챗봇 데이터가 비어있습니다:", res.data);
          return;
        }

        const { recentChatbotId, chatbots } = res.data.data;

        setChatbots(chatbots);

        // recentChatbotId에 해당하는 챗봇의 index 찾기
        const index = chatbots.findIndex(
          (bot: Chatbot) => bot.id === recentChatbotId
        );

        // 유효한 인덱스일 경우에만 currentIndex 설정
        if (index !== -1) {
          setCurrentIndex(index);
        } else {
          setCurrentIndex(0); // fallback
        }
      } catch (err) {
        console.error("챗봇 목록 조회 실패:", err);
      }
    }

    fetchChatbots();
  }, []);

  // 2) 현재 인덱스 변경 시 onSelect 호출
  useEffect(() => {
    if (chatbots.length === 0) return;

    const bot = chatbots[currentIndex];
    // API imageUrl 이 null 이면 localIcons 에서 찾아서 대체
    const img = bot.imageUrl || localIcons[bot.name] || "";
    const tag = bot.description.split(",").join(" ");

    onSelect({
      id: bot.id,
      img,
      name: bot.name,
      tag,
    });
  }, [chatbots, currentIndex, onSelect]);

  const handlePrev = () => {
    if (chatbots.length === 0) return;
    setFade(true);
    setTimeout(() => {
      setCurrentIndex((i) => (i === 0 ? chatbots.length - 1 : i - 1));
      setFade(false);
    }, 200);
  };

  const handleNext = () => {
    if (chatbots.length === 0) return;
    setFade(true);
    setTimeout(() => {
      setCurrentIndex((i) => (i === chatbots.length - 1 ? 0 : i + 1));
      setFade(false);
    }, 200);
  };

  if (chatbots.length === 0) {
    return (
      <div className="flex items-center justify-center h-[45vh]">
        <span className="text-gray-500">로딩 중...</span>
      </div>
    );
  }

  const current = chatbots[currentIndex];
  const imgSrc = current.imageUrl || localIcons[current.name] || "";

  return (
    <div className="flex flex-col items-center justify-center h-[45vh]">
      <div className="flex items-center justify-center mt-12">
        {/* 왼쪽 화살표 */}
        <button
          onClick={handlePrev}
          className="text-4xl text-gray-200 mx-10 transition-colors hover:text-yellow-400"
        >
          {"<"}
        </button>

        {/* 캐릭터 이미지 */}
        <div
          className={`
            w-56 h-56 rounded-full flex items-center justify-center 
            transition-opacity duration-300 ease-in-out
            ${fade ? "opacity-80" : "opacity-100"}
            shadow-lg bg-white
          `}
        >
          {imgSrc ? (
            <img
              src={imgSrc}
              alt={current.name}
              className="w-56 h-56 object-contain"
            />
          ) : (
            <span className="text-gray-400">No Image</span>
          )}
        </div>

        {/* 오른쪽 화살표 */}
        <button
          onClick={handleNext}
          className="text-4xl text-gray-200 mx-10 transition-colors hover:text-yellow-400"
        >
          {">"}
        </button>
      </div>

      {/* 캐릭터 이름 & 태그 */}
      <div className="text-xl font-semibold text-gray-900 mt-8">
        {current.name}
      </div>
      <div className="text-lg text-gray-500 mt-1">
        {current.description.split(",").join(" ")}
      </div>
    </div>
  );
}
