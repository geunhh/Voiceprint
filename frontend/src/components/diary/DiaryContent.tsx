interface DiaryContentProps {
  content: string;
  maxHeight?: number;
  minHeight?: number;
}

function DiaryContent({
  content,
  maxHeight = 500,
  minHeight = 300,
}: DiaryContentProps) {
  return (
    <div
      className={`
    diary-content
    w-11/12 mx-auto rounded-xl border border-yellow-400 bg-white 
    px-5 py-4 text-gray-700 text-sm leading-relaxed whitespace-pre-line 
    shadow-sm overflow-y-auto
  `}
      style={{ maxHeight, minHeight }}
    >
      {content}
    </div>
  );
}

export default DiaryContent;
