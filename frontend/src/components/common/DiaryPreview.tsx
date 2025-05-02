import { useNavigate } from "react-router";
import useTimeAgo from "../../hooks/useTimeAgo";

interface DiaryPreviewProps {
    userImage: string;
    userName: string;
    groupName: string;
    createdAt: string;
    content: string;
    diaryId: number
}

function DiaryPreview (props : DiaryPreviewProps) {
    const { userImage, userName, groupName, createdAt, content,diaryId } = props;
    const timeAgo = useTimeAgo(createdAt)
    const navigate = useNavigate()

    const handleClick = () => {
        navigate(`/diary/${diaryId}`);
      };

    return (
        <div className="flex mx-auto rounded-xl border border-yellow-400 bg-white p-4 w-11/12" onClick={handleClick}
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
                  <p className="text-sm text-gray-500">[{groupName}]</p>
                </div>
                <span className="text-sm text-gray-400 whitespace-nowrap">
                  {timeAgo}
                </span>
              </div>
              <p className="mt-2 text-sm text-gray-700 line-clamp-2">{content}</p>
            </div>
          </div>
        </div>
      );
}

export default DiaryPreview