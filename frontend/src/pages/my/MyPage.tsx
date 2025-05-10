import axios from "axios";
import { addMonths, format, subMonths } from "date-fns";
import { useEffect, useState } from "react";
import { useSelector } from "react-redux";

import Calendar from "../../components/my/Calendar";
import DiarySummaryCard from "../../components/my/DiarySummaryCard";
import ToggleButton from "../../components/my/ToggleButton";
import UserProfile from "../../components/my/UserProfile";
import { RootState } from "../../store/store";

import back from "../../assets/icons/backYellow.png";
import forward from "../../assets/icons/forwardYellow.png";
import robotCharacter from "../../assets/icons/robotCharacter.png";

// 날짜 포맷팅
function formatDate(iso: string): string {
  const date = new Date(iso);
  const yyyy = date.getFullYear();
  const mm = String(date.getMonth() + 1).padStart(2, "0");
  const dd = String(date.getDate()).padStart(2, "0");
  return `${yyyy}.${mm}.${dd}`;
}

interface Diary {
  diaryId: number;
  title: string;
  createdAt: string;
  emotion: "행복" | "설렘" | "피곤" | "짜증" | "우울";
  content: string;
}

export default function MyPage() {
  const [selected, setSelected] = useState("리스트");
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [diaries, setDiaries] = useState<Diary[]>([]);
  const [loading, setLoading] = useState(false);

  const user = useSelector((state: RootState) => state.user);

  useEffect(() => {
    const fetchDiaries = async () => {
      setLoading(true);
      try {
        const year = currentMonth.getFullYear();
        const month = currentMonth.getMonth() + 1;
        const res = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/diaries/monthly`,
          {
            params: { year, month },
            headers: {
              Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            },
          }
        );
        setDiaries(res.data.data.diaries || []);
      } catch (error) {
        console.error("다이어리 불러오기 실패:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchDiaries();
  }, [currentMonth]);

  return (
    <div className="mt-5">
      {/* 유저 정보 */}
      <div className="p-4 mb-2">
        <UserProfile
          userId={user.userId}
          userName={user.nickname}
          userImage={user.imageUrl}
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
            {diaries.length === 0 ? (
              <div className="flex h-80 flex-col items-center justify-center">
                <img src={robotCharacter} alt="" className="h-32" />
                <p className="text-sm text-gray-400 mt-4">
                  작성된 일기가 없어요
                </p>
              </div>
            ) : (
              diaries.map((diary) => (
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
