import { useNavigate } from "react-router";
import useTimeAgo from "../../hooks/useTimeAgo";

interface GroupDiaryPreviewProps {
  userImage: string;
  userName: string;
  title: string;
  createdAt: string;
  content: string;
  diaryId: number;
  groupId: number;
}

function GroupDiaryPreview(props: GroupDiaryPreviewProps) {
  const { userImage, userName, title, createdAt, content, diaryId, groupId } =
    props;
  const timeAgo = useTimeAgo(createdAt);
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/group/${groupId}/diary/${diaryId}`);
  };

  return (
    <div
      className="flex mx-auto rounded-xl border border-lightmint shadow-sm shadow-mint/50 p-4 w-full hover:bg-lightmint cursor-pointer"
      onClick={handleClick}
    >
      <div className="flex items-start gap-3">
        <img
          src={userImage}
          alt="User"
          className="h-10 w-10 rounded-full object-cover"
        />
        <div className="flex-1">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-base font-semibold">{userName}</p>
              <p className="text-sm text-gray-500">[{title}]</p>
            </div>
            <span className="text-sm text-gray-400 whitespace-nowrap">
              {timeAgo}
            </span>
          </div>
          <p className="mt-2 text-sm text-gray-700 line-clamp-3">{content}</p>
        </div>
      </div>
    </div>
  );
}

export default GroupDiaryPreview;
