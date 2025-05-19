import { useEffect, useRef, useState } from "react";
import { useNavigate, useParams } from "react-router";
import axiosInstance from "../../api/axiosInstance";

import Add from "../../assets/icons/add.png";
import Button from "../../components/common/Button";
import { DayPicker } from "../../components/group/DayPicker";
import ImageUploader from "../../components/group/ImageUploader";
import OnOffToggleButton from "../../components/group/OnOffToggleButton";
import TimePicker from "../../components/group/TimePicker";
import GroupInviteModal from "../../components/modal/GroupInviteModal";

interface GroupUser {
  id: number;
  profileImageUrl: string;
  nickname: string;
}

interface GroupData {
  groupId: number;
  name: string;
  description?: string;
  enableAlarm: boolean;
  alarmDays?: string[];
  alarmTime?: string;
  createdAt: string;
  joinedAt: string;
  groupUserList: GroupUser[];
}

// 영어 -> 한글 매핑
const engToKor: Record<string, string> = {
  MONDAY: "월요일",
  TUESDAY: "화요일",
  WEDNESDAY: "수요일",
  THURSDAY: "목요일",
  FRIDAY: "금요일",
  SATURDAY: "토요일",
  SUNDAY: "일요일",
};
const korToEng = Object.fromEntries(
  Object.entries(engToKor).map(([e, k]) => [k, e])
) as Record<string, string>;

