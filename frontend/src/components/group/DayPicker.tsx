interface DayPickerProps {
  selectedDays: string[];
  onChange: (days: string[]) => void;
}

const daysOfWeek = [
  "월요일",
  "화요일",
  "수요일",
  "목요일",
  "금요일",
  "토요일",
  "일요일",
];

export function DayPicker({ selectedDays, onChange }: DayPickerProps) {
  const toggleDay = (day: string) => {
    if (selectedDays.includes(day)) {
      onChange(selectedDays.filter((d) => d !== day));
    } else {
      onChange([...selectedDays, day]);
    }
  };

  return (
    <div className="absolute top-full mt-1 w-32 border rounded-md shadow bg-white z-20 p-2 flex flex-col gap-1">
      {daysOfWeek.map((day) => (
        <button
          key={day}
          onClick={() => toggleDay(day)}
          className={`px-2 py-1 text-sm rounded text-center ${
            selectedDays.includes(day)
              ? "bg-darkmint text-white"
              : "hover:bg-mint"
          }`}
        >
          {day}
        </button>
      ))}
    </div>
  );
}
