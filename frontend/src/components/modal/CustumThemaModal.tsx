import closeIcon from "../../assets/icons/close.png";
import Button from "../common/Button";

interface CustomThemaModalProps {
  onClose: () => void;
}

function CustomThemaModal({ onClose }: CustomThemaModalProps) {
  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center text-center">
      <div className="w-4/5 max-w-[320px] rounded-xl bg-white flex flex-col py-6 h-64 relative overflow-y-auto">
        {/* 닫기 버튼 */}
        <img
          src={closeIcon}
          alt="닫기버튼"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />

        {/* 제목 */}
        <p className="text-xl font-bold text-center mb-6 mt-6">
          커스텀 테마 생성
        </p>

        {/* 안내 */}
        <div className="text-gray-700 mb-7">
          <p>해당 일기를 활용하여</p>
          <p>커스텀 테마를 만드시겠습니까?</p>
        </div>

        {/* 버튼 */}
        <div className="flex px-8 justify-between">
          <Button text="취소" type="line" size="M" onClick={onClose} />
          <Button text="생성" type="fill" size="M" onClick={() => {}} />
        </div>
      </div>
    </div>
  );
}

export default CustomThemaModal;
