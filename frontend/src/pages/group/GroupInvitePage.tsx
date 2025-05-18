import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import axiosInstance from "../../api/axiosInstance";
import HappyCharacter from "../../assets/icons/happyCharacter.png";
import Button from "../../components/common/Button";

interface InviteInfo {
  groupName: string;
  groupImage: string;
  inviterName: string;
  alreadyJoined: boolean;
}

export default function GroupInvitePage() {
  const { inviteId } = useParams();
  const navigate = useNavigate();
  const [inviteInfo, setInviteInfo] = useState<InviteInfo | null>(null);

  useEffect(() => {
    const fetchInviteInfo = async () => {
      try {
        const res = await axiosInstance.get("/api/v1/group/invite-info", {
          params: { code: inviteId },
        });
        setInviteInfo(res.data.data);
        console.log("초대 정보 조회: ", res.data.data);
      } catch (err) {
        console.error("초대 정보 조회 실패:", err);
        alert("초대 정보를 불러오는 데 실패했어요.");
      }
    };

    if (inviteId) fetchInviteInfo();
  }, [inviteId]);

  if (!inviteInfo) {
    return (
      <div className="text-center mt-20">초대 정보를 불러오는 중입니다.</div>
    );
  }

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
                {inviteInfo.inviterName}
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
            text={
              inviteInfo.alreadyJoined
                ? "이미 참여한 그룹이에요"
                : "초대 수락하기"
            }
            color="mint"
            disabled={inviteInfo.alreadyJoined}
            onClick={async () => {
              if (inviteInfo.alreadyJoined) return;

              try {
                const res = await axiosInstance.post(
                  "/api/v1/group/invite/accept",
                  {
                    code: inviteId,
                  }
                );
                const groupId = res.data.data.groupId;
                navigate(`/group/${groupId}`);
              } catch (err) {
                console.error("초대 수락 실패:", err);
                alert("초대 수락에 실패했어요.");
              }
            }}
          />
        </div>
      </div>
    </div>
  );
}
