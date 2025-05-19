import { useState } from "react";

interface CommentInputProps {
  onSubmit: (comment: string) => void;
}

function CommentInput({ onSubmit }: CommentInputProps) {
  const [input, setInput] = useState("");

  const handleSubmit = () => {
    const trimmed = input.trim();
    if (!trimmed) return;
    onSubmit(trimmed);
    setInput(""); // 상태 초기화
  };

  return (
    <div className="flex items-center justify-between border border-gray-200 rounded-xl px-2 h-14 mt-2">
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
        className="flex-1 text-sm text-gray-700 placeholder:text-gray-400 px-2 focus:outline-none"
      />

      <button
        type="button"
        onClick={handleSubmit}
        className="bg-yellow-400 text-white text-sm font-semibold px-4 py-1 rounded-xl h-10"
      >
        작성
      </button>
    </div>
  );
}

export default CommentInput;
