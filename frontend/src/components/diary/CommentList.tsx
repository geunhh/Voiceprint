import { useEffect, useRef } from "react";
import CommentBubble from "./CommentBubble";

interface Comment {
  commentId: number;
  userId: number;
  userName: string;
  userImage: string;
  content: string;
  createdAt: string;
}

interface CommentProps {
  comments: Comment[];
  authorId: number;
  onReachBottom: () => void;
  hasNext: boolean;
  currentUserId: number | null;
  onDeleteComment: (commentId: number) => void;
}

export default function CommentList({
  comments,
  authorId,
  onReachBottom,
  hasNext,
  currentUserId,
  onDeleteComment,
}: CommentProps) {
  const observerRef = useRef<IntersectionObserver | null>(null);

  const lastCommentRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    if (!hasNext || !lastCommentRef.current) return;

    if (observerRef.current) observerRef.current.disconnect();

    observerRef.current = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          onReachBottom();
        }
      },
      {
        rootMargin: "100px",
        threshold: 0.1,
      }
    );

    observerRef.current.observe(lastCommentRef.current);

    return () => observerRef.current?.disconnect();
  }, [comments, hasNext]);
  return (
    <div
      className="
        comment-list
        w-full mx-auto rounded-xl
         text-gray-700 text-sm overflow-y-auto
        space-y-4 pb-24
      "
    >
      {comments.map((c, i) => {
        const isLast = i === comments.length - 1;
        return (
          <div key={c.commentId} ref={isLast ? lastCommentRef : null}>
            <CommentBubble
              comment={c}
              isAuthor={c.userId === authorId}
              currentUserId={currentUserId}
              onDelete={onDeleteComment}
            />
          </div>
        );
      })}
    </div>
  );
}
