interface WeekEmotionItem  {
    emotion: string | null;
    emotionImage: string;
};

interface WeekEmotionProps  {
    emotions: WeekEmotionItem[];
};

const days = ["일", "월", "화", "수", "목", "금", "토"] as const



function WeekEmotion(props : WeekEmotionProps) {
    const {emotions} = props

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
            {emotions.map(({ emotionImage, emotion }, index) => (
              <div key={index} className="w-8 h-8 flex justify-center items-center">
                <img
                  src={emotionImage}
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

    
