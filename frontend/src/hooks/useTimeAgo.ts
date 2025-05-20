import { useEffect, useState } from "react";

function useTimeAgo(date: string | Date) {
  const [timeAgo, setTimeAgo] = useState("");

  useEffect(() => {
    // 브라우저 환경에 따라 날짜 보정
    const parseDate = (input: string | Date) => {
      if (typeof input === "string") {
        const trimmed = input.split(".")[0]; // 마이크로초 제거
        const date = new Date(trimmed + "Z"); // UTC로 강제 해석
        return new Date(date.getTime() + 9 * 60 * 60 * 1000); // KST 보정
      }

      return input;
    };

    const targetDate = parseDate(date);

    const updateTimeAgo = () => {
      const now = new Date();
      const diff = Math.floor((now.getTime() - targetDate.getTime()) / 1000);

      if (diff < 60) {
        setTimeAgo("방금 전");
      } else if (diff < 3600) {
        const minutes = Math.floor(diff / 60);
        setTimeAgo(`${minutes}분 전`);
      } else if (diff < 86400) {
        const hours = Math.floor(diff / 3600);
        setTimeAgo(`${hours}시간 전`);
      } else if (diff < 604800) {
        const days = Math.floor(diff / 86400);
        setTimeAgo(`${days}일 전`);
      } else {
        setTimeAgo(targetDate.toLocaleDateString());
      }
    };
    updateTimeAgo();

    const interval = setInterval(updateTimeAgo, 60 * 1000);
    return () => clearInterval(interval);
  }, [date]);

  return timeAgo;
}

export default useTimeAgo;
