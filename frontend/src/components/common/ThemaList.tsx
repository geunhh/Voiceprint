import { useState } from "react";
import themaCharacter1 from "../../assets/icons/themaCharacter1.png";
import themaCharacter2 from "../../assets/icons/themaCharacter2.png";
import themaCharacter3 from "../../assets/icons/themaCharacter3.png";
import themaCharacter4 from "../../assets/icons/themaCharacter4.png";
import Button from "./Button";
import ThemaItem from "./ThemaItem";

// 임시 데이터
const data = {
  default_themas: [
    {
      id: 1,
      title: "테마 A",
      description: "테마에 대한 간단한 설명",
      example:
        "테마에 대한 간단한 예시입니다. 테마로 작성한 일기를 간단하게 보여줘서 사용자가 어떤 어투를 선택할지 고를 수 있는거죠. 보다 자세한 내용을 우리 함께 고민해보고 결정하는 걸로 할까요?",
    },
    {
      id: 2,
      title: "테마 B",
      description: "테마에 대한 간단한 설명",
      example:
        "테마에 대한 간단한 예시입니다. 테마로 작성한 일기를 간단하게 보여줘서 사용자가 어떤 어투를 선택할지 고를 수 있는거죠. 보다 자세한 내용을 우리 함께 고민해보고 결정하는 걸로 할까요?",
    },
    {
      id: 3,
      title: "테마 C",
      description: "테마에 대한 간단한 설명",
      example:
        "테마에 대한 간단한 예시입니다. 테마로 작성한 일기를 간단하게 보여줘서 사용자가 어떤 어투를 선택할지 고를 수 있는거죠. 보다 자세한 내용을 우리 함께 고민해보고 결정하는 걸로 할까요?",
    },
  ],
  custom_themas: [
    {
      id: 10,
      title: "나만의 테마",
      description: "사용자 커스텀 테마",
      example: "",
    },
  ],
};

function ThemaList() {
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [examples, setExamples] = useState<{ [id: number]: string }>({});

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

  return (
    <div className="flex flex-col items-center px-4 py-2">
      <div className="w-full mb-5">
        {data.default_themas.map((thema) => (
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

        {data.custom_themas.map((thema) => (
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

      <Button
        text="테마 설정"
        type="fill"
        size="L"
        onClick={() => {
          console.log("선택된 테마 ID:", selectedId);
        }}
      />
    </div>
  );
}

export default ThemaList;
