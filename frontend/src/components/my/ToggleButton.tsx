import "../../index.css";

interface ToggleButtonProps {
  option1: string;
  option2: string;
  selected: string;
  onClick: (selected: string) => void;
}

const ToggleButton = ({
  option1,
  option2,
  selected,
  onClick,
}: ToggleButtonProps) => {
  return (
    <div className="flex w-fit p-1 bg-yellow-50 rounded-full text-base font-medium">
      {[option1, option2].map((option) => (
        <button
          key={option}
          onClick={() => onClick(option)}
          className={`px-4 py-2 rounded-full transition-colors ${
            selected === option
              ? "bg-yellow-400 text-yellow-50"
              : "text-yellow-400"
          }`}
        >
          {option}
        </button>
      ))}
    </div>
  );
};

export default ToggleButton;
