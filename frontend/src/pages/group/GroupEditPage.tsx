import Add from "../../assets/icons/add.png";

import profile1 from "../../assets/temp/profile1.png";
import profile2 from "../../assets/temp/profile2.png";
import profile3 from "../../assets/temp/profile3.png";

import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router";
import Button from "../../components/common/Button";
import { DayPicker } from "../../components/group/DayPicker";
import ImageUploader from "../../components/group/ImageUploader";
import OnOffToggleButton from "../../components/group/OnOffToggleButton";
import TimePicker from "../../components/group/TimePicker";
import GroupInviteModal from "../../components/modal/GroupInviteModal";

const group = {
  groupId: 1,
  groupName: "아이스크림 조아 모임",
  groupImage:
    "https://i.pinimg.com/736x/a4/d2/b9/a4d2b9a45a2083eb4118f4ef7421cc14.jpg",
  groupUsers: [
    {
      userId: 1,
      userName: "민태홍",
      userImage: profile1,
    },
    {
      userId: 2,
      userName: "김근휘",
      userImage: profile2,
    },
    {
      userId: 3,
      userName: "이지은",
      userImage: profile3,
    },
  ],
  routineTime: "12:00",
  routineDays: ["월", "수", "금"],
  isAlertEnabled: false,
};

// 초대 링크
const inviteLink = "www.voice_print/group/1/invite/1234";

export default function GroupEditPage() {
  const navigate = useNavigate();
  const [isOn, setIsOn] = useState(group.isAlertEnabled);
  const [selectedTime, setSelectedTime] = useState(group.routineTime);
  const [showTimePicker, setShowTimePicker] = useState(false);
  const [selectedDays, setSelectedDays] = useState<string[]>(
    group.routineDays.map((day) => day + "요일")
  );
  const [showDayPicker, setShowDayPicker] = useState(false);
  const dayPickerRef = useRef<HTMLDivElement>(null);
  const [groupImageUrl, setGroupImageUrl] = useState<string>(group.groupImage);

  const [modalOpen, setModalOpen] = useState(false); // 초대 모달 표시 여부

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
    const handleClickOutside = (e: MouseEvent) => {
      if (
        dayPickerRef.current &&
        !dayPickerRef.current.contains(e.target as Node)
      ) {
        setShowDayPicker(false);
      }
    };

    if (showDayPicker) {
      document.addEventListener("mousedown", handleClickOutside);
    } else {
      document.removeEventListener("mousedown", handleClickOutside);
    }

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [showDayPicker]);

  const handleImageChange = (imageDataUrl: string) => {
    setGroupImageUrl(imageDataUrl);
  };

  return (
    <div className="p-4">
      {/* 페이지 안내 및 그룹 이미지 업로드 */}
      <div className="flex items-center place-content-between mt-5">
        <p className="font-bold text-2xl">그룹 수정</p>
        <ImageUploader
          defaultImage={group.groupImage}
          onImageChange={handleImageChange}
        />
      </div>

      {/* 그룹명 입력 */}
      <div className="mt-2 ">
        <p className="text-darkmint text-lg font-semibold mb-2">그룹명</p>
        <input
          type="text"
          className="border border-gray-300 rounded-lg h-12 w-full p-2 font-normal placeholder:text-gray-400 placeholder:font-normal"
          placeholder={group.groupName}
        />
      </div>

      <hr className="my-4 border-t border-gray-300" />

      {/* 멤버 */}
      <div className="mt-2">
        <p className="text-darkmint text-lg font-semibold mb-2">멤버</p>
        <div className="flex gap-3 items-center overflow-x-auto scrollbar-hide w-full">
          {group.groupUsers.map((user) => (
            <div
              key={user.userId}
              className="flex flex-col items-center gap-2 shrink-0"
            >
              <img
                src={user.userImage}
                alt="유저 프로필"
                className="w-20 h-20 rounded-full"
              />
              <p className="font-semibold text-gray-500 whitespace-nowrap">
                {user.userName}
              </p>
            </div>
          ))}
          {/* 초대하기 버튼 */}
          <img
            src={Add}
            alt="초대하기"
            className="w-12 h-12 shrink-0"
            onClick={() => setModalOpen(true)}
          />
        </div>
      </div>

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

      <hr className="my-4 border-t border-gray-300" />

      {/* 알림 */}
      <div>
        <p className="text-darkmint text-lg font-semibold mb-2">알림</p>
        <div className="flex items-center place-content-between">
          <p className="text-gray-500">팝업 알림</p>
          <OnOffToggleButton isOn={isOn} onToggle={() => setIsOn(!isOn)} />
        </div>
      </div>

      {/* 저장 버튼 */}
      <div className="flex justify-center mt-10 short-screen-padding">
        <Button
          text="그룹 수정 완료"
          type="fill"
          size="L"
          color="mint"
          onClick={() => {
            navigate("/group/1");
          }}
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