export default function GroupEditPage() {
  const navigate = useNavigate();
  const { groupId } = useParams<{ groupId: string }>();

  const [loading, setLoading] = useState(true);
  const [original, setOriginal] = useState<GroupData | null>(null);

  const [name, setName] = useState("");
  const [imageFile, setImageFile] = useState<File | null>(null);
  const [imageUrl, setImageUrl] = useState("");
  const [isOn, setIsOn] = useState(false);
  const [selectedTime, setSelectedTime] = useState("12:00");
  const [selectedDays, setSelectedDays] = useState<string[]>([]);
  const [modalOpen, setModalOpen] = useState(false);

  const [showTimePicker, setShowTimePicker] = useState(false);
  const [showDayPicker, setShowDayPicker] = useState(false);
  const dayPickerRef = useRef<HTMLDivElement>(null);

  const [inviteLink, setInviteLink] = useState("");

  const getDayLabel = (selectedDays: string[]) => {
    const weekdays = ["월요일", "화요일", "수요일", "목요일", "금요일"];
    const weekends = ["토요일", "일요일"];
    const allDays = [...weekdays, ...weekends];

    const sortedSelected = allDays.filter((day) => selectedDays.includes(day));

    const isAllWeekdays =
      weekdays.every((d) => selectedDays.includes(d)) &&
      selectedDays.length === 5;
    const isAllWeekends =
      weekends.every((d) => selectedDays.includes(d)) &&
      selectedDays.length === 2;
    const isAll = selectedDays.length === 7;

    if (isAll) return "매일";
    if (isAllWeekdays) return "평일";
    if (isAllWeekends) return "주말";

    return sortedSelected.map((d) => d.slice(0, 1)).join("・");
  };

  useEffect(() => {
    const onClick = (e: MouseEvent) => {
      if (
        showDayPicker &&
        dayPickerRef.current &&
        !dayPickerRef.current.contains(e.target as Node)
      ) {
        setShowDayPicker(false);
      }
    };
    document.addEventListener("mousedown", onClick);
    return () => document.removeEventListener("mousedown", onClick);
  }, [showDayPicker]);

  // 기존 그룹 정보 불러오기
  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/group/${groupId}`);
        const d = res.data.data;
        setOriginal(d);

        // state 초기화
        setName(d.name);
        setImageUrl(d.groupImage);
        setIsOn(d.enableAlarm);
        setSelectedTime(d.alarmTime?.slice(0, 5) ?? "12:00");
        setSelectedDays((d.alarmDays || []).map((e: string) => engToKor[e]));
        console.log(d);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    })();
  }, [groupId]);

  const handleEdit = async () => {
    if (!original) return;

    const formData = new FormData();
    if (name !== original.name) formData.append("name", name);
    if (imageFile) formData.append("groupImage", imageFile);
    if (isOn !== original.enableAlarm)
      formData.append("enableAlarm", String(isOn));
    if (isOn) {
      formData.append("alarmTime", `${selectedTime}:00`);
      selectedDays.forEach((k) => formData.append("alarmDays", korToEng[k]));
    }

    try {
      await axiosInstance.patch(`/api/v1/group/${groupId}`, formData);
      navigate(`/group/${groupId}`);
    } catch (err) {
      console.error(err);
      alert("수정 실패");
    }
  };

  // 그룹 초대 코드 생성
  const fetchInviteLink = async () => {
    if (!groupId) return;

    try {
      // 초대 코드 생성
      const inviteRes = await axiosInstance.post(
        `/api/v1/group/${groupId}/invites`
      );
      const inviteCode = inviteRes.data.data.inviteCode;
      // console.log("초대 코드 확인: ", inviteCode);

      // 초대 링크 생성 - 배포용
      // const fullLink = `https://k12b106.p.ssafy.io/group/${groupId}/invite/${inviteCode}`;
      // 초대 링크 생성 - 개발용
      const fullLink = `http://localhost:5173/group/${groupId}/invite/${inviteCode}`;
      setInviteLink(fullLink);
      setModalOpen(true);
    } catch (err) {
      console.error("초대 코드 생성 실패", err);
      alert("초대 링크를 생성하는 데 실패했습니다.");
    }
  };

  const onImageChange = (file: File) => {
    setImageFile(file);
    setImageUrl(URL.createObjectURL(file));
  };

  if (loading || !original) {
    return <p className="p-4 text-center">불러오는 중...</p>;
  }

  return (
    <div className="p-4">
      {/* 페이지 안내 및 그룹 이미지 업로드 */}
      <div className="flex items-center place-content-between mt-5">
        <p className="font-bold text-2xl">그룹 수정</p>
        <ImageUploader defaultImage={imageUrl} onImageChange={onImageChange} />
      </div>

      {/* 그룹명 입력 */}
      <div className="mt-2 ">
        <p className="text-darkmint text-lg font-semibold mb-2">그룹명</p>
        <input
          value={name}
          onChange={(e) => setName(e.target.value)}
          className="border border-gray-300 rounded-lg h-12 w-full p-2 font-normal placeholder:text-gray-400 placeholder:font-normal"
        />
      </div>

      <hr className="my-4 border-t border-gray-300" />

      {/* 멤버 */}
      <div className="mt-2">
        <p className="text-darkmint text-lg font-semibold mb-2">멤버</p>
        <div className="flex gap-3 items-center overflow-x-auto scrollbar-hide w-full">
          {original.groupUserList.map((user) => (
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
          {/* 초대하기 버튼 */}
          <img
            src={Add}
            alt="초대하기"
            className="w-12 h-12 shrink-0"
            onClick={fetchInviteLink}
          />
        </div>
      </div>

      <hr className="my-4 border-t border-gray-300" />

      {/* 알림 */}
      <div>
        <p className="text-darkmint text-lg font-semibold mb-2">알림</p>
        <div className="flex items-center place-content-between">
          <p className="text-gray-500">팝업 알림</p>
          <OnOffToggleButton isOn={isOn} onToggle={() => setIsOn(!isOn)} />
        </div>
      </div>

      {/* 알림 설정 여부에 따른 공유 루틴 설정*/}
      {isOn && (
        <>
          <hr className="my-4 border-t border-gray-300" />

          {/* 일기 공유 루틴  */}
          <div className="mt-2 ">
            <p className="text-darkmint text-lg font-semibold mb-2">
              일기 공유 루틴
            </p>
            {/* 일기 공유 시간 */}
            <div className="flex items-center justify-between">
              <p className="text-gray-500">지정 기록 시간</p>
              <div className="relative">
                <button
                  onClick={() => setShowTimePicker((prev) => !prev)}
                  className="w-32 text-right text-darkmint font-semibold"
                >
                  {selectedTime}
                </button>
                {showTimePicker && (
                  <TimePicker
                    selectedTime={selectedTime}
                    onChange={(time) => {
                      setSelectedTime(time);
                      setShowTimePicker(false);
                    }}
                  />
                )}
              </div>
            </div>

            {/* 일기 공유 요일 */}
            <div className="flex items-center justify-between mt-2">
              <p className="text-gray-500">지정 기록 요일</p>
              <div className="relative" ref={dayPickerRef}>
                <button
                  onClick={() => setShowDayPicker((prev) => !prev)}
                  className="w-32 text-right text-darkmint font-semibold"
                >
                  {getDayLabel(selectedDays) || "요일 선택"}
                </button>
                {showDayPicker && (
                  <DayPicker
                    selectedDays={selectedDays}
                    onChange={setSelectedDays}
                  />
                )}
              </div>
            </div>
          </div>
        </>
      )}

      {/* 저장 버튼 */}
      <div className="flex justify-center mt-10 short-screen-padding">
        <Button
          text="그룹 수정 완료"
          type="fill"
          size="L"
          color="mint"
          onClick={handleEdit}
        />
      </div>

      {/* 그룹 초대 모달 */}
      {modalOpen && (
        <GroupInviteModal
          link={inviteLink}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}
