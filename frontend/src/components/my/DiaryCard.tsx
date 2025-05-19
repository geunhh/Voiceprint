import { useNavigate } from "react-router-dom";
import emotion1 from "../../assets/temp/emotion1.png";
import emotion2 from "../../assets/temp/emotion2.png";
import emotion3 from "../../assets/temp/emotion3.png";
import emotion4 from "../../assets/temp/emotion4.png";
import emotion5 from "../../assets/temp/emotion5.png";

interface DiaryCardProps {
  diaryId: number;
  title: string;
  createdAt: string;
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
}

const emotionIcons = {
  행복: { img: emotion1, bg: "bg-[#FFA9A9]", border: "border-[#FFA9A9]/30" },
  설렘: { img: emotion2, bg: "bg-[#FFBA66]", border: "border-[#FFBA66]/30" },
  피로: { img: emotion3, bg: "bg-[#FFE792]", border: "border-[#FFE792]/30" },
  짜증: { img: emotion4, bg: "bg-[#91DD4B]", border: "border-[#91DD4B]/30" },
  우울: { img: emotion5, bg: "bg-[#7DBEFF]", border: "border-[#7DBEFF]/30" },
};

function formatDate(dateString: string) {
  const date = new Date(dateString);
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}.${mm}.${dd}`;
}

export default function DiaryCard({
  diaryId,
  title,
  createdAt,
  emotion,
}: DiaryCardProps) {
  const navigate = useNavigate();
  const { img, bg, border } = emotionIcons[emotion];

  return (
    <div
      onClick={() => navigate(`/diary/${diaryId}`)}
      className={`w-28 p-3 rounded-2xl border ${border} hover:shadow-md cursor-pointer transition transform max-[375px]:scale-90`}
    >
      <div
        className={`w-full h-16 rounded-lg flex items-center justify-center ${bg}`}
      >
        <img src={img} alt={emotion} className="w-12 h-12" />
      </div>
      <p className="text-xs text-gray-500 mt-2">{formatDate(createdAt)}</p>
      <p className="text-xs font-semibold text-gray-700 truncate">{title}</p>
    </div>
  );
}
