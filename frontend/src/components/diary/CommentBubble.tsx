import authorIcon from "../../assets/icons/diaryAuthor.png";
import deleteIcon from "../../assets/icons/diaryCommentDelete.png";
import useTimeAgo from "../../hooks/useTimeAgo";

interface Comment {
  userId: number;
  userName: string;
  userImage: string;
  content: string;
  createdAt: string;
  commentId: number;
}

interface CommentProps {
  comment: Comment;
  isAuthor: boolean;
  currentUserId: number | null;
  onDelete?: (commentId: number) => void;
}
export default function CommentBubble({
  comment,
  isAuthor,
  currentUserId,
  onDelete,
}: CommentProps) {
  const timeAgo = useTimeAgo(comment.createdAt);

  const isMyComment = comment.userId === currentUserId;

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
          <span className="ml-1">{timeAgo}</span>
          {isAuthor ? (
            <img
              src={authorIcon}
              alt="작성자 아이콘"
              className="w-10 h-4 object-contain"
            />
          ) : isMyComment ? (
            <img
              src={deleteIcon}
              alt="삭제 아이콘"
              className="w-10 h-4 object-contain cursor-pointer"
              onClick={() => onDelete?.(comment.commentId)}
            />
          ) : null}
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
