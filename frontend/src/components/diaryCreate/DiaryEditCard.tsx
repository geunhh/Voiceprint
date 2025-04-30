// src/components/diary/DiaryEditCard.tsx
import React from "react";

interface DiaryEditCardProps {
  dateText: string; // "4월 24일 (목)"
  emotion: "행복" | "기쁨" | "슬픔" | "화남" | "그냥그래";
  title: string;
  content: string;
  onTitleChange: (newTitle: string) => void;
  onContentChange: (newContent: string) => void;
  maxContentLength?: number; // 스크롤 기준 글자 수(기본 160자)
}

const badgeColor: Record<string, string> = {
  행복: "bg-pink-50 text-pink-500 border-pink-200",
  기쁨: "bg-yellow-50 text-yellow-500 border-yellow-200",
  슬픔: "bg-blue-50 text-blue-500 border-blue-200",
  화남: "bg-red-50 text-red-500 border-red-200",
  그냥그래: "bg-gray-50 text-gray-500 border-gray-200",
};

export default function DiaryEditCard({
  dateText,
  emotion,
  title,
  content,
  onTitleChange,
  onContentChange,
  maxContentLength = 160,
}: DiaryEditCardProps) {
  // 내용 길이에 따라 스크롤 필요 여부
  const needsScroll = content.length > maxContentLength;

  return (
    <div className="w-11/12 mx-auto rounded-xl border-2 border-dashed border-yellow-300 bg-white p-4 space-y-4 shadow-sm">
      {/* 날짜·감정 */}
      <div className="flex items-center justify-between">
        <span className="text-lg font-semibold text-yellow-500">
          {dateText}
        </span>
        <span
          className={`px-3 py-0.5 text-sm rounded-full border ${badgeColor[emotion]}`}
        >
          {emotion}
        </span>
      </div>

      {/* 제목 입력 */}
      <div>
        <label className="block mb-1 text-sm font-medium text-gray-400">
          제목
        </label>
        <input
          type="text"
          value={title}
          onChange={(e) => onTitleChange(e.target.value)}
          placeholder="제목을 입력하세요"
          className="
            w-full
            rounded-lg
            border
            border-yellow-200
            bg-yellow-50
            px-3 py-2
            text-gray-600
            placeholder-gray-400
            transition
            focus:border-yellow-400
            focus:bg-white          /* 포커스 시 배경 흰색 */
            focus:outline-none
            focus:shadow-outline
          "
        />
      </div>

      {/* 내용 입력 */}
      <div>
        <label className="block mb-1 text-sm font-medium text-gray-400">
          내용
        </label>
        <textarea
          value={content}
          onChange={(e) => onContentChange(e.target.value)}
          placeholder="일기 내용을 입력하세요"
          className={`
            w-full
            rounded-xl
            border
            border-yellow-200
            bg-yellow-50
            p-3
            text-gray-600
            placeholder-gray-400
            text-sm
            leading-relaxed
            resize-none
            transition
            focus:border-yellow-400
            focus:bg-white          /* 포커스 시 배경 흰색 */
            focus:outline-none
            focus:shadow-outline
            min-h-60
            ${needsScroll ? "max-h-60 overflow-y-auto" : ""}
          `}
        />
      </div>
    </div>
  );
}
