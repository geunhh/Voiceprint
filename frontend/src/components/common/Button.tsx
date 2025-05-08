import "../../index.css";

interface ButtonProps {
  text: string;
  type: "fill" | "line";
  size: "S" | "M" | "L";
  onClick: () => void;
  color?: "mint"; // 선택적으로 색상 변경
}

const Button = ({ text, type, size, onClick, color }: ButtonProps) => {
  const baseStyle = "rounded-xl";
  const isMint = color === "mint";

  const typeStyle =
    type === "fill"
      ? isMint
        ? "bg-mint text-gray-700"
        : "bg-yellow-500 text-white"
      : isMint
        ? "border-2 border-mint text-mint"
        : "border-2 border-yellow-500 text-yellow-500";

  let sizeStyle = "";
  switch (size) {
    case "S":
      sizeStyle = "h-5 px-3 text-xs";
      break;
    case "M":
      sizeStyle = "h-11 w-28 text-sm";
      break;
    case "L":
      sizeStyle = "h-11 w-64 text-base font-semibold";
      break;
  }

  return (
    <button
      type="button"
      onClick={onClick}
      className={`${baseStyle} ${typeStyle} ${sizeStyle}`}
    >
      {text}
    </button>
  );
};

export default Button;
