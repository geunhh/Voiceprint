import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../api/axiosInstance";
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
  const navigate = useNavigate();

  useEffect(() => {
    const fetchNotifications = async () => {
      try {
        const res = await axiosInstance.get("/api/notifications");
        console.log("알림 목록 조회:", res.data);
        setNotifications(res.data.data.diaries);
      } catch (err) {
        console.error("알림 목록 조회 실패:", err);
      }
    };

    fetchNotifications();
  }, []);

  return (
    <div className="p-10 space-y-3 bg-yellow-50">
      <p className="text-xl font-bold text-center text-gray-700 font-muruk">
        알림 목록
      </p>
      {notifications.map((item) => (
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
        />
      ))}
    </div>
  );
}
