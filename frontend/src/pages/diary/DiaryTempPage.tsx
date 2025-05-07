/* src/pages/diary/DiaryTempPage.tsx */
import React, { useState, useEffect } from "react";
import DiaryEntryCard from "../../components/diaryCreate/DiaryEntryCard";
import PageTitle from "../../components/PageTitle";
import Button from "../../components/common/Button";
import { useNavigate } from "react-router-dom";
import axios from "axios";

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
      alert("일기를 다시 생성하고 있어요! 잠시 후 다시 확인해 주세요.");
    } catch (err) {
      console.error("일기 재생성 실패:", err);
      alert("일기 재생성에 실패했습니다.");
    }
  };

  const handleSave = async () => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/diaries/confirm`,
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
      alert("일기 저장에 실패했습니다.");
    }
  };

  const handleRewrite = async () => {
    try {
      const updated = await axios.put(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/diary/temp/update`,
        {
          title: "제목을 수정해보았어요",
          diary: "일기 내용을 이렇게 바꿨어요!",
        },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );
      alert("수정이 완료되었습니다.");
      fetchDiary();
    } catch (err) {
      console.error("일기 수정 실패:", err);
      alert("일기 수정에 실패했습니다.");
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

        <div className="mt-6 flex justify-center items-center gap-4">
          <div
            onMouseEnter={() => setHovered("edit")}
            onMouseLeave={() => setHovered("save")}
          >
            <Button
              text="재생성"
              type={getButtonType("edit")}
              size="M"
              onClick={handleEdit}
            />
          </div>

          <div
            onMouseEnter={() => setHovered("rewrite")}
            onMouseLeave={() => setHovered("save")}
          >
            <Button
              text="수정"
              type={getButtonType("rewrite")}
              size="M"
              onClick={handleRewrite}
            />
          </div>

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
