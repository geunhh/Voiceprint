import "../../index.css";

interface ButtonProps {
  text: string;
  type: "fill" | "line";
  size: "S" | "M" | "L";
  onClick: () => void;
}

const Button = ({ text, type, size, onClick }: ButtonProps) => {
  const baseStyle = "rounded-xl";

  const typeStyle =
    type === "fill"
      ? "bg-yellow-500 text-white"
      : "border-2 border-yellow-500 text-yellow-200";

  let sizeStyle = "";
  switch (size) {
    case "S":
      sizeStyle = "h-5 px-3 text-xs";
      break;
    case "M":
      sizeStyle = "h-11 w-28 text-sm";
      break;
    case "L":
      sizeStyle = "h-11 w-64 text-base";
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
