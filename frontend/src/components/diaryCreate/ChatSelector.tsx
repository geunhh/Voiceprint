import React, { useState, useEffect } from "react";
import chatBlack from "../../assets/icons/chatBlack.png";
import chatBlue from "../../assets/icons/chatBlue.png";
import chatPink from "../../assets/icons/chatPink.png";
import chatRed from "../../assets/icons/chatRed.png";
import chatYellow from "../../assets/icons/chatYellow.png";

interface ChatSelectorProps {
  onSelect: (character: { img: string; name: string; tag: string }) => void;
}

const characters = [
  { img: chatBlack, name: "따분이", tag: "#시니컬  #로봇바이브" },
  { img: chatBlue, name: "맑음이", tag: "#명랑  #쿨톤" },
  { img: chatPink, name: "설렘이", tag: "#러블리  #핑크러버" },
  { img: chatRed, name: "열정이", tag: "#에너지  #리더" },
  { img: chatYellow, name: "햇살이", tag: "#긍정  #따뜻함" },
];

export default function ChatSelector({ onSelect }: ChatSelectorProps) {
  const [currentIndex, setCurrentIndex] = useState(0);
  const [fade, setFade] = useState(false);

  useEffect(() => {
    onSelect(characters[currentIndex]);
  }, [currentIndex, onSelect]);

  const handlePrev = () => {
    setFade(true);
    setTimeout(() => {
      setCurrentIndex((prev) =>
        prev === 0 ? characters.length - 1 : prev - 1
      );
      setFade(false);
    }, 200);
  };

  const handleNext = () => {
    setFade(true);
    setTimeout(() => {
      setCurrentIndex((prev) =>
        prev === characters.length - 1 ? 0 : prev + 1
      );
      setFade(false);
    }, 200);
  };

  const currentCharacter = characters[currentIndex];

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
          className={`w-55 h-55 rounded-full flex items-center justify-center 
                      transition-all duration-300 ease-in-out
                      ${fade ? "opacity-90" : "opacity-100"}
                      shadow-lg bg-100`}
        >
          <img
            src={currentCharacter.img}
            alt={currentCharacter.name}
            className="w-55 h-55 object-contain"
          />
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
        {currentCharacter.name}
      </div>
      <div className="text-lg text-gray-500 mt-1">{currentCharacter.tag}</div>
    </div>
  );
}
