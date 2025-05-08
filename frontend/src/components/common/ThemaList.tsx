// src/components/common/ThemaList.tsx
import axios from "axios";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import themaCharacter1 from "../../assets/icons/themaCharacter1.png";
import themaCharacter2 from "../../assets/icons/themaCharacter2.png";
import themaCharacter3 from "../../assets/icons/themaCharacter3.png";
import themaCharacter4 from "../../assets/icons/themaCharacter4.png";

import AlertModal from "../modal/AlertModal";
import Button from "./Button";
import ThemaItem from "./ThemaItem";

interface Thema {
  id: number;
  title: string;
  description: string;
  example: string;
}

interface ThemaListResponse {
  default_themas: Thema[];
  custom_themas: Thema[];
}

function ThemaList() {
  const navigate = useNavigate();

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [examples, setExamples] = useState<{ [id: number]: string }>({});
  const [themas, setThemas] = useState<ThemaListResponse>({
    default_themas: [],
    custom_themas: [],
  });
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
    callback?: () => void;
  } | null>(null);

  // 테마 리스트 요청하기
  useEffect(() => {
    const fetchThemas = async () => {
      try {
        const res = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/api/thema/all`,
          {
            headers: {
              Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
              "Content-Type": "application/json",
            },
          }
        );
        setThemas(res.data.data);
      } catch (err) {
        console.error("테마 목록 조회 실패:", err);
        setAlert({ message: "테마 목록을 불러오지 못했습니다.", type: "fail" });
      }
    };
    fetchThemas();
  }, []);

  const handleSelect = (id: number) => {
    setSelectedId((prev) => (prev === id ? null : id));
  };

  const getThemaCharacterImage = (id: number) => {
    switch (id) {
      case 1:
        return themaCharacter1;
      case 2:
        return themaCharacter2;
      case 3:
        return themaCharacter3;
      default:
        return themaCharacter4;
    }
  };

  // 테마 선택 보내기
  const handleSubmit = async () => {
    if (selectedId === null) {
      setAlert({ message: "테마를 선택해주세요.", type: "fail" });
      return;
    }

    try {
      await axios.put(
        `${import.meta.env.VITE_API_BASE_URL}/api/thema/select/${selectedId}`,
        {},
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      setAlert({
        message: "테마가 성공적으로 설정되었습니다.",
        type: "success",
        callback: () => navigate("/diary/friend"), // 챗봇 선택으로 이동
      });
    } catch (err: any) {
      const status = err?.response?.status;

      let message = "서버 오류가 발생했습니다.";
      if (status === 400) message = "유효하지 않은 테마입니다.";
      else if (status === 403) message = "해당 테마를 설정할 수 없습니다.";
      else if (status === 404) message = "존재하지 않는 테마입니다.";

      setAlert({ message, type: "fail" });
    }
  };

  // 커스텀 일기 생성 post 요청 보내기
  const handleExampleSubmit = async (id: number, diaryText: string) => {
    try {
      const res = await axios.post(
        `${import.meta.env.VITE_API_BASE_URL}/api/thema/create`,
        { exampleDiary: diaryText },
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );

      const { themaId, example } = res.data.data;
      setExamples((prev) => ({ ...prev, [themaId]: example }));
      setSelectedId(themaId);

      // 전체 목록 재조회 (또는 직접 추가)
      const updated = await axios.get(
        `${import.meta.env.VITE_API_BASE_URL}/api/thema/all`,
        {
          headers: {
            Authorization: `Bearer ${localStorage.getItem("Authorization")}`,
            "Content-Type": "application/json",
          },
        }
      );
      setThemas(updated.data.data);

      setAlert({
        message: "커스텀 테마가 생성되었어요!",
        type: "success",
      });
    } catch (err) {
      setAlert({
        message: "테마 생성 실패: 너무 짧은 예시거나 오류가 발생했어요.",
        type: "fail",
      });
    }
  };

  return (
    <div className="flex flex-col items-center px-4 py-2">
      <div className="w-full mb-5">
        {themas.default_themas.map((thema) => (
          <ThemaItem
            key={thema.id}
            id={thema.id}
            title={thema.title}
            description={thema.description}
            imageSrc={getThemaCharacterImage(thema.id)}
            example={thema.example}
            isSelected={selectedId === thema.id}
            onSelect={handleSelect}
          />
        ))}

        {themas.custom_themas.map((thema) => (
          <ThemaItem
            key={thema.id}
            id={thema.id}
            title={thema.title}
            description={thema.description}
            imageSrc={getThemaCharacterImage(thema.id)}
            example={examples[thema.id] || thema.example || ""}
            isCustom
            isSelected={selectedId === thema.id}
            onSelect={handleSelect}
            onExampleSubmit={handleExampleSubmit}
          />
        ))}
      </div>

      <Button text="테마 설정" type="fill" size="L" onClick={handleSubmit} />

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

export default ThemaList;
