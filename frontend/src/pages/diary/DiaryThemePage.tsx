import React from "react";
import PageTitle from "../../components/PageTitle";
import ThemaList from "../../components/common/ThemaList";

export default function DiaryThemePage() {
  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      <div className="w-[95%] mx-auto">
        <PageTitle
          title="일기 설정하기"
          subtitle="생성될 일기의 어투를 설정해주세요"
        />

        <ThemaList />
      </div>
    </div>
  );
}
