import { addMonths, format, subMonths } from "date-fns";
import { useEffect, useRef, useState } from "react";
import { useSelector } from "react-redux";
import axiosInstance from "../../api/axiosInstance";

import Calendar from "../../components/my/Calendar";
import DiaryCard from "../../components/my/DiaryCard";
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
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  content: string;
}

export default function MyPage() {
  const [selected, setSelected] = useState("달력");
  const [currentMonth, setCurrentMonth] = useState(new Date());
  const [diaries, setDiaries] = useState<Diary[]>([]);

  const user = useSelector((state: RootState) => state.user);

  const [allDiaries, setAllDiaries] = useState<Diary[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef(null);

  // 전체 일기 불러오기
  const fetchAllDiaries = async (cursor?: number) => {
    try {
      const res = await axiosInstance.get("/api/diaries/me/all", {
        params: cursor ? { cursor } : {},
      });

      const newDiaries: Diary[] = res.data.data.diaries;
      setAllDiaries((prev) => {
        const existingIds = new Set(prev.map((d) => d.diaryId));
        const filtered = newDiaries.filter((d) => !existingIds.has(d.diaryId));
        return [...prev, ...filtered];
      });
      setNextCursor(res.data.data.nextCursor);
      setHasMore(res.data.data.nextCursor !== null);
      // console.log("전체 일기 불러오기: ", res.data.data);
    } catch (e) {
      console.error("전체 일기 불러오기 실패", e);
    }
  };

  useEffect(() => {
    fetchAllDiaries();
  }, []);

  useEffect(() => {
    const currentRef = observerRef.current;
    if (!currentRef) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore) {
          fetchAllDiaries(nextCursor!);
        }
      },
      { threshold: 1 }
    );

    observer.observe(currentRef);

    return () => {
      observer.disconnect();
    };
  }, [nextCursor, hasMore]);

  // 월별 일기 불러오기
  useEffect(() => {
    const fetchDiaries = async () => {
      try {
        const year = currentMonth.getFullYear();
        const month = currentMonth.getMonth() + 1;
        const res = await axiosInstance.get("/api/diaries/monthly", {
          params: { year, month },
        });
        setDiaries(res.data.data.diaries || []);
      } catch (error) {
        console.error("다이어리 불러오기 실패:", error);
      }
    };

    fetchDiaries();
  }, [currentMonth]);

  if (!user.userId) return;

  return (
    <div className="mt-5 p-4">
      {/* 유저 정보 */}
      <div className="mb-5">
        <UserProfile
          userId={user.userId}
          userName={user.nickname}
          userImage={user.imageUrl}
        />
      </div>

      {/* 달력 및 일기 리스트 */}
      <div className="space-y-6 mb-3">
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
          <div className="w-full">
            <Calendar currentMonth={currentMonth} diaries={diaries} />
          </div>
        )}

        {/* 일기 카드 리스트 */}
        {selected === "리스트" && (
          <div className="max-h-96 overflow-y-auto flex flex-col items-center space-y-3 custom-scroll">
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
                    diary.emotion as "행복" | "설렘" | "피로" | "짜증" | "우울"
                  }
                  diaryId={diary.diaryId}
                  content={diary.content}
                />
              ))
            )}
          </div>
        )}
      </div>

      {/* 내 말자국 */}
      {allDiaries.length > 0 && (
        <div className="pb-24">
          <p className="text-yellow-400 font-semibold mb-2">내 말자국</p>
          <div className="grid grid-cols-3 gap-4">
            {allDiaries.map((diary) => (
              <DiaryCard
                key={diary.diaryId}
                diaryId={diary.diaryId}
                title={diary.title}
                createdAt={diary.createdAt}
                emotion={diary.emotion}
              />
            ))}

            {hasMore && (
              <div
                ref={observerRef}
                className="col-span-3 h-10 flex justify-center items-center"
              >
                <p className="text-gray-400 text-sm">불러오는 중...</p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}
