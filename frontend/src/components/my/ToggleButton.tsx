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
    <div className="flex w-32 h-7 bg-yellow-50 rounded-full text-sm font-medium self-center">
      {[option1, option2].map((option) => (
        <button
          key={option}
          onClick={() => onClick(option)}
          className={`flex-1 h-full rounded-full transition-colors self-center ${
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
