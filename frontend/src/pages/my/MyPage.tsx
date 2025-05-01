import { useState } from "react";
import ToggleButton from "../../components/my/toggleButton";
import DiarySummaryCard from "../../components/my/ DiarySummaryCard";

// 임시 데이터
// 이번 달 일기 목록
const diaries = [
  {
    diaryId: 101,
    title: "벚꽃놀이",
    createdAt: "2025-04-03T15:00:00",
    emotion: "행복",
  },
  {
    diaryId: 102,
    title: "면접 전날이라 긴장돼",
    createdAt: "2025-04-10T22:00:00",
    emotion: "설렘",
  },
  {
    diaryId: 103,
    title: "과제하다가 새벽 3시...",
    createdAt: "2025-04-11T03:10:00",
    emotion: "피곤",
  },
  {
    diaryId: 104,
    title: "요즘 좀 우울한 것 같아",
    createdAt: "2025-04-15T10:00:00",
    emotion: "우울",
  },
  {
    diaryId: 105,
    title: "짜증나는 일이 있었어",
    createdAt: "2025-04-20T19:00:00",
    emotion: "짜증",
  },
];

// 날짜 포맷팅
function formatDate(iso: string): string {
  const date = new Date(iso);
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}.${mm}.${dd}`;
}

export default function MyPage() {
  const [selected, setSelected] = useState("리스트");

  return (
    <div className="p-6 space-y-6">
      <div className="flex justify-center">
        <ToggleButton
          option1="리스트"
          option2="달력"
          selected={selected}
          onClick={setSelected}
        />
      </div>

      {/* 일기 카드 리스트 */}
      {selected === "리스트" && (
        <div className="flex flex-col items-center space-y-3">
          {diaries.map((diary) => (
            <DiarySummaryCard
              key={diary.diaryId}
              date={formatDate(diary.createdAt)}
              title={diary.title}
              emotion={
                diary.emotion as "행복" | "설렘" | "피곤" | "짜증" | "우울"
              }
              diaryId={diary.diaryId}
            />
          ))}
        </div>
      )}
    </div>
  );
}
