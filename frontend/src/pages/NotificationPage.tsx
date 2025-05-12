import NotificationItem from "../components/notification/notificationItem";

const notifications = [
  {
    type: "reminder",
    message: "[알림 1] 표시할 메시지",
  },
  {
    type: "commentCreated",
    message: "[알림 2] 표시할 메시지",
    groupId: 1,
    diaryId: 1,
  },
  {
    type: "type1",
    message: "[알림 3] 표시할 메시지",
  },
  {
    type: "type2",
    message: "[알림 4] 표시할 메시지",
  },
  {
    type: "type3",
    message: "[알림 5] 표시할 메시지",
  },
];

export default function NotificationPage() {
  return (
    <div className="p-4 space-y-3">
      <p className="text-xl font-bold text-center text-gray-700">알림 목록</p>
      {notifications.map((item, idx) => (
        <NotificationItem key={idx} {...item} />
      ))}
    </div>
  );
}
