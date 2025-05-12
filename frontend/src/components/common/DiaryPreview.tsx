import { useNavigate } from "react-router";
import useTimeAgo from "../../hooks/useTimeAgo";

interface DiaryPreviewProps {
  userImage: string;
  userName: string;
  groupName: string;
  createdAt: string;
  content: string;
  diaryId: number;
}

function DiaryPreview(props: DiaryPreviewProps) {
  const { userImage, userName, groupName, createdAt, content, diaryId } = props;
  const timeAgo = useTimeAgo(createdAt);
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/diary/${diaryId}`);
  };

  return (
    <div
      className="flex flex-col mx-auto rounded-xl border border-yellow-400 shadow-sm shadow-yellow-400/30 p-6 w-full hover:bg-yellow-50 cursor-pointer"
      onClick={handleClick}
    >
      {/* 유저 정보 및 그룹 이름, 일자 */}
      <div className="flex items-start gap-3">
        <img
          src={userImage}
          alt="User"
          className="h-12 w-12 rounded-full object-cover"
        />
        <div className="flex flex-col flex-1">
          <div className="flex justify-between items-center">
            <p className="text-base font-semibold">{userName}</p>
            <span className="text-sm text-gray-400 whitespace-nowrap">
              {timeAgo}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-0.5">[{groupName}]</p>
        </div>
      </div>

      {/* 일기 내용 */}
      <p className="mt-3 text-sm text-gray-700 line-clamp-3 text-justify">
        {content}
      </p>
    </div>
  );
}

export default DiaryPreview;
