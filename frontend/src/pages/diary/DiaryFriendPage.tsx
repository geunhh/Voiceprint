import React from "react";
import ChatSelector from "../../components/diaryCreate/ChatSelector";
import PageTitle from "../../components/PageTitle";

export default function DiaryFriendPage() {
  return (
    <div className="flex flex-col items-center min-h-screen bg-white">
      {/* 90퍼센트만 이용 */}
      <div className="w-[95%] mx-auto">
        {/* 상단 타이틀 */}
        <PageTitle
          title="대화 친구 선택하기"
          subtitle="오늘 하루 대화 나눌 친구를 선택해주세요"
        />

        <ChatSelector />
      </div>
    </div>
  );
}
