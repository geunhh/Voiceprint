// src/pages/diary/DiaryEditPage.tsx
import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import PageTitle from "../../components/PageTitle";
import DiaryEditCard from "../../components/diaryCreate/DiaryEditCard";
import Button from "../../components/common/Button";
import AlertModal from "../../components/modal/AlertModal";
import axios from "axios";

interface TempDiary {
  title: string;
  dateText: string;
  emotion: "행복" | "설렘" | "피곤" | "짜증" | "우울";
  content: string;
}

export default function DiaryEditPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const diary = (location.state as TempDiary) || {
    title: "",
    dateText: "",
    emotion: "피곤" as const,
    content: "",
  };

  const [title, setTitle] = useState(diary.title);
  const [content, setContent] = useState(diary.content);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
    callback?: () => void;
  } | null>(null);

  const handleSave = async () => {
    setIsSubmitting(true);
    try {
      await axios.patch(
        `${import.meta.env.VITE_API_BASE_URL}/api/chat/diary/temp/update`,
        { title, diary: content },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );
      setAlert({
        message: "일기 수정이 완료되었습니다.",
        type: "success",
        callback: () => navigate(-1), // 모달 닫을 때 이동
      });
    } catch (err) {
      console.error("일기 수정 실패:", err);
      setAlert({
        message: "일기 수정에 실패했습니다.",
        type: "fail",
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      <div className="w-[95%] mx-auto">
        <PageTitle
          title="생성된 일기 수정하기"
          subtitle="완성된 일기를 직접 수정해보세요!"
        />

        <DiaryEditCard
          dateText={diary.dateText}
          emotion={diary.emotion}
          title={title}
          content={content}
          onTitleChange={setTitle}
          onContentChange={setContent}
        />

        <div className="mt-6 flex justify-center">
          <Button
            text={isSubmitting ? "수정 중..." : "수정 완료"}
            type="fill"
            size="L"
            onClick={handleSave}
          />
        </div>
      </div>

      {alert && (
        <AlertModal
          message={alert.message}
          type={alert.type}
          onClose={() => {
            setAlert(null);
            if (alert.callback) alert.callback();
          }}
        />
      )}
    </div>
  );
}
