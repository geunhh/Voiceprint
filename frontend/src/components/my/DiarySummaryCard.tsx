import { useNavigate } from "react-router";
import emotionTag1 from "../../assets/temp/emotionTag1.png";
import emotionTag2 from "../../assets/temp/emotionTag2.png";
import emotionTag3 from "../../assets/temp/emotionTag3.png";
import emotionTag4 from "../../assets/temp/emotionTag4.png";
import emotionTag5 from "../../assets/temp/emotionTag5.png";

interface DiarySummaryCardProps {
  date: string;
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  title: string;
  diaryId: number;
  content: string;
}

const emotionImageMap: Record<DiarySummaryCardProps["emotion"], string> = {
  행복: emotionTag1,
  설렘: emotionTag2,
  피로: emotionTag3,
  짜증: emotionTag4,
  우울: emotionTag5,
};

function DiarySummaryCard(props: DiarySummaryCardProps) {
  const { date, emotion, title, diaryId, content } = props;
  const emotionImage = emotionImageMap[emotion];

  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/diary/${diaryId}`);
  };

  return (
    <div
      className="flex mx-auto rounded-xl border border-yellow-400 bg-white p-4 w-11/12"
      onClick={handleClick}
    >
      <div className="flex-col">
        <div className="flex-col gap-2 text-sm">
          <span className="text-gray-500">{date}</span>
          <div className="flex gap-2">
            <span className="font-semibold text-gray-700">{title}</span>
            <img src={emotionImage} className="w-11" />
          </div>
        </div>
        <div>
          <span className="text-gray-500 text-sm line-clamp-2">{content}</span>
        </div>
      </div>
    </div>
  );
}

export default DiarySummaryCard;
