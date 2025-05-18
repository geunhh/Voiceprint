import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../api/axiosInstance";
import robotCharacter from "../assets/icons/robotCharacter.png";
import NotificationFilterTabs from "../components/notification/notificationFilterTabs";
import NotificationItem from "../components/notification/notificationItem";

interface Notification {
  type: "reminder" | "newComment" | "newDiary";
  message: string;
  metadata: {
    groupId?: number;
    diaryId?: number;
    notificationId: number;
  };
}

export default function NotificationPage() {
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [filter, setFilter] = useState<"all" | "newDiary" | "newComment">(
    "all"
  );
  const navigate = useNavigate();

  // 알림 목록 조회
  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await axiosInstance.get("/api/notifications");
        console.log("알림 목록 조회:", res.data.data);
        setNotifications(res.data.data.diaries);
      } catch (err) {
        console.error("알림 목록 조회 실패:", err);
      }
    };

    fetchNotifications();
  }, []);

  const filtered = notifications.filter((item) => {
    if (filter === "all") return true;
    return item.type === filter;
  });

  return (
    <div className="p-4 space-y-3 h-full">
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
            onClick={() => {
              if (item.metadata.groupId && item.metadata.diaryId) {
                navigate(
                  `/group/${item.metadata.groupId}/diary/${item.metadata.diaryId}`
                );
              } else {
                navigate("diary/setting/friend");
              }
            }}
            variant="list"
          />
        ))
      )}
    </div>
  );
}
