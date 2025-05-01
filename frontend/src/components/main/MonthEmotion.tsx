import emotion1 from "../../assets/temp/emotion1.png";
import emotion2 from "../../assets/temp/emotion2.png";
import emotion3 from "../../assets/temp/emotion3.png";
import emotion4 from "../../assets/temp/emotion4.png";
import emotion5 from "../../assets/temp/emotion5.png";

interface MonthEmotionItem {
  emotion: string;
  count: number;
}

interface MonthEmotionProps {
  emotions: MonthEmotionItem[];
}

const emotionColors: Record<string, string> = {
  행복: "bg-[#F59CA9] text-[#F59CA9]",
  설렘: "bg-[#F8B95E] text-[#F8B95E]",
  피로: "bg-[#FCEB8F] text-[#FCEB8F]",
  짜증: "bg-[#95D971] text-[#95D971]",
  우울: "bg-[#94C7F2] text-[#94C7F2]",
};

const emotionIcons: Record<string, string> = {
  행복: emotion1,
  설렘: emotion2,
  피로: emotion3,
  짜증: emotion4,
  우울: emotion5,
};

function MonthEmotion({ emotions }: MonthEmotionProps) {
  const total = emotions.reduce((sum, item) => sum + item.count, 0);

  return (
    <div className="flex justify-between items-end gap-4 w-full">
      {emotions.map(({ emotion, count }) => {
        const ratio = total === 0 ? 0 : (count / total) * 100;
        const height = Math.max(ratio * 1.5, 8);

        return (
          <div key={emotion} className="flex flex-col items-center flex-1">
            {/* 퍼센트 */}
            <span
              className={`mb-1 text-sm font-semibold ${
                emotionColors[emotion]?.split(" ")[1]
              }`}
            >
              {Math.round(ratio)}%
            </span>
            {/* 막대 그래프 */}
            <div
              className={`w-4 rounded-full ${
                emotionColors[emotion]?.split(" ")[0]
              }`}
              style={{ height: `${height}px` }}
            />
            {/* 아이콘 */}
            <img
              src={emotionIcons[emotion]}
              alt={emotion}
              className="w-10 h-10 mt-1"
            />
            {/* 라벨 */}
            <p className="text-sm text-gray-500 mt-1">{emotion}</p>
          </div>
        );
      })}
    </div>
  );
}

export default MonthEmotion;
