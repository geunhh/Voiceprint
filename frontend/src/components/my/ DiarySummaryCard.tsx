import { useNavigate } from "react-router";
import emotion1 from "../../assets/temp/emotion1.png";
import emotion2 from "../../assets/temp/emotion2.png";
import emotion3 from "../../assets/temp/emotion3.png";
import emotion4 from "../../assets/temp/emotion4.png";
import emotion5 from "../../assets/temp/emotion5.png";

interface DiarySummaryCardProps {
  date: string;
  emotion: "행복" | "설렘" | "피곤" | "짜증" | "우울";
  title: string;
  diaryId: number;
}

const emotionImageMap: Record<DiarySummaryCardProps["emotion"], string> = {
  행복: emotion1,
  설렘: emotion2,
  피곤: emotion3,
  짜증: emotion4,
  우울: emotion5,
};

function DiarySummaryCard(props: DiarySummaryCardProps) {
  const { date, emotion, title, diaryId } = props;
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
      <img
        src={emotionImage}
        alt={emotion}
        className="w-10 h-10 rounded-full object-cover"
      />
      <div className="flex flex-col text-sm ml-4">
        <span className="text-gray-500">{date}</span>
        <span className="font-semibold text-black">{title}</span>
      </div>
    </div>
  );
}

export default DiarySummaryCard;
