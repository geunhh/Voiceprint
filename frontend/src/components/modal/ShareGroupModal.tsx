// src/components/modal/ShareGroupModal.tsx
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import closeIcon from "../../assets/icons/close.png";
import folderIcon from "../../assets/icons/folderYellow.png"; // (선택) 헤더용
import profileSelect from "../../assets/icons/profileSelect.png"; //  선택 표시
import Button from "../common/Button";
import AlertModal from "./AlertModal";

interface ShareGroupModalProps {
  /** 모달 닫기 콜백 */
  onClose: () => void;
}

interface Group {
  groupId: number;
  groupName: string;
  groupImageUrl: string;
  memberCount: number;
}

function ShareGroupModal({ onClose }: ShareGroupModalProps) {
  const [groups, setGroups] = useState<Group[]>([]);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  const navigate = useNavigate();

  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
  } | null>(null);
  const [nextDiaryId, setNextDiaryId] = useState<number | null>(null);

  useEffect(() => {
    /* 1) 그룹 목록 불러오기 */
    const fetchGroups = async () => {
      try {
        const res = await axiosInstance.get("/api/v1/group/my");
        setGroups(res.data.data);
      } catch (e) {
        console.error("그룹 목록 조회 실패:", e);
        setGroups([]); // 실패 → 빈 목록 처리
      } finally {
        setLoading(false);
      }
    };
    fetchGroups();
  }, []);

  /* 2) 그룹 선택 토글 */
  const toggleSelect = (id: number) =>
    setSelectedIds((prev) => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });

  const filteredGroups = groups.filter((g) =>
    g.groupName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  /* 3) 저장 & 공유 실행 */
  const handleSave = async () => {
    try {
      /* 3-1) 임시 일기 저장 */
      const saveRes = await axiosInstance.post(
        "/api/chat/diary/temp/confirm",
        {}
      );
      const diaryId: number = saveRes.data.data.diaryId;

      /* 3-2) 선택된 그룹이 있다면 공유 */
      if (selectedIds.size) {
        await axiosInstance.post(`/api/diaries/shared/${diaryId}`, {
          groupIds: Array.from(selectedIds),
        });
      }

      // 3-3) 성공 모달 표시 → 확인 누르면 이동
      setNextDiaryId(diaryId);
      setAlert({
        message: "오늘의 자국을 \n성공적으로 남겼어요!",
        type: "success",
      });
    } catch (err) {
      console.error(err);
      setAlert({
        message: "일기 저장 또는 그룹 공유에 \n실패했습니다.",
        type: "fail",
      });
    }
  };

  /* 4) 렌더 */
  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center">
      <div className="w-4/5 max-w-[360px] rounded-xl bg-white flex flex-col items-center py-6 relative">
        {/*  헤더  */}
        <img
          src={closeIcon}
          alt="닫기"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />

        <img src={folderIcon} alt="" className="w-20 mb-2" />

        <p className="text-xl font-bold mb-1">저장하시겠습니까?</p>

        {loading ? (
          <p className="text-yellow-500 mt-8">그룹을 불러오는 중...</p>
        ) : groups.length ? (
          <>
            <p className="text-yellow-500 mb-3">공유할 그룹이 있어요!</p>

            {/* 검색 바 */}
            <div className="w-full flex items-center gap-2 px-5 mb-4">
              <input
                type="text"
                placeholder="그룹 검색"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="flex-1 py-2 pl-3 pr-10 rounded-full bg-gray-100 focus:bg-white focus:ring-1 focus:ring-yellow-400 outline-none"
              />
              {/* 필요하면 검색 아이콘 배치 */}
            </div>
            {/* ▼ 동그라미 아바타 그리드 ▼ */}
            <div className="grid grid-cols-3 gap-x-6 gap-y-8 px-5 max-h-64 overflow-y-auto mb-6 justify-items-start">
              {filteredGroups.map((g) => {
                const isSelected = selectedIds.has(g.groupId);
                return (
                  <div
                    key={g.groupId}
                    onClick={() => toggleSelect(g.groupId)}
                    className="cursor-pointer relative flex flex-col items-center w-20"
                  >
                    {/* 아바타 */}
                    <img
                      src={g.groupImageUrl}
                      alt={g.groupName}
                      className={`w-20 h-20 rounded-full object-cover border-2 transition
            ${isSelected ? "border-yellow-400" : "border-transparent"}`}
                    />

                    {/* 이름(최대 2줄) */}
                    <span className="mt-1 w-full text-center text-xs leading-[14px] h-[28px] break-words overflow-hidden">
                      {g.groupName}
                    </span>

                    {/* 체크아이콘 */}
                    {isSelected && (
                      <img
                        src={profileSelect}
                        alt="선택됨"
                        className="w-6 h-6 absolute bottom-10 right-0"
                      />
                    )}
                  </div>
                );
              })}
            </div>
          </>
        ) : (
          /*  그룹 없음 상태  */
          <div className="flex flex-col items-center gap-1 my-8">
            <p className="text-yellow-500 font-medium">공유할 그룹은 없네요!</p>
            <p className="text-sm text-gray-500">일단 저장만 진행할게요.</p>
          </div>
        )}

        {/* 하단 버튼  */}
        <div className="w-full px-6 mt-auto flex justify-center gap-4">
          <Button type="line" size="M" text="취소" onClick={onClose} />
          <Button type="fill" size="M" text="저장" onClick={handleSave} />
        </div>

        {alert && (
          <AlertModal
            message={alert.message}
            type={alert.type}
            onClose={() => {
              setAlert(null);
              // 성공 모달이었다면 이동
              if (alert.type === "success" && nextDiaryId) {
                navigate(`/diary/${nextDiaryId}`);
              }
            }}
          />
        )}
      </div>
    </div>
  );
}

export default ShareGroupModal;
