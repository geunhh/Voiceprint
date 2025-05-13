import { useState } from "react";
import { useNavigate } from "react-router";
import axiosInstance from "../../api/axiosInstance";
import {
  create,
  group_default,
  group_selected,
  main_default,
  main_selected,
  my_default,
  my_selected,
  temp_default,
  temp_selected,
} from "../../assets/icons";
import AlertModal from "../modal/AlertModal";

interface TabbarProps {
  type: "Main" | "Temp" | "Create" | "Group" | "My";
  onClick: () => void;
  name?: string;
}

const Tabbar = ({ type: currentType }: TabbarProps) => {
  const navigate = useNavigate();
  const BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
    callback?: () => void;
  } | null>(null);

  const handleCreateClick = async () => {
    try {
      const res = await axiosInstance.get("/api/thema/using");

      const themaId = res.data.data?.themaId;
      if (themaId === null) {
        navigate("/diary/setting/theme");
      } else {
        navigate("/diary/setting/friend");
      }
    } catch (err) {
      console.error("테마 조회 실패:", err);
      navigate("/diary/setting/theme");
    }
  };

  const handleTempClick = async () => {
    try {
      const res = await axiosInstance.get("/api/chat/session/status");

      const status = res.data.data;
      if (status === "DIARY_DONE") {
        navigate("/diary/temp");
      } else if (status === "DIARY_SAVED") {
        setAlert({
          message: "오늘 하루 일기를 이미 완성했어요!",
          type: "fail",
          callback: () => navigate("/my"),
        });
      } else if (status === "DIARY_CREATING") {
        setAlert({
          message: "현재 일기 생성 중이에요! 메인으로 이동합니다",
          type: "fail",
          callback: () => navigate("/main"),
        });
      } else if (status === "IN_PROGRESS") {
        setAlert({
          message: "아직 대화 중이에요! 채팅으로 이동합니다.",
          type: "fail",
          callback: () => navigate("/diary/chat"),
        });
      } else {
        setAlert({
          message: "아직 일기를 시작하지 않았어요. 지금 시작할까요?",
          type: "fail",
          callback: () => navigate("/diary/setting/theme"),
        });
      }
    } catch (err) {
      console.error("세션 상태 조회 실패:", err);
      setAlert({
        message: "세션 상태 조회에 실패했어요.",
        type: "fail",
        callback: () => navigate("/main"),
      });
    }
  };

  const tabItems: TabbarProps[] = [
    { type: "Main", onClick: () => navigate("/main"), name: "메인" },
    { type: "Temp", onClick: handleTempClick, name: "임시" },
    { type: "Create", onClick: handleCreateClick },
    { type: "Group", onClick: () => navigate("/group"), name: "그룹" },
    { type: "My", onClick: () => navigate("/my"), name: "마이" },
  ];

  const getIconSource = (itemType: TabbarProps["type"]) => {
    const isSelected = itemType === currentType;

    switch (itemType) {
      case "Main":
        return isSelected ? main_selected : main_default;
      case "Temp":
        return isSelected ? temp_selected : temp_default;
      case "Create":
        return create;
      case "Group":
        return isSelected ? group_selected : group_default;
      case "My":
        return isSelected ? my_selected : my_default;
      default:
        return "";
    }
  };

  return (
    <>
      <div className="flex justify-around items-center h-16 border border-gray-100 border-opacity-60 bg-white">
        {tabItems.map((item) => (
          <div
            key={item.type}
            className={`flex flex-col items-center justify-center flex-1 ${
              item.type === currentType ? "text-black" : "text-gray-400"
            }`}
            onClick={item.onClick}
          >
            <div
              className={`$${
                item.type === "Create"
                  ? "flex items-center justify-center -mt-16"
                  : ""
              }`}
            >
              <img
                src={getIconSource(item.type)}
                alt={item.name}
                className={`$${
                  item.type === "Create" ? "w-20 h-20" : "w-6 h-6"
                }`}
              />
            </div>
            {item.name && item.type !== "Create" && (
              <div className="text-xs mt-1">{item.name}</div>
            )}
          </div>
        ))}
      </div>

      {alert && (
        <AlertModal
          message={alert.message}
          type={alert.type}
          onClose={() => setAlert(null)} // 닫기 버튼: 단순 닫기만
          callback={() => {
            setAlert(null);
            alert.callback?.(); // 확인 버튼: 닫고 + 이동
          }}
        />
      )}
    </>
  );
};

export default Tabbar;
