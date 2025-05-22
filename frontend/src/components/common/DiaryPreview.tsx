import { useNavigate } from "react-router";
import useTimeAgo from "../../hooks/useTimeAgo";

interface DiaryPreviewProps {
  groupId: number;
  diaryId: number;
  title: string;
  content: string;
  createdAt: string;
  profileUrl: string;
  nickname: string;
}

function DiaryPreview(props: DiaryPreviewProps) {
  const { groupId, diaryId, title, content, createdAt, profileUrl, nickname } =
    props;
  const timeAgo = useTimeAgo(createdAt);
  const navigate = useNavigate();

  const handleClick = () => {
    navigate(`/group/${groupId}/diary/${diaryId}`);
  };

  return (
    <div
      className="flex flex-col mx-auto rounded-xl border border-yellow-400 shadow-sm shadow-yellow-400/30 p-6 w-full hover:bg-yellow-50 cursor-pointer"
      onClick={handleClick}
    >
      {/* 유저 정보 및 그룹 이름, 일자 */}
      <div className="flex items-start gap-3">
        <img
          src={profileUrl}
          alt="User"
          className="h-12 w-12 rounded-full object-cover"
        />
        <div className="flex flex-col flex-1">
          <div className="flex justify-between items-center">
            <p className="text-base font-semibold">{nickname}</p>
            <span className="text-sm text-gray-400 whitespace-nowrap">
              {timeAgo}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-0.5">[{title}]</p>
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
