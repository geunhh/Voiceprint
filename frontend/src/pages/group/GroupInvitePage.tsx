import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import axiosInstance from "../../api/axiosInstance";
import Button from "../../components/common/Button";

interface InviteInfo {
  groupName: string;
  groupImage: string;
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
      {/* 그라데이션 테두리 + 애니메이션 */}
      <div className="relative w-4/5 h-2/5 max-h-64 rounded-xl p-[2px] bg-gradient-to-r from-mint via-lightyellow to-mint animate-shimmer bg-[length:300%_100%] bg-repeat-x">
        {/* 실제 내용이 들어가는 흰색 내부 카드 */}
        <div className="bg-white w-full h-full rounded-xl flex flex-col justify-between p-2">
          {/* 안내 */}
          <div className="flex flex-col items-center justify-center gap-2 mt-5">
            <img
              src={inviteInfo.groupImage}
              className="h-24 w-24 rounded-full"
            />
            <div className="text-center">
              <p className="font-semibold text-gray-700 text-xl">
                {inviteInfo.groupName}
              </p>
            </div>
          </div>

          {/* 버튼 */}
          <div className="flex justify-center mb-6 mt-3">
            <Button
              type="fill"
              size="L"
              text={
                inviteInfo.alreadyJoined
                  ? "이미 참여한 그룹이에요"
                  : "그룹 참여하기"
              }
              color="mint"
              disabled={inviteInfo.alreadyJoined}
              onClick={async () => {
                if (inviteInfo.alreadyJoined) return;

                try {
                  const res = await axiosInstance.post(
                    "/api/v1/group/invite/accept",
                    {
                      inviteCode: inviteId,
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
    </div>
  );
}
