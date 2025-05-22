interface TimeOnOffToggleButtonProps {
  isOn: boolean | string;
  onToggle: () => void;
}

function TimeOnOffToggleButton({ isOn, onToggle }: TimeOnOffToggleButtonProps) {
  const toggleState = isOn === true || isOn === "true";

  return (
    <button
      onClick={onToggle}
      className={`w-16 h-8 rounded-full transition-colors duration-300 relative
        ${toggleState ? "bg-yellow-100" : "bg-gray-100"}`}
    >
      <span
        className={`w-8 h-8 rounded-full absolute top-0 left-0 transition-transform duration-300
          flex items-center justify-center text-white text-xs font-medium
          ${toggleState ? "translate-x-8 bg-yellow-400" : "translate-x-0 bg-gray-500"}`}
      >
        {toggleState ? "On" : "Off"}
      </span>
    </button>
  );
}

export default TimeOnOffToggleButton;
