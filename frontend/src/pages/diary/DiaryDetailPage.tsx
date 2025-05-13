import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";

import DiaryContent from "../../components/diary/DiaryContent";
import ChatHistoryModal from "../../components/modal/ChatHistoryModal";
import CustomThemaModal from "../../components/modal/CustumThemaModal";

import happyCharacter from "../../assets/icons/happyCharacter.png";
import lovelyCharacter from "../../assets/icons/lovelyCharacter.png";
import emotionTag1 from "../../assets/temp/emotionTag1.png";
import emotionTag2 from "../../assets/temp/emotionTag2.png";
import emotionTag3 from "../../assets/temp/emotionTag3.png";
import emotionTag4 from "../../assets/temp/emotionTag4.png";
import emotionTag5 from "../../assets/temp/emotionTag5.png";

// 감정 태그 매핑
const emotionTagMap: Record<string, string> = {
  행복: emotionTag1,
  설렘: emotionTag2,
  피곤: emotionTag3,
  짜증: emotionTag4,
  우울: emotionTag5,
};

interface DiaryData {
  diaryId: number;
  title: string;
  content: string;
  emotion: string | null;
  createdAt: string;
  authorNickname: string;
  thumbnail: string | null;
}

export default function DiaryDetailPage() {
  const { diaryId } = useParams<{ diaryId: string }>();
  const [diary, setDiary] = useState<DiaryData | null>(null);

  const [showChatModal, setShowChatModal] = useState(false);
  const [showThemaModal, setShowThemaModal] = useState(false);

  useEffect(() => {
    const fetchDiary = async () => {
      try {
        const res = await axiosInstance.get(`/api/diaries/diary/${diaryId}`);

        setDiary(res.data.data);
      } catch (err) {
        console.error("다이어리 불러오기 실패", err);
      }
    };

    if (diaryId) fetchDiary();
  }, [diaryId]);

  if (!diary) return null;

  const date = new Date(diary.createdAt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  const emotionTagImage = emotionTagMap[diary.emotion];

  return (
    <div className="pb-28">
      {/* 날짜 및 감정 태그 */}
      <div className="flex gap-2 mt-10 ml-4 items-center">
        <p className="font-semibold text-gray-500 text-lg">
          {year}.{month}.{day}
        </p>
        <img src={emotionTagImage} alt="감정태그" className="w-12 h-5" />
      </div>

      {/* 일기 제목 */}
      <div className="ml-4 mb-4">
        <p className="font-semibold text-xl"> {diary.title}</p>
      </div>

      {/* 일기 내용 */}
      <div className="mb-4">
        <DiaryContent content={diary.content} />
      </div>

      {/* 이전 채팅 기록 버튼 */}
      <div
        className="flex items-center gap-4 mx-auto w-11/12 rounded-2xl bg-yellow-50 p-2 mb-4"
        onClick={() => setShowChatModal(true)}
      >
        <img src={happyCharacter} className="w-20 h-auto" alt="캐릭터" />
        <div className="flex-col start">
          <p className="text-yellow-400 font-bold text-lg mb-1">대화 기록</p>
          <p className="text-gray-500 text-base">
            그때 나눴던 대화가 궁금하다면
            <br />
            버튼을 클릭해 확인할 수 있어요!
          </p>
        </div>
      </div>

      {/* 커스텀 테마 생성 버튼 */}
      <div
        className="flex items-center justify-between gap-4 mx-auto w-11/12 rounded-2xl bg-lightmint p-2"
        onClick={() => setShowThemaModal(true)}
      >
        <div className="flex-col start ml-4">
          <p className="text-darkmint font-bold text-lg mb-1">
            커스텀 테마 생성
          </p>
          <p className="text-gray-500 text-base">
            생성된 일기가 마음에 든다면
            <br />
            커스텀 테마 생성에 활용해 보세요!
          </p>
        </div>
        <img src={lovelyCharacter} className="w-auto h-24" alt="캐릭터" />
      </div>

      {showChatModal && (
        <ChatHistoryModal onClose={() => setShowChatModal(false)} />
      )}

      {showThemaModal && (
        <CustomThemaModal onClose={() => setShowThemaModal(false)} />
      )}
    </div>
  );
}
