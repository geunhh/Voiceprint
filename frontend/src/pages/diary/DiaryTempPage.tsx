/* src/pages/diary/DiaryTempPage.tsx */
import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import Button from "../../components/common/Button";
import PageTitle from "../../components/common/PageTitle";
import DiaryEntryCard from "../../components/diaryCreate/DiaryEntryCard";
import AlertModal from "../../components/modal/AlertModal";

interface Diary {
  title: string;
  dateText: string;
  emotion: "행복" | "기쁨" | "슬픔" | "화남" | "그냥그래";
  content: string;
}

export default function DiaryTempPage() {
  const navigate = useNavigate();

  const [hovered, setHovered] = useState<"edit" | "save" | "rewrite" | null>(
    null
  );
  const [diary, setDiary] = useState<Diary | null>(null);

  const getButtonType = (btn: "edit" | "save" | "rewrite") => {
    if (hovered) {
      return hovered === btn ? "fill" : "line";
    }
    return btn === "save" ? "fill" : "line";
  };

  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
  } | null>(null);

  // 임시 일기 데이터 조회
  const fetchDiary = async () => {
    try {
      const { data } = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/diary/temp`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      const { title, diary: content, createdAt, emotion } = data.data;
      const date = new Date(createdAt);
      const dateText = `${date.getMonth() + 1}월 ${date.getDate()}일 (${"일월화수목금토"[date.getDay()]})`;

      setDiary({ title, content, emotion, dateText });
    } catch (err) {
      console.error("임시 일기 불러오기 실패:", err);
    }
  };

  useEffect(() => {
    fetchDiary();
  }, []);

  // 임시 일기 재생성
  const handleEdit = async () => {
    try {
      await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/diary/temp/retry`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );
      setAlert({
        message: "일기를 다시 생성하고 있어요!",
        type: "success",
      });
    } catch (err) {
      console.error("일기 재생성 실패:", err);
      setAlert({
        message: "일기 재생성에 실패했어요!",
        type: "fail",
      });
    }
  };

  // 일기 생성(확정) post 요청
  const handleSave = async () => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/diary/temp/confirm`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );
      const { diaryId } = res.data.data;
      navigate(`/diary/${diaryId}`);
    } catch (err) {
      console.error("일기 저장 실패:", err);
      setAlert({
        message: "일기 저장에 실패했습니다.",
        type: "fail",
      });
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      <div className="w-[95%] mx-auto">
        <PageTitle
          title="생성된 일기 확인하기"
          subtitle="대화를 통해 완성된 일기를 확인해보세요"
        />
        {diary && <DiaryEntryCard {...diary} />}

        {/* 재생성 버튼 (상대적 위치) */}
        <div className="flex justify-end mt-6 mr-6">
          <button onClick={handleEdit} title="다시 생성">
            <img
              src="/src/assets/icons/button/arrowButton.png"
              alt="재생성"
              className="w-5 h-5 hover:scale-110 transition-transform"
            />
          </button>
        </div>
        {/* 수정/저장 버튼 영역 */}
        <div className="mt-6 flex flex-row justify-center items-center gap-4">
          <Button
            text="수정"
            type="line"
            size="M"
            onClick={() => navigate("edit", { state: diary })}
          />
          <Button text="저장" type="fill" size="M" onClick={handleSave} />
        </div>

        {alert && (
          <AlertModal
            message={alert.message}
            type={alert.type}
            onClose={() => setAlert(null)}
          />
        )}
      </div>
    </div>
  );
}
