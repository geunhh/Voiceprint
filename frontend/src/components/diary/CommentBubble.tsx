import authorIcon from "../../assets/icons/diaryAuthor.png";
import useTimeAgo from "../../hooks/useTimeAgo";

interface Comment {
  userId: number;
  userName: string;
  userImage: string;
  content: string;
  createdAt: string;
}

interface CommentProps {
  comment: Comment;
  isAuthor: boolean;
}
export default function CommentBubble({ comment, isAuthor }: CommentProps) {
  const timeAgo = useTimeAgo(comment.createdAt);

  return (
    <div className="flex items-start gap-3 px-2">
      {/* 프로필 이미지 */}
      <img
        src={comment.userImage}
        className="w-10 h-10 rounded-full"
        alt={`프로필 이미지`}
      />

      <div className="flex flex-col gap-1">
        {/* 이름 + 시간 */}
        <div className="flex items-center gap-1 text-xs text-gray-500">
          <span className="font-semibold text-gray-700">
            {comment.userName}
          </span>
          {isAuthor && (
            // 일기 작성자의 경우 작성자 아이콘 함께 표시
            <img
              src={authorIcon}
              alt="작성자 아이콘"
              className="w-10 h-4 object-contain"
            />
          )}
          <span className="ml-1">{timeAgo}</span>
        </div>

        {/* 말풍선 */}
        <div
          className={`px-4 py-2 rounded-xl whitespace-pre-wrap text-sm break-words ${
            isAuthor ? "bg-yellow-100" : "bg-lightmint"
          }`}
        >
          {comment.content}
        </div>
      </div>
    </div>
  );
}
