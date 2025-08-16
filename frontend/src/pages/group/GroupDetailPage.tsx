import { useCallback, useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import axiosInstance from "../../api/axiosInstance";

import Add from "../../assets/icons/add.png";
import calendarIcon from "../../assets/icons/calendar.png";
import clockIcon from "../../assets/icons/clock.png";
import happyCharacter from "../../assets/icons/happyCharacter.png";
import QuestionCharacter from "../../assets/icons/lovelyCharacter.png";
import robotCharacter from "../../assets/icons/robotCharacter.png";
import settingIcon from "../../assets/icons/setting.png";
import GroupDiaryPreview from "../../components/group/GroupDiaryPreview";
import GroupInviteModal from "../../components/modal/GroupInviteModal";

interface GroupUser {
  id: number;
  nickname: string;
  profileImageUrl: string;
}

interface Group {
  groupId: number;
  name: string;
  description: string | null;
  groupImage: string;
  createdAt: string;
  joinedAt: string;
  enableAlarm: boolean;
  alarmTime: string | null;
  alarmDays: string[];
  groupUserList: GroupUser[];
}

interface GroupDiary {
  diaryId: number;
  title: string;
  content: string;
  createdAt: string;
  groupId: number;
  nickname: string;
  profileUrl: string;
}

export default function GroupDetailPage() {
  const navigate = useNavigate();

  const { groupId } = useParams();
  const [group, setGroup] = useState<Group | null>(null);
  const [loading, setLoading] = useState(true);

  const [groupDiaries, setGroupDiaries] = useState<GroupDiary[]>([]);
  const [nextCursor, setNextCursor] = useState<string | null>(null);
  const [isFetching, setIsFetching] = useState(false);

  const [isAdmin, setIsAdmin] = useState(false);

  const [isInviteModalOpen, setIsInviteModalOpen] = useState(false);
  const [inviteLink, setInviteLink] = useState("");

  // 그룹 다이어리 목록 불러오기
  const fetchGroupDiaries = useCallback(async (cursor?: string) => {
    if (isFetching) return;
    if (!cursor && groupDiaries.length > 0) return;
    if (cursor === null) return;

    setIsFetching(true);
    try {
      const res = await axiosInstance.get(`/api/v1/group/${groupId}/diaries`, {
        params: cursor ? { cursor } : {},
      });

      const { diaries, nextCursor } = res.data.data as {
        diaries: GroupDiary[];
        nextCursor: string | null;
      };

      setGroupDiaries((prev) => {
        const newIds = new Set(prev.map((d) => d.diaryId));
        const filtered = diaries.filter((d) => !newIds.has(d.diaryId));
        return [...prev, ...filtered];
      });

      setNextCursor(nextCursor);
      // console.log("그룹 다이어리 목록 확인", diaries);
    } catch (err) {
      console.error("그룹 다이어리 불러오기 실패", err);
    } finally {
      setIsFetching(false);
    }
  }, []);

  useEffect(() => {
    fetchGroupDiaries();
  }, [fetchGroupDiaries]);

  useEffect(() => {
    let isFirst = true;

    const handleScroll = () => {
      if (
        window.innerHeight + window.scrollY >=
          document.body.offsetHeight - 300 &&
        nextCursor &&
        !isFirst
      ) {
        fetchGroupDiaries(nextCursor);
      }
      isFirst = false;
    };
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, [nextCursor, groupId, fetchGroupDiaries]);

  // 그룹 정보 불러오기
  useEffect(() => {
    const fetchGroupData = async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/group/${groupId}`);
        setGroup(res.data.data);
        // console.log("그룹 데이터 확인: ", res.data.data);

        if (res.data.data.role === "ADMIN") {
          setIsAdmin(true);
        }
      } catch (err) {
        console.error("그룹 데이터 불러오기 실패", err);
      } finally {
        setLoading(false);
      }
    };

    fetchGroupData();
  }, [groupId]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen text-center flex-col">
        <img src={happyCharacter} alt="캐릭터" />
        <p className="p-4 text-gray-500 text-lg font-medium">
          그룹 정보를 불러오는 중입니다.
        </p>
      </div>
    );
  }

  if (!group) return;

  // 초대 링크 생성
  const handleInviteClick = async () => {
    try {
      const res = await axiosInstance.post(
        `/api/v1/group/${group.groupId}/invites`
      );
      const inviteCode = res.data.data.inviteCode;

      // 초대 링크 생성 - 배포용
      // const fullLink = `https://k12b106.p.ssafy.io/group/${group.groupId}/invite/${inviteCode}`;
      // 초대 링크 생성 - 개발용
      // const fullLink = `http://localhost:5173/group/${group.groupId}/invite/${inviteCode}`; // 배포 시 도메인 변경
      const fullLink = `http://localhost:81/group/${group.groupId}/invite/${inviteCode}`; // 배포 시 도메인 변경
      setInviteLink(fullLink);
      setIsInviteModalOpen(true);
    } catch (err) {
      console.error("초대 링크 생성 실패", err);
    }
  };

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

          {/* 방장인 경우 수정 버튼 활성화 */}
          {isAdmin && (
            <img
              src={settingIcon}
              alt="수정"
              className="w-6 h-6 cursor-pointer"
              onClick={() => navigate(`/group/${group.groupId}/edit`)}
            />
          )}
        </div>
        {/* 그룹명 */}
        <div>
          <p className="font-semibold text-2xl">{group.name}</p>
        </div>
      </div>

      {/* 공유 루틴 및 디데이 정보 */}
      {group.enableAlarm && group.alarmTime && (
        <div className="mb-3 w-full">
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
                <span className="font-semibold text-gray-700">
                  {routineText}
                </span>{" "}
                기록해요
              </p>
            </div>
          </div>
        </div>
      )}

      {/* 디데이 */}
      <div className="mb-2 w-full rounded-xl bg-lightmint flex items-center justify-between p-4">
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
      <div className="mb-5">
        {group.groupUserList.length === 1 ? (
          <div className="flex items-center gap-4 bg-yellow-50 p-4 rounded-xl mb-2">
            <img src={happyCharacter} className="w-20 h-auto" alt="캐릭터" />
            <div
              className="flex flex-col cursor-pointer"
              onClick={handleInviteClick}
            >
              <p className="text-yellow-400 font-bold text-lg mb-1">
                그룹 초대하기
              </p>
              <p className="text-gray-500 text-base">
                초대 코드를 통해
                <br />
                일기 메이트를 초대할 수 있어요!
              </p>
            </div>
          </div>
        ) : (
          <>
            <div className="flex mb-2">
              <p className="text-darkmint font-semibold">
                {group.groupUserList.length}명
              </p>
              <p className="font-semibold text-gray-700">의 일기 메이트</p>
            </div>

            <div className="flex gap-3 items-center overflow-x-auto scrollbar-hide w-full">
              {group.groupUserList.map((user) => (
                <div
                  key={user.id}
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
              {/* 초대 버튼 */}
              <img
                src={Add}
                alt="초대하기"
                className="w-16 h-16 shrink-0 cursor-pointer"
                onClick={handleInviteClick}
              />
            </div>
          </>
        )}
      </div>

      {/* 우리들의 발자국 */}
      <div>
        <div className="pb-20">
          {groupDiaries.length === 0 ? (
            <div className="flex flex-col items-center justify-center min-h-[40vh]">
              <img src={robotCharacter} alt="캐릭터" className="h-32" />
              <p className="text-sm text-gray-400 mt-4 text-center">
                공유된 일기가 없어요! <br /> 그룹에 일기를 공유해 보세요
              </p>
            </div>
          ) : (
            <>
              <p className="text-darkmint font-semibold mb-2">
                우리들의 발자국
              </p>
              {groupDiaries.map((diary) => (
                <div key={diary.diaryId} className="mb-3">
                  <GroupDiaryPreview {...diary} />
                </div>
              ))}
              {isFetching && (
                <p className="text-center text-gray-500 mt-4">불러오는 중...</p>
              )}
            </>
          )}
        </div>
      </div>

      {isInviteModalOpen && (
        <GroupInviteModal
          link={inviteLink}
          onClose={() => setIsInviteModalOpen(false)}
        />
      )}
    </div>
  );
}
