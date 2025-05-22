import emotion1 from "../../assets/temp/emotion1.png";
import emotion2 from "../../assets/temp/emotion2.png";
import emotion3 from "../../assets/temp/emotion3.png";
import emotion4 from "../../assets/temp/emotion4.png";
import emotion5 from "../../assets/temp/emotion5.png";
import emotion6 from "../../assets/temp/emotion6.png";

interface WeekEmotionProps {
  emotions: ("행복" | "설렘" | "피로" | "짜증" | "우울" | null)[];
}

const days = ["일", "월", "화", "수", "목", "금", "토"] as const;

const emotionImageMap: Record<
  Exclude<WeekEmotionProps["emotions"][number], null>,
  string
> = {
  행복: emotion1,
  설렘: emotion2,
  피로: emotion3,
  짜증: emotion4,
  우울: emotion5,
};

function WeekEmotion({ emotions }: WeekEmotionProps) {
  return (
    <div className="flex flex-col items-center">
      {/* 요일 */}
      <div className="flex justify-between w-full mb-2 text-gray-500 text-sm">
        {days.map((day) => (
          <span key={day} className="w-8 text-center">
            {day}
          </span>
        ))}
      </div>

      {/* 감정 이미지 */}
      <div className="flex justify-between w-full">
        {emotions.map((emotion, index) => (
          <div key={index} className="w-8 h-8 flex justify-center items-center">
            <img
              src={emotion ? emotionImageMap[emotion] : emotion6}
              alt={emotion ?? "기록 없음"}
              className="w-6 h-6 object-contain"
            />
          </div>
        ))}
      </div>
    </div>
  );
}

export default WeekEmotion;
