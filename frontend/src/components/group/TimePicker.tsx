interface TimePickerProps {
  selectedTime: string;
  onChange: (time: string) => void;
}

function TimePicker({ selectedTime, onChange }: TimePickerProps) {
  const timeOptions = Array.from({ length: 48 }, (_, index) => {
    const hour = Math.floor(index / 2);
    const minute = index % 2 === 0 ? "00" : "30";
    return `${hour.toString().padStart(2, "0")}:${minute}`;
  });

  return (
    <div className="absolute top-full mt-1 w-32 max-h-60 overflow-y-scroll rounded-md shadow bg-white z-20 border">
      {timeOptions.map((time) => (
        <button
          key={time}
          onClick={() => onChange(time)}
          className={`w-full text-left px-2 py-1 text-sm hover:bg-mint hover:text-white ${
            selectedTime === time ? "bg-darkmint text-white" : ""
          }`}
        >
          {time}
        </button>
      ))}
    </div>
  );
}

export default TimePicker;
