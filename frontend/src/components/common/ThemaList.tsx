// src/components/common/ThemaList.tsx
import { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

import themaCharacter1 from "../../assets/icons/themaCharacter1.png";
import themaCharacter2 from "../../assets/icons/themaCharacter2.png";
import themaCharacter3 from "../../assets/icons/themaCharacter3.png";
import themaCharacter4 from "../../assets/icons/themaCharacter4.png";

import Button from "./Button";
import ThemaItem from "./ThemaItem";
import AlertModal from "../modal/AlertModal";

interface Thema {
  id: number;
  title: string;
  description: string;
  example: string;
}

interface ThemaListResponse {
  default_themes: Thema[];
  custom_themes: Thema[];
}

function ThemaList() {
  const navigate = useNavigate();

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [examples, setExamples] = useState<{ [id: number]: string }>({});
  const [themas, setThemas] = useState<ThemaListResponse>({
    default_themes: [],
    custom_themes: [],
  });
  const [alert, setAlert] = useState<{
    message: string;
    type: "success" | "fail";
    callback?: () => void;
  } | null>(null);

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

  const handleExampleSubmit = (id: number, value: string) => {
    setExamples((prev) => ({ ...prev, [id]: value }));
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

  return (
    <div className="flex flex-col items-center px-4 py-2">
      <div className="w-full mb-5">
        {themas.default_themes.map((thema) => (
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

        {themas.custom_themes.map((thema) => (
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
