import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import axiosInstance from "../../api/axiosInstance";

import calendarIcon from "../../assets/icons/calendar.png";
import clockIcon from "../../assets/icons/clock.png";
import QuestionCharacter from "../../assets/icons/lovelyCharacter.png";
import settingIcon from "../../assets/icons/setting.png";
import GroupDiaryPreview from "../../components/group/GroupDiaryPreview";

// 임시데이터
// 그룹에 공유된 일기 정보는 수정 예정
import profile1 from "../../assets/temp/profile1.png";
import profile2 from "../../assets/temp/profile2.png";
import profile3 from "../../assets/temp/profile3.png";

const groupDiaries = [
  {
    groupId: 1,
    diaryId: 101,
    title: "오늘의 개발 회고",
    createdAt: "2025-05-05T15:00:00",
    userName: "민태홍",
    userImage: profile1,
    content:
      "둔산동 할리스에서 공부를 하고고 매운 음식이 먹고 싶어서 마라탕을 먹었다. 유부를 5번 추가하고 꿔바로우랑 같이 맛있게 먹었다! ",
  },
  {
    groupId: 1,
    diaryId: 102,
    title: "대청호 드라이브",
    createdAt: "2025-05-01T15:00:00",
    userName: "김근휘",
    userImage: profile2,
    content:
      "날씨 좋은 날 대청호에 가서 신나게 놀았다. 드라이브도 하고 너무 재미있었따.",
  },
  {
    groupId: 1,
    diaryId: 103,
    title: "오늘 회의 기록",
    createdAt: "2025-04-26T15:00:00",
    userName: "민태홍",
    userImage: profile1,
    content:
      "오늘은 친구들이랑 프로젝트 회의를 끝내고 다같이 배스킨라빈스에 갔다. 내가 제일 좋아하는 슈팅스타, 엄마는 외계인, 레인보우 샤베트를 맛있게 먹었다.",
  },
  {
    groupId: 1,
    diaryId: 104,
    title: "오늘 회의 기록",
    createdAt: "2025-04-20T15:00:00",
    userName: "이지은",
    userImage: profile3,
    content: "드디어 기획 회의가 끝났어요. 야~~~호! 너무너무 신난다.",
  },
];

