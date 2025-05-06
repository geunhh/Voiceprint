import { useState } from "react";
import sendIcon from "../../assets/icons/send.png";

interface CommentInputProps {
  user: {
    userId: number;
    userName: string;
    userImage: string;
  };
  onSubmit: (comment: string) => void;
}

function CommentInput({ user, onSubmit }: CommentInputProps) {
  const [input, setInput] = useState("");

  const handleSubmit = () => {
    const trimmed = input.trim();
    if (!trimmed) return;
    onSubmit(trimmed);
    setInput(""); // 상태 초기화
  };

  return (
    <div className="flex items-center gap-2 p-2 mt-4">
      <input
        type="text"
        value={input}
        onChange={(e) => setInput(e.target.value)}
        onKeyUp={(e) => {
          if (e.key === "Enter") {
            e.preventDefault();
            handleSubmit();
          }
        }}
        placeholder="댓글을 입력해주세요"
        className="flex-1 rounded-xl border border-gray-300 px-4 py-2 text-sm text-gray-700 h-11
             focus:outline-none focus:ring-1 focus:ring-yellow-400 focus:border-yellow-400"
      />

      <button
        type="button"
        onClick={handleSubmit}
        className="rounded-lg bg-yellow-400 p-2 w-11 h-11"
      >
        <img src={sendIcon} alt="댓글 작성" />
      </button>
    </div>
  );
}

export default CommentInput;
