import { useEffect, useState } from "react";
import axiosInstance from "../../api/axiosInstance";
import closeIcon from "../../assets/icons/close.png";
import folderIcon from "../../assets/icons/folderYellow.png";
import profileSelect from "../../assets/icons/profileSelect.png";
import Button from "../common/Button";
import AlertModal from "./AlertModal";

interface DiaryShareGroupModalProps {
  onClose: () => void;
  diaryId: number;
}

interface Group {
  groupId: number;
  groupName: string;
  groupImageUrl: string;
  memberCount: number;
}

function DiaryShareGroupModal({ onClose, diaryId }: DiaryShareGroupModalProps) {
  const [groups, setGroups] = useState<Group[]>([]);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
  } | null>(null);

  useEffect(() => {
    const fetchGroups = async () => {
      try {
        const res = await axiosInstance.get("/api/v1/group/my");
        setGroups(res.data.data);
      } catch (e) {
        console.error("그룹 목록 조회 실패:", e);
        setGroups([]);
      } finally {
        setLoading(false);
      }
    };
    fetchGroups();
  }, []);

  const toggleSelect = (id: number) =>
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return next;
    });

  const filteredGroups = groups.filter((g) =>
    g.groupName.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSave = async () => {
    try {
      if (selectedIds.size) {
        await axiosInstance.post(`/api/diaries/shared/${diaryId}`, {
          groupIds: Array.from(selectedIds),
        });
      }

      setAlert({
        message: "그룹에 성공적으로 공유했어요!",
        type: "success",
      });
    } catch (err) {
      console.error(err);
      setAlert({
        message: "그룹 공유에 실패했어요.",
        type: "fail",
      });
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center">
      <div className="w-4/5 max-w-[360px] rounded-xl bg-white flex flex-col items-center py-6 relative">
        <img
          src={closeIcon}
          alt="닫기"
          className="w-6 absolute top-4 right-4 cursor-pointer"
          onClick={onClose}
        />
        <img src={folderIcon} alt="" className="w-20 mb-2" />
        <p className="text-xl font-bold mb-1">일기 공유하기</p>

        {loading ? (
          <p className="text-yellow-500 mt-8">그룹을 불러오는 중...</p>
        ) : groups.length ? (
          <>
            <p className="text-yellow-500 mb-3">공유할 그룹을 선택해주세요!</p>

            <div className="w-full flex justify-center gap-2 px-5 mb-4">
              <input
                type="text"
                placeholder="그룹 검색"
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full max-w-[300px] py-2 pl-3 pr-10 rounded-full bg-gray-100 focus:bg-white focus:ring-1 focus:ring-yellow-400 outline-none"
              />
            </div>

            <div className="grid grid-cols-3 gap-x-6 gap-y-8 px-5 max-h-64 overflow-y-auto mb-6 justify-items-start">
              {filteredGroups.map((g) => {
                const isSelected = selectedIds.has(g.groupId);
                return (
                  <div
                    key={g.groupId}
                    onClick={() => toggleSelect(g.groupId)}
                    className="cursor-pointer relative flex flex-col items-center w-20"
                  >
                    <img
                      src={g.groupImageUrl}
                      alt={g.groupName}
                      className={`w-20 h-20 rounded-full object-cover border-2 transition ${
                        isSelected ? "border-yellow-400" : "border-transparent"
                      }`}
                    />
                    <span className="mt-1 w-full text-center text-xs leading-[14px] h-[28px] break-words overflow-hidden">
                      {g.groupName}
                    </span>
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
          <div className="flex flex-col items-center gap-1 my-8">
            <p className="text-yellow-500 font-medium">가입한 그룹이 없어요</p>
            <p className="text-sm text-gray-500">
              그룹에 참여하고 친구들에게 공유해 보세요.
            </p>
          </div>
        )}

        {/* 하단 버튼 */}
        <div className="w-full px-6 mt-auto flex justify-center gap-4">
          <Button type="line" size="M" text="취소" onClick={onClose} />
          <Button
            type="fill"
            size="M"
            text="저장"
            onClick={handleSave}
            disabled={selectedIds.size === 0}
          />
        </div>

        {alert && (
          <AlertModal
            message={alert.message}
            type={alert.type}
            onClose={() => {
              setAlert(null);
              if (alert.type === "success") {
                onClose();
              }
            }}
          />
        )}
      </div>
    </div>
  );
}

export default DiaryShareGroupModal;
