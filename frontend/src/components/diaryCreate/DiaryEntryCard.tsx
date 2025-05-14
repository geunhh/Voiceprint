import emotionTag1 from "../../assets/temp/emotionTag1.png";
import emotionTag2 from "../../assets/temp/emotionTag2.png";
import emotionTag3 from "../../assets/temp/emotionTag3.png";
import emotionTag4 from "../../assets/temp/emotionTag4.png";
import emotionTag5 from "../../assets/temp/emotionTag5.png";

interface DiaryEntryCardProps {
  dateText: string; // "4월 24일 (목)"
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  title: string;
  content: string;
}

const emotionImageMap: Record<DiaryEntryCardProps["emotion"], string> = {
  행복: emotionTag1,
  설렘: emotionTag2,
  피로: emotionTag3,
  짜증: emotionTag4,
  우울: emotionTag5,
};

export default function DiaryEntryCard({
  dateText,
  emotion,
  title,
  content,
}: DiaryEntryCardProps) {
  // 대략 160자 이상일 때 스크롤
  const needsScroll = content.length > 160;

  const emotionImage = emotionImageMap[emotion];

  return (
    <div className="w-11/12 mx-auto rounded-xl border border-yellow-400 bg-white p-4 space-y-4">
      {/* 날짜 · 감정 라벨 */}
      <div className="flex items-center justify-between">
        <span className="text-lg font-bold text-yellow-600">{dateText}</span>
        <img src={emotionImage} className="w-11" />
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
