import { useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import sendIcon from "../../assets/icons/send.png";

interface ThemaItemProps {
  id: number;
  title: string;
  description: string;
  imageSrc: string;
  example: string;
  isCustom?: boolean;
  isSelected: boolean;
  onSelect: (id: number) => void;
  onExampleSubmit?: (id: number, value: string) => void;
}

function ThemaItem({
  id,
  title,
  description,
  imageSrc,
  example,
  isCustom = false,
  isSelected,
  onSelect,
  onExampleSubmit,
}: ThemaItemProps) {
  const [inputValue, setInputValue] = useState("");

  const handleClick = () => {
    onSelect(id);
  };

  const handleSend = async () => {
    const trimmed = inputValue.trim();
    if (!trimmed) return;

    onExampleSubmit?.(id, trimmed);

    try {
      if (isCustom) {
        // 항상 새 커스텀 테마 생성
        const res = await axiosInstance.post("/api/thema/create", {
          exampleDiary: trimmed,
        });
        console.log("커스텀 테마 생성 성공:", res.data);
      }
    } catch (error) {
      console.error("커스텀 테마 생성 실패:", error);
    }

    // setInputValue(""); // 필요하면 입력창 초기화
  };

  return (
    <div
      className={`flex w-full flex-col gap-2 rounded-xl border border-yellow-400 px-4 py-4 mb-3 transition-all duration-200 cursor-pointer
        ${isSelected ? "bg-yellow-50" : "bg-white hover:bg-yellow-50"}`}
      onClick={handleClick}
    >
      <div className="flex items-center justify-between">
        <div>
          <p className="font-semibold text-gray-700 text-lg">{title}</p>
          <p className="text-md text-gray-500 mt-1">
            {isCustom ? "작성한 일기로 테마를 만들 수 있어요" : description}
          </p>
        </div>
        <div className="w-16 h-16 flex items-center justify-center">
          <img
            src={imageSrc}
            alt="theme"
            className="max-w-full max-h-full object-contain"
          />
        </div>
      </div>

      {isSelected && (
        <>
          {example && (
            <p className="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap line-clamp-3 mt-1">
              {example}
            </p>
          )}

          {isCustom && (
            <div
              className="flex items-center justify-between rounded-lg border border-gray-300 bg-white px-3 py-2 mt-1"
              onClick={(e) => e.stopPropagation()}
            >
              <input
                type="text"
                placeholder="일기를 입력해주세요"
                className="flex-1 text-sm text-gray-700 placeholder-gray-400 outline-none"
                value={inputValue}
                onChange={(e) => setInputValue(e.target.value)}
              />
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleSend();
                }}
                className="ml-2 w-9 h-9 rounded-lg bg-yellow-400 flex items-center justify-center"
              >
                <img
                  src={sendIcon}
                  alt="send"
                  className="w-5 h-5 object-contain"
                />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default ThemaItem;
