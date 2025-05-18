import { useNavigate } from "react-router";
import happyCharacter from "../../assets/icons/happyCharacter.png";
import lovelyCharacter from "../../assets/icons/lovelyCharacter.png";
import questionCharacter from "../../assets/icons/questionCharacter.png";

interface NotificationItemProps {
  type: "reminder" | "newComment" | "newDiary";
  groupId?: number;
  diaryId?: number;
  message: string;
  onClick: () => void;
  variant?: "default" | "list";
}

const notificationImageMap: Record<NotificationItemProps["type"], string> = {
  reminder: lovelyCharacter,
  newComment: happyCharacter,
  newDiary: questionCharacter,
};

const notificationTitleMap: Record<NotificationItemProps["type"], string> = {
  reminder: "오늘의 말자국",
  newComment: "새로운 댓글",
  newDiary: "새로운 일기",
};

function NotificationItem({
  type,
  message,
  groupId,
  diaryId,
  onClick,
  variant = "default",
}: NotificationItemProps) {
  const navigate = useNavigate();

  const image = notificationImageMap[type];
  const title = notificationTitleMap[type];

  const handleClick = () => {
    if (onClick) return onClick();
    if (groupId && diaryId) {
      // 그룹 댓글 및 그룹 일기 알림의 경우 해당 일기 상세 페이지로 이동
      navigate(`/group/${groupId}/diary/${diaryId}`);
    } else {
      // 일기 작성 reminder 알림의 경우 일기 작성 페이지로 이동
      navigate("diary/setting/friend");
    }
  };

  const baseStyle =
    "w-full flex items-center gap-4 p-4 text-left transition-all";

  const variantStyle =
    variant === "list"
      ? "bg-white border-b border-lightmint hover:bg-lightmint"
      : "rounded-xl bg-lightmint hover:bg-mint";

  return (
    <button className={`${baseStyle} ${variantStyle}`} onClick={handleClick}>
      <img src={image} alt="알림 캐릭터" className="h-14" />
      <div>
        <p className="font-semibold text-gray-800 text-sm">{title}</p>
        <p className="text-gray-700 text-sm">{message}</p>
      </div>
    </button>
  );
}

export default NotificationItem;
