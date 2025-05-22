// src/components/diary/DiaryEditCard.tsx
import { useEffect, useRef } from "react";

// 감정 태그 이미지 import
import emotionTag1 from "../../assets/temp/emotionTag1.png";
import emotionTag2 from "../../assets/temp/emotionTag2.png";
import emotionTag3 from "../../assets/temp/emotionTag3.png";
import emotionTag4 from "../../assets/temp/emotionTag4.png";
import emotionTag5 from "../../assets/temp/emotionTag5.png";

interface DiaryEditCardProps {
  dateText: string;
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  title: string;
  content: string;
  onTitleChange: (newTitle: string) => void;
  onContentChange: (newContent: string) => void;
  maxContentLength?: number;
}

const emotionTagMap: Record<string, string> = {
  행복: emotionTag1,
  설렘: emotionTag2,
  피로: emotionTag3,
  짜증: emotionTag4,
  우울: emotionTag5,
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
  const needsScroll = content.length > maxContentLength;
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  useEffect(() => {
    textareaRef.current?.focus();
  }, []);

  const emotionImage = emotionTagMap[emotion];

  return (
    <div className="w-11/12 mx-auto rounded-xl border-2 border-dashed border-yellow-300 bg-white p-4 space-y-4 shadow-sm">
      {/* 날짜·감정 */}
      <div className="flex items-center justify-between">
        <span className="text-lg font-semibold text-yellow-500">
          {dateText}
        </span>
        {emotionImage && (
          <img
            src={emotionImage}
            alt={emotion}
            className="w-12 h-5 object-contain"
          />
        )}
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
          className="w-full rounded-lg border border-yellow-200 bg-yellow-50 px-3 py-2 text-gray-600 placeholder-gray-400 transition focus:border-yellow-400 focus:bg-white focus:outline-none focus:shadow-outline"
        />
      </div>

      {/* 내용 입력 */}
      <div>
        <label className="block mb-1 text-sm font-medium text-gray-400">
          내용
        </label>
        <textarea
          ref={textareaRef}
          value={content}
          onChange={(e) => onContentChange(e.target.value)}
          placeholder="일기 내용을 입력하세요"
          className={`
            w-full rounded-xl border border-yellow-200 bg-yellow-50
            p-3 text-gray-600 placeholder-gray-400 text-sm leading-relaxed
            resize-none transition focus:border-yellow-400 focus:bg-white
            focus:outline-none focus:shadow-outline min-h-60
            ${needsScroll ? "max-h-60 overflow-y-auto" : ""}
          `}
        />
      </div>
    </div>
  );
}
