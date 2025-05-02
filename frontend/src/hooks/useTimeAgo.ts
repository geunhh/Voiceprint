import { useEffect, useState } from "react";

function useTimeAgo(date: string | Date) {
    const [timeAgo, setTimeAgo] = useState("")

    useEffect(() => {
       const targetDate = typeof date === "string" ? new Date(date) : date;

       const updateTimeAgo = () => {
        const now = new Date()
        const diff = Math.floor((now.getTime() - targetDate.getTime())/1000)
        
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
       }
       updateTimeAgo()

       const interval = setInterval(updateTimeAgo, 60*1000)
       return () => clearInterval(interval)
    },[date])

    return timeAgo
 }

export default useTimeAgo