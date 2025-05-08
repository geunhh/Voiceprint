import { useCopyToClipboard } from "@uidotdev/usehooks";
import toast from "react-hot-toast";
import closeIcon from "../../assets/icons/close.png";

interface GroupInviteModalProps {
  link: string;
  onClose: () => void;
}

function GroupInviteModal({ link, onClose }: GroupInviteModalProps) {
  const [copiedText, copy] = useCopyToClipboard();

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
        <p className="text-xl font-bold text-center mb-6 mt-6">그룹 초대하기</p>

        {/* 안내 */}
        <div className="text-gray-700 mb-7">
          <p>링크를 복사해서</p>
          <p>친구를 그룹에 초대해보세요!</p>
        </div>

        {/* 버튼 */}
        <div className="flex px-4 gap-2 justify-center items-center">
          <p className="truncate max-w-[70%] text-sm text-gray-500">{link}</p>
          <button
            className="bg-yellow-500 text-white h-10 w-16 text-sm rounded-xl"
            onClick={() => {
              copy(link);
              toast.success("초대 링크가 복사되었습니다.");
            }}
          >
            <p>복사</p>
          </button>
        </div>
      </div>
    </div>
  );
}

export default GroupInviteModal;
