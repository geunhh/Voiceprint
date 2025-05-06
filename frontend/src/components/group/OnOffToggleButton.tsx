interface OnOffToggleButtonProps {
  isOn: boolean;
  onToggle: () => void;
}

function OnOffToggleButton({ isOn, onToggle }: OnOffToggleButtonProps) {
  return (
    <button
      onClick={onToggle}
      className={`w-16 h-8 rounded-full transition-colors duration-300 relative
          ${isOn ? "bg-mint" : "bg-gray-100"}`}
    >
      <span
        className={`
            w-8 h-8 rounded-full absolute top-0 left-0
            transition-transform duration-300
            flex items-center justify-center text-white text-xs font-medium
            ${isOn ? "translate-x-full bg-darkmint" : "translate-x-0 bg-gray-500"}
          `}
      >
        {isOn ? "On" : "Off"}
      </span>
    </button>
  );
}

export default OnOffToggleButton;
