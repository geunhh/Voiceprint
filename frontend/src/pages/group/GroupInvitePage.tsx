import { useNavigate } from "react-router";
import HappyCharacter from "../../assets/icons/happyCharacter.png";
import Button from "../../components/common/Button";

// 임시 데이터
const inviteInfo = {
  groupId: 1,
  groupName: "아이스크림 조아 모임",
  groupImage:
    "https://i.pinimg.com/736x/a7/ca/36/a7ca369a79ff17fb0ae1c13e72a7a8b4.jpg",
  userName: "김혜민",
};

export default function GroupInvitePage() {
  const navigate = useNavigate();
  return (
    <div className="flex items-center justify-center h-dvh bg-lightmint">
      <div className="bg-white w-4/5 h-2/5 max-h-64 rounded-xl border-2 border-mint p-2 flex flex-col justify-between">
        {/* 제목 */}
        <p className="text-xl font-bold text-center mt-6">그룹 참여하기</p>

        {/* 안내 */}
        <div className="flex items-center justify-center gap-2">
          <div className="text-center">
            <div className="flex items-center  justify-center gap-1">
              <p className="text-gray-600 font-semibold text-lg">
                {inviteInfo.userName}
              </p>
              <p className="text-gray-600">님이 초대한</p>
            </div>
            <p className="font-semibold text-gray-700 text-xl">
              {inviteInfo.groupName}
            </p>
          </div>
          <img src={HappyCharacter} className="h-24" />
        </div>

        {/* 버튼 */}
        <div className="flex justify-center mb-6 mt-3">
          <Button
            type="fill"
            size="L"
            text="초대 수락하기"
            color="mint"
            onClick={() => {
              navigate(`/group/${inviteInfo.groupId}`);
            }}
          />
        </div>
      </div>
    </div>
  );
}