export default function GroupDetailPage() {
  const navigate = useNavigate();

  const { groupId } = useParams();
  const [group, setGroup] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/group/${groupId}`);
        setGroup(res.data.data);
        console.log(res.data.data);
      } catch (err) {
        console.error("그룹 데이터 불러오기 실패", err);
      } finally {
        setLoading(false);
      }
    };

    fetchGroupData();
  }, [groupId]);

  if (loading || !group) {
    return (
      <p className="p-4 flex justify-center items-center">
        그룹 정보를 불러오는 중입니다...
      </p>
    );
  }

  const date = new Date(group.createdAt);
  const year = date.getFullYear();
  const month = date.getMonth() + 1;
  const day = date.getDate();

  const routineHour = group.alarmTime ? Number(group.alarmTime.slice(0, 2)) : 0;

  const joinedDate = new Date(group.joinedAt);
  const todayDate = new Date();
  const diffTime = todayDate.getTime() - joinedDate.getTime();
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24)) + 1; // 디데이 계산

  // 요일 영어 -> 한글 변환
  const dayMap: Record<string, string> = {
    MONDAY: "월",
    TUESDAY: "화",
    WEDNESDAY: "수",
    THURSDAY: "목",
    FRIDAY: "금",
    SATURDAY: "토",
    SUNDAY: "일",
  };

  const routineDays = (group.alarmDays ?? []).map((day: string) => dayMap[day]);

  // 일기 공유 요일 출력을 위한 로직
  const fullWeek = ["월", "화", "수", "목", "금", "토", "일"];
  const weekday = ["월", "화", "수", "목", "금"];
  const weekend = ["토", "일"];

  let routineText = "";

  const sortedDays = [...routineDays].sort(
    (a, b) => fullWeek.indexOf(a) - fullWeek.indexOf(b)
  );

  if (sortedDays.length === 7) {
    routineText = "매일";
  } else if (
    sortedDays.length === 5 &&
    weekday.every((day) => sortedDays.includes(day))
  ) {
    routineText = "평일마다";
  } else if (
    sortedDays.length === 2 &&
    weekend.every((day) => sortedDays.includes(day))
  ) {
    routineText = "주말마다";
  } else {
    routineText = `${sortedDays.join("・")}`;
  }

  return (
    <div className="mt-5 p-4">
      {/* 그룹 생성 관련 정보 */}
      <div className="mb-3">
        {/* 생성일 및 수정 페이지 이동 아이콘 */}
        <div className="flex justify-between items-center">
          <p className="text-gray-500 text-lg font-semibold">
            {year}.{month}.{day} ~
          </p>
          <img
            src={settingIcon}
            alt="수정"
            className="w-6 h-6 cursor-pointer"
            onClick={() => navigate(`/group/${group.groupId}/edit`)}
          />
        </div>
        {/* 그룹명 */}
        <div>
          <p className="font-semibold text-2xl">{group.name}</p>
        </div>
      </div>

      {/* 공유 루틴 및 디데이 정보 */}
      <div className="mb-3 w-full">
        {/* <p className="text-darkmint font-semibold mb-2">일기 공유 루틴</p> */}

        <div className="flex items-center gap-4">
          {/* 시간 */}
          <div className="flex items-center">
            <img src={clockIcon} alt="시계" className="h-6 w-6 mr-2" />
            <p className="font-bold text-gray-700">
              {routineHour < 12
                ? `오전 ${group.alarmTime.slice(0, 5)}`
                : `오후 ${group.alarmTime.slice(0, 5)}`}
            </p>
          </div>

          {/* 요일 */}
          <div className="flex items-center">
            <img src={calendarIcon} alt="달력" className="h-6 w-6 mr-2" />
            <p className="font-medium text-gray-500">
              <span className="font-semibold text-gray-700">{routineText}</span>{" "}
              기록해요
            </p>
          </div>
        </div>
      </div>

      {/* 디데이 */}
      <div className="mb-3 w-full rounded-xl bg-lightmint flex items-center justify-between p-4">
        {/* 텍스트 */}
        <div className="flex flex-col">
          <p className="text-gray-500 text-base font-semibold mb-1">
            일기 메이트들과 함께 기록한 시간
          </p>
          <p className="text-2xl font-semibold text-darkmint">D+{diffDays}</p>
        </div>

        <img
          src={QuestionCharacter}
          alt="캐릭터"
          className="w-20 h-20 object-contain"
        />
      </div>

      {/* 그룹 메이트 */}
      <div className="mb-3">
        {/* 메이트 인원수 정보 */}
        <div className="flex mb-2">
          <p className="text-darkmint font-semibold">
            {group.groupUserList.length}명
          </p>
          <p className="font-semibold text-gray-700">의 일기 메이트</p>
        </div>
        {/* 메이트 프로필 이미지 */}
        <div className="flex gap-3 items-center overflow-x-auto scrollbar-hide w-full">
          {group.groupUserList.map((user) => (
            <div
              key={user.userId}
              className="flex flex-col items-center gap-2 shrink-0"
            >
              <img
                src={user.profileImageUrl}
                alt="유저 프로필"
                className="w-20 h-20 rounded-full"
              />
              <p className="font-semibold text-gray-500 whitespace-nowrap">
                {user.nickname}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* 우리들의 발자국 */}
      <div>
        <p className="text-darkmint font-semibold mb-2">우리들의 발자국</p>
        <div className="pb-20">
          {groupDiaries.map((diary) => (
            <div className="mb-3">
              <GroupDiaryPreview {...diary} />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}
