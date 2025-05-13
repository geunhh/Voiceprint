import { useEffect, useRef } from "react";
import CommentBubble from "./CommentBubble";

interface Comment {
  userId: number;
  userName: string;
  userImage: string;
  content: string;
  createdAt: string;
}

export default function CommentList({
  comments,
  authorId,
}: {
  comments: Comment[];
  authorId: number;
}) {
  const endRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    endRef.current?.scrollIntoView();
  }, [comments]);

  return (
    <div
      className="
        comment-list
        w-11/12 mx-auto rounded-xl border border-yellow-400 bg-white 
        px-5 py-4 text-gray-700 text-sm overflow-y-auto
        space-y-4
        max-h-[450px] min-h-[300px]
      "
    >
      {comments.map((c, i) => (
        <CommentBubble
          key={i}
          from={c.userId === authorId ? "author" : "others"}
          text={c.content}
          userImage={c.userImage}
          userName={c.userName}
          createdAt={c.createdAt}
        />
      ))}
      {/* 마지막 요소에 ref 연결 */}
      <div ref={endRef} />
    </div>
  );
}
