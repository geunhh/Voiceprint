import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../api/axiosInstance";
import robotCharacter from "../assets/icons/robotCharacter.png";
import NotificationFilterTabs from "../components/notification/notificationFilterTabs";
import NotificationItem from "../components/notification/notificationItem";

interface Notification {
  type: "reminder" | "newComment" | "newDiary" | "newMember";
  message: string;
  metadata: {
    groupId?: number;
    diaryId?: number;
    notificationId: number;
  };
}

interface NotificationResponse {
  diaries: Notification[];
  nextCursor: number | null;
}

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [nextCursor, setNextCursor] = useState<number | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [filter, setFilter] = useState<
    "all" | "newDiary" | "newComment" | "newMember"
  >("all");
  const navigate = useNavigate();

  // 초기 알림 목록 불러오기
  useEffect(() => {
    const fetchInitialNotifications = async () => {
      try {
        const res = await axiosInstance.get<{
          data: NotificationResponse;
        }>("/api/notifications");

        setNotifications(res.data.data.diaries);
        setNextCursor(res.data.data.nextCursor);
        console.log("초기 알림 목록 조회: ", res.data.data);
      } catch (err) {
        console.error("알림 목록 조회 실패:", err);
      }
    };

    fetchInitialNotifications();
  }, []);

  // 추가 알림 목록 불러오기
  const fetchMoreNotifications = async () => {
    if (!nextCursor || isLoading) return;

    try {
      setIsLoading(true);
      const res = await axiosInstance.get<{
        data: NotificationResponse;
      }>(`/api/notifications?cursor=${nextCursor}`);

      setNotifications((prev) => [...prev, ...res.data.data.diaries]);
      setNextCursor(res.data.data.nextCursor);
      // console.log("추가 알림 목록 조회: ", res.data.data);
    } catch (err) {
      console.error("추가 알림 불러오기 실패:", err);
    } finally {
      setIsLoading(false);
    }
  };

  // 스크롤 감지
  useEffect(() => {
    const handleScroll = () => {
      const scrollTop = window.scrollY;
      const windowHeight = window.innerHeight;
      const documentHeight = document.documentElement.scrollHeight;

      if (scrollTop + windowHeight >= documentHeight - 100) {
        fetchMoreNotifications();
      }
    };

    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [nextCursor, isLoading]);

  // 알림 읽음 처리
  const handleNotificationClick = async (item: Notification) => {
    try {
      // 읽음 처리 요청
      const res = await axiosInstance.patch(
        `/api/notifications/${item.metadata.notificationId}/read`
      );
      console.log("알림 읽음 처리: ", res.data);
    } catch (err) {
      console.error("알림 읽음 처리 실패:", err);
    }

    if (item.metadata.groupId && item.metadata.diaryId) {
      navigate(
        `/group/${item.metadata.groupId}/diary/${item.metadata.diaryId}`
      );
    } else if (item.type === "newMember") {
      navigate(`/group/${item.metadata.groupId}`);
    } else {
      navigate("diary/setting/friend");
    }
  };

  const filtered = notifications.filter((item) => {
    if (filter === "all") return true;
    return item.type === filter;
  });

  return (
    <div className="p-4 space-y-3 h-full pb-28">
      <p className="text-xl mt-5 font-bold text-center text-gray-700">
        알림 목록
      </p>

      <NotificationFilterTabs selected={filter} onSelect={setFilter} />

      {filtered.length === 0 ? (
        <div className="flex flex-col items-center justify-center min-h-[60vh]">
          <img src={robotCharacter} alt="알림 없음 캐릭터" className="h-32" />
          <p className="text-sm text-gray-400 mt-4">읽지 않은 알림이 없어요</p>
        </div>
      ) : (
        filtered.map((item) => (
          <NotificationItem
            key={item.metadata.notificationId}
            type={item.type}
            message={item.message}
            groupId={item.metadata.groupId}
            diaryId={item.metadata.diaryId}
            onClick={() => handleNotificationClick(item)}
            variant="list"
          />
        ))
      )}
    </div>
  );
}
