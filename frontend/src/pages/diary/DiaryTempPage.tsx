/* src/pages/diary/DiaryTempPage.tsx */
import react, { useState } from "react";
import DiaryEntryCard from "../../components/diaryCreate/DiaryEntryCard";
import PageTitle from "../../components/PageTitle";
import Button from "../../components/common/Button";
import { useNavigate } from "react-router-dom";
// import ChatExistModal from "../../components/modal/ChatExistModal";";

// ─── 임시 더미 데이터 ───
const diary = {
  title: "오늘 회의 기록",
  dateText: "4월 26일 (토)",
  emotion: "슬픔" as const,
  content:
    "오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다. 오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다. 오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다.오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다. 오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다. 오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있",
};

export default function DiaryTempPage() {
  const navigate = useNavigate();

  // hover 상태: "edit" or "save" or null
  const [hovered, setHovered] = useState<"edit" | "save" | null>(null);

  // 저장 모달 여부
  const [modalOpen, setModalOpen] = useState(false);

  // 기본: save 버튼이 fill, edit은 line
  // hover 있을 때만 그 버튼이 fill, 나머지는 line
  const getButtonType = (btn: "edit" | "save") => {
    if (hovered) {
      return hovered === btn ? "fill" : "line";
    }
    return btn === "save" ? "fill" : "line";
  };

  const handleEdit = () => {
    // 기존 일기 데이터를 편집 페이지에 넘겨줍니다
    navigate("edit", { state: diary });
  };
  const handleSave = () => {
    // TODO: 저장 로직
    console.log("저장할 일기:", diary);
    navigate("/my"); // 마이페이지로
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      <div className="w-[95%] mx-auto">
        <PageTitle
          title="생성된 일기 확인하기"
          subtitle="대화를 통해 완성된 일기를 확인해보세요"
        />

        {/* 일기 카드 */}
        <DiaryEntryCard {...diary} />

        {/* 버튼 영역 */}
        <div className="mt-6 flex justify-center items-center gap-4">
          {/* 수정 버튼 */}
          <div
            onMouseEnter={() => setHovered("edit")}
            onMouseLeave={() => setHovered("save")}
          >
            <Button
              text="수정"
              type={getButtonType("edit")}
              size="M"
              onClick={handleEdit}
            />
          </div>

          {/* <ChatExistModal /> */}
          {/* 저장 버튼 */}
          <div
            onMouseEnter={() => setHovered("save")}
            onMouseLeave={() => setHovered("save")}
          >
            <Button
              text="저장"
              type={getButtonType("save")}
              size="M"
              onClick={handleSave}
            />
          </div>
        </div>
      </div>
    </div>
  );
}
