import { useNavigate } from "react-router";
import angryCharacter from "../../assets/icons/angryCharacter.png";
import happyCharacter from "../../assets/icons/happyCharacter.png";
import lovelyCharacter from "../../assets/icons/lovelyCharacter.png";
import questionCharacter from "../../assets/icons/questionCharacter.png";
import robotCharacter from "../../assets/icons/robotCharacter.png";

interface NotificationItemProps {
  type: "reminder" | "newComment" | "type1" | "type2" | "type3"; // 타입 추가 예정
  message: string;
  groupId?: number;
  diaryId?: number;
  authorName?: string;
}

const notificationImageMap: Record<NotificationItemProps["type"], string> = {
  reminder: lovelyCharacter,
  newComment: happyCharacter,
  type1: questionCharacter,
  type2: robotCharacter,
  type3: angryCharacter,
};

const notificationTitleMap: Record<NotificationItemProps["type"], string> = {
  reminder: "잊지 마세용 알림",
  newComment: "댓글 작성 알림",
  type1: "알림 제목 1",
  type2: "알림 제목 2",
  type3: "알림 제목 3",
};

function NotificationItem(props: NotificationItemProps) {
  const navigate = useNavigate();

  const { type, message, groupId, diaryId } = props;

  const image = notificationImageMap[type];
  const title = notificationTitleMap[type];

  const handleClick = () => {
    if (type === "newComment" && groupId && diaryId) {
      navigate(`/group/${groupId}/diary/${diaryId}`);
    }
  };

  return (
    <button
      className="bg-lightmint/50 flex items-center gap-4 p-4 rounded-xl w-full hover:bg-mint text-left"
      onClick={handleClick}
    >
      <img src={image} alt="알림 캐릭터" className="h-14" />
      <div>
        <p className="font-semibold text-gray-800 text-sm">{title}</p>
        <p className="text-gray-700 text-sm">{message}</p>
      </div>
    </button>
  );
}

export default NotificationItem;
