import happyIcon from "../../assets/icons/happyCharacter.png";
import questionIcon from "../../assets/icons/questionCharacter.png";
import Button from "../common/Button";

interface AlertModalProps {
  message: string;
  type: "success" | "fail";
  onClose: () => void;
  callback?: () => void; // 선택: 확인 시 실행할 추가 동작
}

export default function AlertModal({
  message,
  type,
  onClose,
  callback,
}: AlertModalProps) {
  const icon = type === "success" ? happyIcon : questionIcon;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="relative bg-white rounded-2xl p-6 w-4/5 max-w-[320px]">
        {/* 닫기 버튼 */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 text-gray-400 hover:text-gray-600"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="w-6 h-6"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M6 18L18 6M6 6l12 12"
            />
          </svg>
        </button>

        {/* 이미지 + 메시지 */}
        <div className="flex flex-col items-center justify-center space-y-4 text-center my-4">
          <img src={icon} alt="icon" className="w-25 h-32" />
          <h2 className="text-lg font-semibold text-gray-800 whitespace-pre-line">
            {message}
          </h2>
          <div className="w-full">
            <Button
              text="확인"
              type="fill"
              size="L"
              onClick={() => {
                onClose();
                callback?.();
              }}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
