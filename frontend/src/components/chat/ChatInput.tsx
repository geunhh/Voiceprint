import Button from "../common/Button";

interface ChatInputProps {
  value: string;
  onChange: (v: string) => void;
  onSend: () => void;
  loading: boolean;
}

export default function ChatInput({
  value,
  onChange,
  onSend,
  loading,
}: ChatInputProps) {
  return (
    <div className="w-full max-w-[320px] flex items-center gap-2 my-4">
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder="대화를 입력해주세요"
        className="flex-1 border max-w-[230px] border-gray-300 rounded-xl px-4 py-2 focus:outline-none"
        onKeyDown={(e) => e.key === "Enter" && onSend()}
      />
      <Button
        text={loading ? "전송중..." : "전송"}
        type="fill"
        size="M"
        onClick={onSend}
      />
    </div>
  );
}
