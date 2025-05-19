interface NotificationFilterTabsProps {
  selected: "all" | "newDiary" | "newComment" | "newMember";
  onSelect: (type: "all" | "newDiary" | "newComment" | "newMember") => void;
}

export default function NotificationFilterTabs({
  selected,
  onSelect,
}: NotificationFilterTabsProps) {
  return (
    <div className="bg-mint rounded-full p-1 flex justify-between w-full max-w-xs mx-auto">
      <TabButton
        label="전체"
        isSelected={selected === "all"}
        onClick={() => onSelect("all")}
      />
      <TabButton
        label="일기"
        isSelected={selected === "newDiary"}
        onClick={() => onSelect("newDiary")}
      />
      <TabButton
        label="댓글"
        isSelected={selected === "newComment"}
        onClick={() => onSelect("newComment")}
      />
      <TabButton
        label="멤버"
        isSelected={selected === "newMember"}
        onClick={() => onSelect("newMember")}
      />
    </div>
  );
}

interface TabButtonProps {
  label: string;
  isSelected: boolean;
  onClick: () => void;
}

function TabButton({ label, isSelected, onClick }: TabButtonProps) {
  return (
    <button
      onClick={onClick}
      className={`flex-1 py-2 text-sm font-semibold text-center rounded-full transition-all
        ${isSelected ? "bg-white shadow text-darkmint " : "text-white"}
      `}
    >
      {label}
    </button>
  );
}
