import spinnerIcon from "../../assets/icons/modal/spinner.gif";
import Button from "../common/Button";

interface DiaryCreatingModalProps {
  showConfirm: boolean;
  onConfirm: () => void;
}

export default function DiaryCreatingModal({
  showConfirm,
  onConfirm,
}: DiaryCreatingModalProps) {
  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-8 w-4/5 max-w-[320px] flex flex-col items-center justify-center space-y-6 text-center">
        <img
          src={spinnerIcon}
          alt="로딩 중"
          className="w-20 h-20 animate-spin"
        />
        <h2 className="text-lg font-semibold text-gray-800">
          일기를 생성 중입니다...
        </h2>
        <p className="text-sm text-gray-500">잠시만 기다려주세요!</p>

        {/* ✅ 2초 후 표시되는 버튼 */}
        {showConfirm && (
          <div className="w-full">
            <Button text="확인" type="fill" size="L" onClick={onConfirm} />
          </div>
        )}
      </div>
    </div>
  );
}
