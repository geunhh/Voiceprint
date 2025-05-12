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

      if (!isCurrent) {
        // 이번 달이 아닌 날짜는 흐리게
        days.push(
          <div
            key={thisDate.toString()}
            className="flex-1 text-sm flex flex-col items-center justify-center text-gray-300 opacity-30 cursor-default"
            style={{ height: "var(--cell-height, 64px)" }}
          >
            <div className="px-2 py-1">{formatted}</div>
            {icon && (
              <img
                src={icon}
                alt=""
                className="w-6 h-6 mt-1 select-none pointer-events-none"
              />
            )}
          </div>
        );
      } else {
        // 이번 달 날짜
        days.push(
          <div
            key={thisDate.toString()}
            onClick={onClick ?? undefined}
            className={`flex-1 text-sm flex flex-col items-center justify-center text-gray-500 ${
              onClick ? "cursor-pointer" : "cursor-default"
            }`}
            style={{ height: "var(--cell-height, 64px)" }}
          >
            <div
              className={`px-2 py-1 ${
                isTodayDate ? " font-semibold text-yellow-400" : ""
              }`}
            >
              {formatted}
            </div>
            {icon && (
              <img
                src={icon}
                alt=""
                className="w-6 h-6 mt-1 select-none pointer-events-none"
              />
            )}
          </div>
        );
      }

      day = addDays(day, 1);
    }

    rows.push(
      <div key={day.toString()} className="grid grid-cols-7 gap-y-2">
        {days}
      </div>
    );

    days = [];
  }

  return (
    <div className="space-y-2">
      {/* 요일 헤더 */}
      <div className="grid grid-cols-7 text-sm text-gray-500 text-center">
        {["일", "월", "화", "수", "목", "금", "토"].map((d) => (
          <div key={d}>{d}</div>
        ))}
      </div>

      {/* 날짜들 */}
      <div className="space-y-2">{rows}</div>
    </div>
  );
}

export default Calendar;
