// src/pages/diary/DiaryEditPage.tsx
import React, { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import PageTitle from "../../components/PageTitle";
import DiaryEditCard from "../../components/diaryCreate/DiaryEditCard";
import Button from "../../components/common/Button";

interface TempDiary {
  title: string;
  dateText: string;
  emotion: "행복" | "기쁨" | "슬픔" | "화남" | "그냥그래";
  content: string;
}

export default function DiaryEditPage() {
  const navigate = useNavigate();
  const location = useLocation();

  // ① navigate로 보낸 state를 꺼내서
  const diary = (location.state as TempDiary) || {
    title: "",
    dateText: "",
    emotion: "행복" as const,
    content: "",
  };

  // ② 그걸 그대로 useState 초기값으로 사용
  const [title, setTitle] = useState(diary.title);
  const [content, setContent] = useState(diary.content);

  const handleSave = () => {
    // TODO: 수정 API 호출
    console.log("수정할 제목:", title);
    console.log("수정할 내용:", content);
    navigate(-1); // 저장 후 이전 페이지로 돌아가기
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
          <Button text="수정 완료" type="fill" size="L" onClick={handleSave} />
        </div>
      </div>
    </div>
  );
}
