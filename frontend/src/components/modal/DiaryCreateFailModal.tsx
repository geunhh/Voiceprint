// src/components/modal/DiaryCreateFailModal.tsx
import warningIcon from "../../assets/icons/modal/warning.png";
import Button from "../common/Button";

interface DiaryCreateFailModalProps {
  onClose: () => void;
  onRetry: () => void;
}

export default function DiaryCreateFailModal({
  onClose,
  onRetry,
}: DiaryCreateFailModalProps) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="relative bg-white rounded-2xl p-6 w-4/5 max-w-[320px]">
        <div className="flex flex-col items-center justify-center space-y-4 text-center my-4">
          <img src={warningIcon} alt="Warning" className="w-14 h-12" />
          <h2 className="text-center text-lg font-semibold text-gray-800">
            일기 생성에 실패했습니다.
          </h2>

          <div className="w-full">
            <Button
              text="다시 시도하기"
              type="fill"
              size="L"
              onClick={onRetry}
            />
          </div>
          <div className="w-full">
            <Button text="확인" type="line" size="L" onClick={onClose} />
          </div>
        </div>
      </div>
    </div>
  );
}
