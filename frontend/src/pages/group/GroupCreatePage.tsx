import Add from "../../assets/icons/add.png";
import ImageEdit from "../../assets/icons/edit.png";

import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router";
import Button from "../../components/common/Button";
import { DayPicker } from "../../components/group/DayPicker";
import ImageUploader from "../../components/group/ImageUploader";
import OnOffToggleButton from "../../components/group/OnOffToggleButton";
import TimePicker from "../../components/group/TimePicker";
import GroupInviteModal from "../../components/modal/GroupInviteModal";

// 유저 정보
const user = {
  userId: 1,
  userName: "김혜민",
  userImage:
    "https://i.pinimg.com/736x/a7/ca/36/a7ca369a79ff17fb0ae1c13e72a7a8b4.jpg",
  customThemaId: null,
};

// 초대 링크
const inviteLink = "www.voice_print/group/1/invite/1234";

export default function GroupCreatePage() {
  const navigate = useNavigate();
  const [isOn, setIsOn] = useState(false); // 알림 설정 관련 토글버튼 상태
  const [selectedTime, setSelectedTime] = useState("12:00"); // 알림 시간 설정
  const [showTimePicker, setShowTimePicker] = useState(false); // 알림 시간 선택을 위한 타임피커 표시 여부
  const [selectedDays, setSelectedDays] = useState<string[]>([]); // 선택한 알림 요일
  const [showDayPicker, setShowDayPicker] = useState(false); // 알림 요일 선택을 위한 데이피커 표시 여부
  const dayPickerRef = useRef<HTMLDivElement>(null);
  const [groupImageUrl, setGroupImageUrl] = useState<string>(""); // 그룹 이미지 업로드 상태

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
  }, [showDayPicker]); // 데이피커를 닫기 위한 useEffect

  const handleImageChange = (imageDataUrl: string) => {
    setGroupImageUrl(imageDataUrl);
  };

  return (
    <div className="p-4">
      {/* 페이지 안내 및 그룹 이미지 업로드 */}
      <div className="flex items-center place-content-between mt-5">
        <p className="font-bold text-2xl">그룹 만들기</p>
        <ImageUploader
          defaultImage={ImageEdit}
          onImageChange={handleImageChange}
        />
      </div>

      {/* 그룹명 입력 */}
      <div className="mt-2 ">
        <p className="text-darkmint text-lg font-semibold mb-2">그룹명</p>
        <input
          type="text"
          className="border border-gray-300 rounded-lg h-12 w-full p-2 font-normal placeholder:text-gray-400 placeholder:font-normal"
          placeholder="그룹명을 입력해주세요 (최대 20자)"
        />
      </div>

      <hr className="my-4 border-t border-gray-300" />

      {/* 멤버 */}
      <div className="mt-2 ">
        <p className="text-darkmint text-lg font-semibold mb-2">멤버</p>
        <div className="flex gap-3 items-center">
          {/* 로그인한 유저 */}
          <div className="flex flex-col items-center gap-2">
            <img
              src={user.userImage}
              alt="유저 프로필"
              className="w-20 rounded-full"
            />
            <p className="font-semibold text-gray-500">{user.userName}</p>
          </div>
          {/* 초대하기 버튼 */}
          <img
            src={Add}
            alt="초대하기"
            className="w-12 h-12"
            onClick={() => setModalOpen(true)}
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
          text="그룹 만들기 완료"
          type="fill"
          size="L"
          color="mint"
          onClick={() => {
            // 1번 그룹 페이지로 이동 (임시)
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
