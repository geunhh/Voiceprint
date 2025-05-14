// src/components/common/ChatExistModal.tsx
import warningIcon from "../../assets/icons/modal/warning.png";
import Button from "../common/Button";

interface ChatExistModalProps {
  onClose: () => void;
  onContinue: () => void;
  onRestart: () => void;
}

export default function ChatExistModal({
  onClose,
  onContinue,
  onRestart,
}: ChatExistModalProps) {
  return (
    // 전체 화면을 덮는 반투명 오버레이
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      {/* 흰색 모달 박스 */}
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

        {/* 내용 영역: 모두 가운데 정렬 */}
        <div className="flex flex-col items-center justify-center space-y-4 text-center my-4">
          {/* warning.png 아이콘 */}
          <img src={warningIcon} alt="Warning" className="w-14 h-12" />

          {/* 메시지 */}
          <h2 className="text-center text-lg font-semibold text-gray-800">
            진행 중인 채팅이 존재합니다.
          </h2>

          {/* 이어하기 (outlined) */}
          <div className="w-full">
            <Button text="이어하기" type="fill" size="L" onClick={onContinue} />
          </div>

          {/* 새로 시작하기 (filled) */}
          <div className="w-full">
            <Button
              text="새로 시작하기"
              type="line"
              size="L"
              onClick={onRestart}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
