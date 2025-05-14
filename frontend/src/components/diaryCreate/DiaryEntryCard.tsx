/* src/components/diary/DiaryEntryCard.tsx */

interface DiaryEntryCardProps {
  dateText: string; // "4월 24일 (목)"
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  title: string;
  content: string;
}

const badgeColor: Record<string, string> = {
  행복: "bg-pink-50 text-pink-600 border-pink-300",
  설렘: "bg-yellow-50 text-yellow-600 border-yellow-300",
  피로: "bg-blue-50 text-blue-600 border-blue-300",
  짜증: "bg-red-50 text-red-600 border-red-300",
  우울: "bg-gray-50 text-gray-600 border-gray-300",
};

export default function DiaryEntryCard({
  dateText,
  emotion,
  title,
  content,
}: DiaryEntryCardProps) {
  // 대략 160자 이상일 때 스크롤
  const needsScroll = content.length > 160;

  return (
    <div className="w-11/12 mx-auto rounded-xl border border-yellow-400 bg-white p-4 space-y-4">
      {/* 날짜 · 감정 라벨 */}
      <div className="flex items-center justify-between">
        <span className="text-lg font-bold text-yellow-600">{dateText}</span>
        <span
          className={`px-3 py-0.5 text-sm rounded-full border ${badgeColor[emotion]}`}
        >
          {emotion}
        </span>
      </div>

      {/* 제목 */}
      <div className="bg-yellow-50 rounded-lg p-3">
        <p className="font-medium">{title}</p>
      </div>

      {/* 내용 (길면 스크롤) */}
      <div
        className={`
          bg-yellow-50 rounded-xl p-4
          text-gray-700 text-sm leading-relaxed
          whitespace-pre-wrap
          min-h-72
          ${needsScroll ? "max-h-72 overflow-y-auto" : ""}
        `}
      >
        {content}
      </div>
    </div>
  );
}
