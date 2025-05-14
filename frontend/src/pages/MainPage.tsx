import { useEffect, useState } from "react";
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

// 오늘의 질문
const todayQuestion = [
  "최근 나를 가장 몰입하게 만든 소소한 취미나 관심사가 있나요?",
];

// 월별 감정 통계
const monthEmotions = [
  { emotion: "행복", count: 15 },
  { emotion: "설렘", count: 7 },
  { emotion: "피로", count: 6 },
  { emotion: "짜증", count: 4 },
  { emotion: "우울", count: 3 },
];

// 최근 말자국 모음
const diaries = [
  {
    diaryId: 98,
    title: "카공데이",
    createdAt: "2025-04-30T12:22:30",
    userId: 1,
    userName: "민태홍",
    userImage:
      "https://i.pinimg.com/736x/a4/a3/06/a4a3060f6e0e9a90170a70cc9f84122c.jpg",
    groupName: "개발자 모임",
    content:
      "둔산동 할리스에서 공부를 하고고 매운 음식이 먹고 싶어서 마라탕을 먹었다. 유부를 5번 추가하고 꿔바로우랑 같이 맛있게 먹었다! ",
  },
  {
    diaryId: 99,
    title: "무서운게 딱좋아",
    createdAt: "2025-04-29T14:22:30",
    userId: 2,
    userName: "조기흠",
    userImage:
      "https://i.pinimg.com/736x/7b/ba/50/7bba500beb814b376a83fd0cf5015cc7.jpg",
    groupName: "개발자 모임",
    content:
      "학봉초 가서 무서운 게 딱 좋아 체험을 했다. 무서운 소리가 나서 도망가고 싶었다. 바들바들. 무서운게 딱 좋아는 너무 재미있어요.",
  },
  {
    diaryId: 100,
    title: "대청호 드라이브",
    createdAt: "2025-04-28T13:10:30",
    userId: 3,
    userName: "김근휘",
    userImage:
      "https://i.pinimg.com/736x/22/6e/ab/226eab96db865ae998703008c2b36d7b.jpg",
    groupName: "개발자 모임",
    content:
      "날씨 좋은 날 대청호에 가서 신나게 놀았다. 드라이브도 하고 너무 재미있었따.",
  },
  {
    diaryId: 101,
    title: "오늘 회의 기록",
    createdAt: "2025-04-28T20:22:30",
    userId: 4,
    userName: "김혜민",
    userImage:
      "https://i.pinimg.com/736x/70/44/41/704441fb50f5629ad2d5d2ba3a42b873.jpg",
    groupName: "아이스크림 좋아 모임",
    content:
      "오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다. ",
  },
  {
    diaryId: 102,
    title: "갑천 산책 일기",
    createdAt: "2025-04-27T14:22:30",
    userId: 5,
    userName: "정다인",
    userImage:
      "https://i.pinimg.com/736x/d7/fb/c4/d7fbc4317b5684298c6f767cd0154c37.jpg",
    groupName: "아이스크림 좋아 모임",
    content:
      "오늘은 저녁을 먹고 오랜만에 갑천 산책을 다녀왔다. 너무 재미있어요~~. 날씨가 선선해서 진짜 기분 좋게 산책도 하고 수달도 봐서 신기한 하루였다.",
  },
  {
    diaryId: 103,
    title: "오늘 회의 기록",
    createdAt: "2025-04-26T14:22:30",
    userId: 6,
    userName: "이지은",
    userImage:
      "https://i.pinimg.com/736x/bd/84/6e/bd846edb7c28812b706a01b680d1c2ef.jpg",
    groupName: "아이스크림 좋아 모임",
    content: "드디어 기획 회의가 끝났어요. 야~~~호! 너무너무 신난다.",
  },
];

export default function MainPage() {
  const dispatch = useDispatch();
  const user = useSelector((state: RootState) => state.user); // Redux에서 유저 정보 가져오기
  const [weekEmotions, setWeekEmotions] = useState<any>([]);

  const navigate = useNavigate();

  const [showModal, setShowModal] = useState(false); // 보이지 않도록 설정(임시) -> 유저 정보를 받아 처리 예정

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

        console.log("유저 정보 불러오기 성공", res.data.data);
      } catch (err) {
        console.error("유저 정보 불러오기 실패:", err);
      }
    };

    fetchUser();
  }, [dispatch]);

  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get("/api/emotions/weekly");
        setWeekEmotions(res.data.data.emotions);
        console.log("주간 감정 결과: ", res.data.data.emotions);
      } catch (err) {
        console.error("주간 감정 불러오기 오류: ", err);
      }
    })();
  }, []);

  if (!user || !user.userId) return null;

  return (
    <div className="p-4">
      {/* 유저 정보 */}
      <div className="flex items-center justify-between my-3 p-2">
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
      <p className=" text-yellow-400 font-semibold mb-2">최근 말자국</p>
      <div className="pb-20">
        {diaries.map((diary) => (
          <div className="mb-3">
            <DiaryPreview {...diary} />
          </div>
        ))}
      </div>

      {showModal && <NotificationModal onClose={() => setShowModal(false)} />}
    </div>
  );
}
