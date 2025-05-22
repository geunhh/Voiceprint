import React from "react";

interface ProgressBarProps {
  label?: string; // 예: "대화량" 같은 텍스트 (선택)
  progress: number; // 0~100 (%)
}

export default function ProgressBar({ label, progress }: ProgressBarProps) {
  return (
    <div className="w-full flex flex-col gap-2">
      {/* 라벨 (optional) */}
      {label && <span className="text-sm text-gray-500">{label}</span>}

      {/* 진행바 */}
      <div className="w-full h-3 bg-gray-200 rounded-full overflow-hidden">
        <div
          className="h-full bg-yellow-400 transition-all duration-500"
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}
