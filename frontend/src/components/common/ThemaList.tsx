// src/components/common/ThemaList.tsx
import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import axiosInstance from "../../api/axiosInstance";

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

export default function ThemaList() {
  const { diaryId } = useParams<{ diaryId: string }>();
  const navigate = useNavigate();

  const [usingId, setUsingId] = useState<number | null>(null);
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

  // 1) 현재 사용 중인 테마 조회
  useEffect(() => {
    async function fetchUsing() {
      try {
        const res = await axiosInstance.get("/api/thema/using");
        const id = res.data.data?.themaId ?? null;
        setUsingId(id);
      } catch (err) {
        console.error("테마 조회 실패:", err);
      }
    }
    fetchUsing();
  }, []);

  // 2) 조회된 usingId에 따라 선택 강조
  useEffect(() => {
    if (usingId !== null) {
      setSelectedId(usingId);
    }
  }, [usingId]);

  // 3) 테마 목록 가져오기
  useEffect(() => {
    async function fetchThemas() {
      try {
        const res = await axiosInstance.get("/api/thema/all");
        const { default_themas, custom_themas } = res.data.data;
        const customWithFallback =
          custom_themas.length === 0
            ? [
                {
                  id: 9999,
                  title: "내 커스텀 테마",
                  description: "나만의 스타일로 일기를 써보세요!",
                  example: "",
                },
              ]
            : custom_themas;
        setThemas({ default_themas, custom_themas: customWithFallback });
        // console.log("테마 목록 조회: ", res.data.data);
      } catch (err) {
        console.error("테마 목록 조회 실패:", err);
        setAlert({ message: "테마 목록을 불러오지 못했습니다.", type: "fail" });
      }
    }
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

  const handleExampleSubmit = (id: number, value: string) => {
    setExamples((prev) => ({ ...prev, [id]: value }));
    setSelectedId(id);
  };

  const handleSubmit = async () => {
    if (!selectedId) {
      setAlert({ message: "테마를 선택해주세요.", type: "fail" });
      return;
    }

    if (usingId !== null && selectedId === usingId) {
      setAlert({ message: "똑같아요\n변경하고 요청해주세요!", type: "fail" });
      return;
    }

    try {
      // 이미 설정된 테마가 있을 때
      if (usingId !== null) {
        if (selectedId === usingId) {
          // 선택 테마와 동일 -> 수정
          await axiosInstance.patch(`/api/thema/extract/${diaryId}`);
          setAlert({
            message: "테마가 수정되었습니다.",
            type: "success",
            callback: () => navigate("/my/edit"),
          });
        } else {
          // 다른 테마 선택 -> 변경
          if (selectedId === 9999 && examples[selectedId]) {
            const res = await axiosInstance.post("/api/thema/create", {
              exampleDiary: examples[selectedId],
            });
            const { themaId, example } = res.data.data;
            setExamples((prev) => ({ ...prev, [themaId]: example }));
            setThemas((prev) => ({
              ...prev,
              custom_themas: [
                {
                  id: themaId,
                  title: "내 커스텀 테마",
                  description: "",
                  example,
                },
                ...prev.custom_themas.filter((t) => t.id !== 9999),
              ],
            }));
            setAlert({
              message: "커스텀 테마가 생성되었습니다.",
              type: "success",
              callback: () => navigate("/diary/setting/friend"),
            });
          } else {
            await axiosInstance.put(`/api/thema/select/${selectedId}`);
            setAlert({
              message: "테마가 변경되었습니다.",
              type: "success",
              callback: () => navigate("/my/edit"),
            });
          }
        }
      } else {
        // 첫 설정
        if (selectedId === 9999 && examples[selectedId]) {
          const res = await axiosInstance.post("/api/thema/create", {
            exampleDiary: examples[selectedId],
          });
          const { themaId, example } = res.data.data;
          setExamples((prev) => ({ ...prev, [themaId]: example }));
          setThemas((prev) => ({
            ...prev,
            custom_themas: [
              {
                id: themaId,
                title: "내 커스텀 테마",
                description: "",
                example,
              },
              ...prev.custom_themas.filter((t) => t.id !== 9999),
            ],
          }));
          setAlert({
            message: "커스텀 테마가 생성되었습니다.",
            type: "success",
            callback: () => navigate("/diary/setting/friend"),
          });
        } else {
          await axiosInstance.put(`/api/thema/select/${selectedId}`);
          setAlert({
            message: "테마가 성공적으로 설정되었습니다.",
            type: "success",
            callback: () => navigate("/diary/setting/friend"),
          });
        }
      }
    } catch (err: any) {
      console.error("테마 설정 오류:", err);
      const status = err?.response?.status;
      let msg = "서버 오류가 발생했습니다.";
      if (status === 400) msg = "유효하지 않은 테마입니다.";
      else if (status === 403) msg = "해당 테마를 설정할 수 없습니다.";
      else if (status === 404) msg = "존재하지 않는 테마입니다.";
      setAlert({ message: msg, type: "fail" });
    }
  };

  return (
    <div className="flex flex-col items-center py-2">
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
