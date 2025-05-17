import { useEffect, useRef, useState } from "react";

interface DiaryContentProps {
  content: string;
}

function DiaryContent({ content }: DiaryContentProps) {
  const containerRef = useRef<HTMLDivElement>(null);
  const contentRef = useRef<HTMLDivElement>(null);
  const [isCentered, setIsCentered] = useState(false);

  useEffect(() => {
    if (!containerRef.current || !contentRef.current) return;

    const containerHeight = containerRef.current.clientHeight;
    const contentHeight = contentRef.current.scrollHeight;

    setIsCentered(contentHeight < containerHeight);
  }, [content]);

  return (
    <div
      ref={containerRef}
      className="w-full mx-auto h-96 overflow-y-auto rounded-xl border border-yellow-400 bg-white 
                 px-5 py-4 shadow-sm"
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
