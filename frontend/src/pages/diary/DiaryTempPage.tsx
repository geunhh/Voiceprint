import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";
import arrowButton from "../../assets/icons/button/arrowButton.png";
import happyIcon from "../../assets/icons/happyCharacter.png";
import questionIcon from "../../assets/icons/questionCharacter.png";
import Button from "../../components/common/Button";
import PageTitle from "../../components/common/PageTitle";
import DiaryEntryCard from "../../components/diaryCreate/DiaryEntryCard";
import AlertModal from "../../components/modal/AlertModal";
import ShareGroupModal from "../../components/modal/ShareGroupModal";

interface Diary {
  title: string;
  dateText: string;
  emotion: "행복" | "설렘" | "피로" | "짜증" | "우울";
  content: string;
}

export default function DiaryTempPage() {
  const navigate = useNavigate();

  const [hovered, setHovered] = useState<"edit" | "save" | "rewrite" | null>(
    null
  );
  const [diary, setDiary] = useState<Diary | null>(null);

  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
    callback?: () => void;
  } | null>(null);

  const [isAlertClosed, setIsAlertClosed] = useState(false); // X 버튼 눌렀는지 여부

  const getButtonType = (btn: "edit" | "save" | "rewrite") => {
    if (hovered) return hovered === btn ? "fill" : "line";
    return btn === "save" ? "fill" : "line";
  };

  const [showShareModal, setShowShareModal] = useState(false);

  // 세션 상태 확인 후 상황에 따라 알림 설정
  const checkSessionStatus = async () => {
    try {
      const res = await axiosInstance.get("/api/chat/session/status");
      const status = res.data.data;

      if (status !== "DIARY_DONE") {
        if (status === "DIARY_SAVED") {
          setAlert({
            message: "오늘 하루 일기를 이미 완성했어요!",
            type: "fail",
            callback: () => navigate("/my"),
          });
        } else if (status === "DIARY_CREATING") {
          setAlert({
            message: "현재 일기 생성 중이에요! \n메인으로 이동합니다.",
            type: "fail",
            callback: () => navigate("/main"),
          });
        } else if (status === "IN_PROGRESS") {
          setAlert({
            message: "아직 대화 중이에요! \n채팅으로 이동합니다.",
            type: "fail",
            callback: () => navigate("/diary/chat"),
          });
        } else {
          setAlert({
            message: "아직 일기를 시작하지 않았어요. \n지금 시작할까요?",
            type: "fail",
            callback: () => navigate("/diary/setting/theme"),
          });
        }
      }
    } catch (err) {
      console.error("세션 상태 확인 실패:", err);
      setAlert({
        message: "세션 상태 확인 중 오류가 발생했어요.",
        type: "fail",
        callback: () => navigate("/main"),
      });
    }
  };

  // 임시 일기 데이터 불러오기
  const fetchDiary = async () => {
    try {
      const { data } = await axiosInstance.get("/api/chat/diary/temp");
      const { title, diary: content, createdAt, emotion } = data.data;

      const date = new Date(createdAt);
      const dateText = `${date.getMonth() + 1}월 ${date.getDate()}일 (${"일월화수목금토"[date.getDay()]})`;

      setDiary({ title, content, emotion, dateText });
    } catch (err) {
      console.error("임시 일기 불러오기 실패:", err);
    }
  };

  useEffect(() => {
    checkSessionStatus();
    fetchDiary();
  }, []);

  // 일기 재생성 요청
  const handleEdit = async () => {
    try {
      await axiosInstance.post("/api/chat/diary/temp/retry", {});
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

  // // 일기 저장 요청
  // const handleSave = async () => {
  //   try {
  //     const res = await axiosInstance.post("/api/chat/diary/temp/confirm", {});
  //     const { diaryId } = res.data.data;
  //     navigate(`/diary/${diaryId}`);
  //   } catch (err) {
  //     console.error("일기 저장 실패:", err);
  //     setAlert({
  //       message: "일기 저장에 실패했습니다.",
  //       type: "fail",
  //     });
  //   }
  // };

  // 안내 화면 (AlertModal 닫힌 후 보일 전체 안내 UI)
  const renderEmptyState = () => {
    const icon = alert?.type === "success" ? happyIcon : questionIcon;
    return (
      <div className="flex flex-col items-center justify-center min-h-screen text-center px-4 bg-white">
        <img src={icon} alt="알림" className="w-28 h-32 mb-4" />
        <h2 className="text-gray-800 text-lg font-semibold whitespace-pre-line mb-6">
          {alert?.message}
        </h2>
        <Button
          text="확인"
          type="fill"
          size="L"
          onClick={() => {
            setIsAlertClosed(false);
            setAlert(null);
            alert?.callback?.();
          }}
        />
      </div>
    );
  };

  return (
    <div className="w-[95%] mx-auto">
      {alert ? (
        renderEmptyState()
      ) : (
        <>
          <PageTitle
            title="생성된 일기 확인하기"
            subtitle="대화를 통해 완성된 일기를 확인해보세요"
          />

          {diary && <DiaryEntryCard {...diary} />}

          <div className="flex justify-end mt-6 mr-6">
            <button onClick={handleEdit} title="다시 생성">
              <img
                src={arrowButton}
                alt="재생성"
                className="w-5 h-5 hover:scale-110 transition-transform"
              />
            </button>
          </div>

          <div className="mt-6 flex flex-row justify-center items-center gap-4">
            <Button
              text="수정"
              type="line"
              size="M"
              onClick={() => navigate("edit", { state: diary })}
            />
            <Button
              text="저장"
              type="fill"
              size="M"
              onClick={() => setShowShareModal(true)}
            />
          </div>
        </>
      )}

      {/* 모달은 여전히 보여줄 수 있음 (isAlertClosed가 false일 때만) */}
      {alert && !isAlertClosed && (
        <AlertModal
          message={alert.message}
          type={alert.type}
          onClose={() => setIsAlertClosed(true)}
          callback={() => {
            setIsAlertClosed(false);
            setAlert(null);
            alert.callback?.();
          }}
        />
      )}

      {showShareModal && (
        <ShareGroupModal onClose={() => setShowShareModal(false)} />
      )}
    </div>
  );
}
