import {
  addDays,
  endOfMonth,
  endOfWeek,
  format,
  isSameDay,
  isSameMonth,
  isToday,
  startOfMonth,
  startOfWeek,
} from "date-fns";
import { ko } from "date-fns/locale";
import { useNavigate } from "react-router-dom";

import add from "../../assets/icons/add.png";
import emotion1 from "../../assets/temp/emotion1.png";
import emotion2 from "../../assets/temp/emotion2.png";
import emotion3 from "../../assets/temp/emotion3.png";
import emotion4 from "../../assets/temp/emotion4.png";
import emotion5 from "../../assets/temp/emotion5.png";
import emotion6 from "../../assets/temp/emotion6.png";

interface Diary {
  diaryId: number;
  title: string;
  createdAt: string;
  emotion: string;
}

interface CalendarProps {
  currentMonth: Date;
  diaries: Diary[];
}

const emotionIcons: Record<string, string> = {
  행복: emotion1,
  설렘: emotion2,
  피곤: emotion3,
  짜증: emotion4,
  우울: emotion5,
};
function Calendar({ currentMonth, diaries }: CalendarProps) {
  const navigate = useNavigate();

  const monthStart = startOfMonth(currentMonth);
  const monthEnd = endOfMonth(currentMonth);
  const startDate = startOfWeek(monthStart, { locale: ko });
  const endDate = endOfWeek(monthEnd, { locale: ko });

  const rows = [];
  let days = [];
  let day = startDate;

  while (day <= endDate) {
    for (let i = 0; i < 7; i++) {
      const thisDate = day;
      const formatted = format(thisDate, "d");
      const diary = diaries.find((d) =>
        isSameDay(new Date(d.createdAt), thisDate)
      );

      const isCurrent = isSameMonth(thisDate, currentMonth);
      const isTodayDate = isToday(thisDate);

      let icon = null;
      let onClick: (() => void) | null = null;

      if (isTodayDate) {
        icon = add;
        onClick = () => navigate("/diary/setting/friend");
      } else if (diary) {
        icon = emotionIcons[diary.emotion];
        onClick = () => navigate(`/diary/${diary.diaryId}`);
      } else {
        icon = emotion6;
        onClick = null;
      }

      days.push(
        <div
          key={thisDate.toString()}
          onClick={onClick ?? undefined}
          className={`flex-1 text-sm flex flex-col items-center justify-center ${
            !isCurrent ? "text-gray-200" : "text-gray-500"
          } ${onClick ? "cursor-pointer" : "cursor-default"}`}
          style={{ height: "var(--cell-height, 64px)" }}
        >
          <div>{formatted}</div>
          {icon && (
            <img
              src={icon}
              alt=""
              className="w-6 h-6 mt-1 select-none pointer-events-none"
            />
          )}
        </div>
      );

      day = addDays(day, 1);
    }

    rows.push(
      <div key={day.toString()} className="flex gap-x-6">
        {days}
      </div>
    );
    days = [];
  }

  return (
    <div className="space-y-2 calendar-compact">
      {/* 요일 헤더 */}
      <div className="flex justify-between text-sm text-gray-500 gap-x-6">
        {["일", "월", "화", "수", "목", "금", "토"].map((d) => (
          <div className="flex-1 text-center" key={d}>
            {d}
          </div>
        ))}
      </div>

      {/* 날짜들 */}
      <div className="space-y-2">{rows}</div>
    </div>
  );
}

export default Calendar;
