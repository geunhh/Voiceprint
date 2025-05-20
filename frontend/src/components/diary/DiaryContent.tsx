import { useEffect, useRef, useState } from "react";

interface DiaryContentProps {
  content: string;
  emotion: string;
}

const borderColors: Record<string, string> = {
  행복: "border-[#FFA9A9]/50",
  설렘: "border-[#FFBA66]/50",
  피로: "border-[#FFE792]/50",
  짜증: "border-[#91DD4B]/50",
  우울: "border-[#7DBEFF]/50",
};

function DiaryContent({ content, emotion }: DiaryContentProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const [isCentered, setIsCentered] = useState(false);

  useEffect(() => {
    if (!containerRef.current || !contentRef.current) return;

    const containerHeight = containerRef.current.clientHeight;
    const contentHeight = contentRef.current.scrollHeight;

    setIsCentered(contentHeight < containerHeight);
  }, [content]);

  const borderColor =
    emotion && borderColors[emotion]
      ? borderColors[emotion]
      : "border-yellow-400";

  return (
    <div
      ref={containerRef}
      className={`w-full mx-auto h-96 overflow-y-auto rounded-xl bg-white 
                  px-5 py-4 shadow-sm border ${borderColor}`}
    >
      <div
        ref={contentRef}
        className={`text-sm text-gray-700 leading-relaxed whitespace-pre-line 
                    text-justify ${isCentered ? "flex items-center justify-center h-full" : ""}`}
      >
        {content}
      </div>
    </div>
  );
}

export default DiaryContent;
