import { useEffect, useRef, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { useNavigate } from "react-router";
import axiosInstance from "../api/axiosInstance";
import notificationIcon from "../assets/icons/notification.png";
import DiaryPreview from "../components/common/DiaryPreview";
import MonthEmotion from "../components/main/MonthEmotion";
import TodayQuestion from "../components/main/TodayQuestion";
import WeekEmotion from "../components/main/WeekEmotion";
import NotificationModal from "../components/modal/NotificationModal";
import { RootState } from "../store/store";
import { setUser } from "../store/userSlice";

// import QuestionCharacter from "../assets/icons/questionCharacter.png";
import robotCharacter from "../assets/icons/robotCharacter.png";

// import chatAi from "../assets/intro/chatAI.png";
// import chatUser from "../assets/intro/chatUser.png";

interface Diary {
  groupId: number;
  diaryId: number;
  title: string;
  content: string;
  createdAt: string;
  profileUrl: string;
  nickname: string;
}

type EmotionType = "행복" | "설렘" | "피로" | "짜증" | "우울";
interface EmotionCount {
  emotion: EmotionType;
  count: number;
}

export default function MainPage() {
  const dispatch = useDispatch();
  const user = useSelector((state: RootState) => state.user); // Redux에서 유저 정보 가져오기
  const [weekEmotions, setWeekEmotions] = useState<(EmotionType | null)[]>([]);
  const [monthEmotions, setMonthEmotions] = useState<EmotionCount[]>([]);
  const [reminderSetting, setReminderSetting] = useState<true | false | null>(
    null
  );
  const [todayQuestion, setTodayQuestion] = useState("");

  const navigate = useNavigate();

  const [showModal, setShowModal] = useState(false);

  const [hasWrittenDiary, setHasWrittenDiary] = useState<boolean | null>(null);

  const [diaries, setDiaries] = useState<Diary[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [hasMore, setHasMore] = useState(true);
  const observerRef = useRef<HTMLDivElement | null>(null);

  // 유저 정보 불러오기
  useEffect(() => {
    const fetchUser = async () => {
      try {
        const res = await axiosInstance.get("/api/v1/user/profile");

        const { userId, nickname, imageUrl } = res.data.data;

        dispatch(
          setUser({
            userId,
            nickname,
            imageUrl,
          })
        );

        // console.log("유저 정보 불러오기 성공", res.data.data);
      } catch (err) {
        console.error("유저 정보 불러오기 실패:", err);
      }
    };

    fetchUser();
  }, [dispatch]);

  // 알림 설정 여부 불러오기
  useEffect(() => {
    {
      (async () => {
        try {
          const res = await axiosInstance.get("/api/v1/user/reminder-setting");

          setReminderSetting(res.data.data.enableAlarms);
          // console.log("확인하기", res.data.data);
          if (res.data.data.enableAlarms === null) {
            setShowModal(true);
          }
        } catch (err) {
          console.error("알림 설정 여부 불러오기 오류: ", err);
        }
      })();
    }
  }, []);

  // 오늘의 질문 불러오기
  useEffect(() => {
    {
      (async () => {
        try {
          const res = await axiosInstance.get("/api/v1/today-question");

          setTodayQuestion(res.data.data.question);
          // console.log("오늘의 질문 불러오기", res.data.data.question);
        } catch (err) {
          console.error("오늘의 질문 불러오기 오류: ", err);
        }
      })();
    }
  }, []);

  // 주간 감정 불러오기
  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get("/api/emotions/weekly");
        setWeekEmotions(res.data.data.emotions);
      } catch (err) {
        console.error("주간 감정 불러오기 오류: ", err);
      }
    })();
  }, []);

  // 월별 감정 통계 불러오기
  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get("/api/emotions/monthly");
        setMonthEmotions(res.data.data.emotions);
        // console.log("월별 감정 결과: ", res.data.data.emotions);
      } catch (err) {
        console.error("월별 감정 불러오기 오류: ", err);
      }
    })();
  }, []);

  // 사용자의 일기 목록 불러오기
  useEffect(() => {
    const checkHasWrittenDiary = async () => {
      try {
        const res = await axiosInstance.get("/api/diaries/me/all");
        const diaries = res.data.data.diaries;
        // console.log("일기 유무 확인: ", res.data.data);
        setHasWrittenDiary(diaries.length > 0);
      } catch (err) {
        console.error("일기 유무 확인 실패", err);
      }
    };

    checkHasWrittenDiary();
  }, []);

  // 최근 말자국 목록 불러오기
  const fetchRecentDiaries = async (cursor?: number) => {
    try {
      const res = await axiosInstance.get("/api/v1/group/diaries", {
        params: cursor ? { cursor } : {},
      });

      const newDiaries: Diary[] = res.data.data.diaries;
      setDiaries((prev) => {
        const existingIds = new Set(prev.map((d) => d.diaryId));
        const filtered = newDiaries.filter((d) => !existingIds.has(d.diaryId));
        return [...prev, ...filtered];
      });

      setNextCursor(res.data.data.nextCursor);
      setHasMore(res.data.data.nextCursor !== null);
    } catch (e) {
      console.error("최근 말자국 불러오기 실패", e);
    }
  };

  // 초기 목록 불러오기
  useEffect(() => {
    fetchRecentDiaries();
  }, []);

  // 추가 말자국 불러오기
  useEffect(() => {
    const currentRef = observerRef.current;
    if (!currentRef) return;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore) {
          fetchRecentDiaries(nextCursor!);
        }
      },
      { threshold: 1 }
    );

    observer.observe(currentRef);

    return () => {
      observer.disconnect();
    };
  }, [nextCursor, hasMore]);

  if (!user || !user.userId) return null;

  // 작성한 일기와 최근 말자국 모두 없는 사용자인 경우 소개 렌딩
  // if (hasWrittenDiary === false && diaries.length === 0) {
  //   return (
  //     <div className="p-4">
  //       {/* 유저 정보 */}
  //       <div className="flex items-center justify-between my-3 ">
  //         {/* 유저 정보 */}
  //         <div className="flex items-center gap-3">
  //           <img
  //             src={user.imageUrl}
  //             className="rounded-full w-14 h-14 object-cover"
  //             alt="프로필"
  //           />
  //           <div className="flex flex-col">
  //             <div className="flex items-baseline">
  //               <p className="text-xl font-semibold text-gray-700">
  //                 {user.nickname}
  //               </p>
  //               <p className="ml-1 text-gray-700">님</p>
  //             </div>
  //             <p className="text-gray-700">오늘 하루를 기록해 보세요!</p>
  //           </div>
  //         </div>

  //         {/* 알림 버튼 */}
  //         <button onClick={() => navigate("/notification")}>
  //           <img src={notificationIcon} alt="알림" className="w-6 h-6" />
  //         </button>
  //       </div>

  //       {/* 말자국 소개 */}
  //       <div className="relative w-full h-32 max-w-md mx-auto bg-yellow-50 rounded-2xl px-6 py-4 overflow-hidden my-2">
  //         <img
  //           src={QuestionCharacter}
  //           alt="말자국 캐릭터"
  //           className="absolute right-4 top-3 w-16 h-auto"
  //         />
  //         <div className="flex flex-col h-full justify-center">
  //           <p className="text-yellow-400 font-semibold text-xl mb-1">말자국</p>
  //           <p className="text-gray-500 font-medium leading-relaxed mt-1">
  //             말 한마디로 완성되는 일기,
  //             <br />
  //             대화를 통해 감정과 일기를 기록해 보세요
  //           </p>
  //         </div>
  //       </div>

  //       {/* 말자국 남기기 */}
  //       <div className="mb-5">
  //         <p className=" text-yellow-400 font-semibold mb-1">이번 주 기록</p>
  //         <p className=" text-gray-500 font-medium mb-2">
  //           대화를 통해 기록하는 오늘 내 하루 일기
  //         </p>
  //         <div className="flex flex-col gap-2">
  //           <img src={chatAi} alt="AI" />
  //           <img src={chatUser} alt="USER" />
  //         </div>
  //       </div>

  //       {/* 실시간 알림 */}
  //       <div className="mb-5">
  //         <p className=" text-mint font-semibold mb-1 text-end">
  //           실시간 알림 서비스
  //         </p>
  //         <p className=" text-gray-500 font-medium mb-2 text-end">
  //           친구들의 활동을 알림 받고 소통할 수 있어요
  //         </p>
  //       </div>
  //     </div>
  //   );
  // }

  return (
    <div className="p-4">
      {/* 유저 정보 */}
      <div className="flex items-center justify-between my-3">
        {/* 유저 정보 */}
        <div className="flex items-center gap-3">
          <img
            src={user.imageUrl}
            className="rounded-full w-14 h-14 object-cover"
            alt="프로필"
          />
          <div className="flex flex-col">
            <div className="flex items-baseline">
              <p className="text-xl font-semibold text-gray-700">
                {user.nickname}
              </p>
              <p className="ml-1 text-gray-700">님</p>
            </div>
            <p className="text-gray-700">오늘 하루를 기록해 보세요!</p>
          </div>
        </div>

        {/* 알림 버튼 */}
        <button onClick={() => navigate("/notification")}>
          <img src={notificationIcon} alt="알림" className="w-6 h-6" />
        </button>
      </div>

      {/* 오늘의 질문 */}
      <div className="mb-3">
        <TodayQuestion question={todayQuestion} />
      </div>

      {/* 이번 주 기록 */}
      <p className=" text-yellow-400 font-semibold mb-2">이번 주 기록</p>
      <div className="mb-3">
        <WeekEmotion
          emotions={
            weekEmotions as (
              | "행복"
              | "설렘"
              | "피로"
              | "짜증"
              | "우울"
              | null
            )[]
          }
        />
      </div>

      {/* 이번 달 통계 */}
      <p className=" text-yellow-400 font-semibold mb-2">이번 달 내 마음</p>
      <div className="mb-3">
        <MonthEmotion emotions={monthEmotions} />
      </div>

      {/* 최근 말자국 모음 */}
      <div>
        <p className=" text-yellow-400 font-semibold mb-2">최근 말자국</p>
        {diaries.length === 0 ? (
          <div className="flex flex-col items-center justify-center min-h-[40vh]">
            <img src={robotCharacter} alt="캐릭터" className="h-32" />
            <p className="text-sm text-gray-400 mt-4 text-center">
              최근 공유된 일기가 없어요! <br /> 가정 먼저 일기를 공유해 보세요
            </p>
          </div>
        ) : (
          <>
            <div className="pb-20">
              {diaries.map((diary) => (
                <div key={diary.diaryId} className="mb-3">
                  <DiaryPreview {...diary} />
                </div>
              ))}
            </div>
          </>
        )}
      </div>

      {showModal && (
        <NotificationModal
          onUpdate={(value) => {
            setReminderSetting(value);
            setShowModal(false);
          }}
        />
      )}
    </div>
  );
}
