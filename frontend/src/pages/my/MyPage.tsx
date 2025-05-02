import { addMonths, format, subMonths } from "date-fns";
import { useState } from "react";
import Calendar from "../../components/my/Calendar";
import DiarySummaryCard from "../../components/my/DiarySummaryCard";
import ToggleButton from "../../components/my/ToggleButton";
import UserProfile from "../../components/my/UserProfile";

import back from "../../assets/icons/backYellow.png";
import forward from "../../assets/icons/forwardYellow.png";
import robotCharacter from "../../assets/icons/robotCharacter.png";

// 임시 데이터
// 유저 정보
const user = {
  userId : 1, 
  userName: '김혜민',
  userImage:"https://i.pinimg.com/736x/a7/ca/36/a7ca369a79ff17fb0ae1c13e72a7a8b4.jpg",
  customThemaId:null
}

// 이번 달 일기 목록
const diaries = [
  {
    diaryId: 101,
    title: "벚꽃놀이",
    createdAt: "2025-04-03T15:00:00",
    emotion: "행복",
    content:"오늘은 신나는 벚꽃놀이 가는 날~! 너무너무 즐거워워"
  },
  {
    diaryId: 102,
    title: "면접 전날이라 긴장돼",
    createdAt: "2025-04-10T22:00:00",
    emotion: "설렘",
    content: "일기 내용내용용"
  },
  {
    diaryId: 103,
    title: "과제하다가 새벽 3시...",
    createdAt: "2025-04-11T03:10:00",
    emotion: "피곤",
    content: "일기 내용내용용"
  },
  {
    diaryId: 104,
    title: "요즘 좀 우울한 것 같아",
    createdAt: "2025-04-15T10:00:00",
    emotion: "우울",
    content: "일기 내용내용용"
  },
  {
    diaryId: 105,
    title: "짜증나는 일이 있었어",
    createdAt: "2025-04-20T19:00:00",
    emotion: "짜증",
    content: "일기 내용내용용"
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
  const [currentMonth, setCurrentMonth] = useState(new Date());

  const filteredDiaries = diaries.filter((diary) => {
    const date = new Date(diary.createdAt);
    return (
      date.getFullYear() === currentMonth.getFullYear() &&
      date.getMonth() === currentMonth.getMonth()
    );
  });

  return (
    <div className="mt-5">
      {/* 유저 정보 */}
      <div className="p-4 mb-2">
        <UserProfile 
          userId={user.userId}
          userName={user.userName}
          userImage={user.userImage}
          customThemaId={user.customThemaId}
        />
      </div>

      {/* 달력 및 일기 리스트 */}
      <div className="px-4 space-y-6">
        <div className="flex justify-between">
          <div className="flex justify-center items-center gap-2">
            <button onClick={() => setCurrentMonth(subMonths(currentMonth, 1))}>
              <img src={back} alt="이전 달" className="w-6 h-auto" />
            </button>
            <span className="text-lg text-yellow-400 font-semibold">
              {format(currentMonth, "yyyy년 M월")}
            </span>
            <button onClick={() => setCurrentMonth(addMonths(currentMonth, 1))}>
              <img src={forward} alt="다음 달" className="w-6 h-auto" />
            </button>
          </div>
          <ToggleButton
            option1="리스트"
            option2="달력"
            selected={selected}
            onClick={setSelected}
          />
        </div>

        {/* 달력 */}
        {selected === "달력" && (
          <div
          className="
            w-full
            flex justify-center
          "
        >
            <Calendar currentMonth={currentMonth} diaries={diaries} />
          </div>
        )}

        {/* 일기 카드 리스트 */}
        {selected === "리스트" && (
          <div className="flex flex-col items-center space-y-3">
            {filteredDiaries.length === 0 ? (
              <div className="flex h-80 flex-col items-center justify-center">
                <img src={robotCharacter} alt="" className="h-32" />
                <p className="text-sm text-gray-400 mt-4">
                  작성된 일기가 없어요
                </p>
              </div>
            ) : (
              filteredDiaries.map((diary) => (
                <DiarySummaryCard
                  key={diary.diaryId}
                  date={formatDate(diary.createdAt)}
                  title={diary.title}
                  emotion={
                    diary.emotion as "행복" | "설렘" | "피곤" | "짜증" | "우울"
                  }
                  diaryId={diary.diaryId}
                  content={diary.content}
                />
              ))
            )}
          </div>
        )}

      </div>
    </div>
  );
}
