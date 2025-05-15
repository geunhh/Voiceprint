import { useEffect, useState } from "react";
import { useSelector } from "react-redux";
import axiosInstance from "../../api/axiosInstance";
import profileImageEdit from "../../assets/icons/edit.png";
import ThemaList from "../../components/common/ThemaList";
import TimePicker from "../../components/group/TimePicker";
import ProfileEditModal from "../../components/modal/ProfileEditModal";
import TimeOnOffToggleButton from "../../components/my/TimeOnOffToggleButton";
import { RootState } from "../../store/store";

export default function EditProfilePage() {
  const [modalOpen, setModalOpen] = useState(false);
  const user = useSelector((state: RootState) => state.user);

  const [selectedTime, setSelectedTime] = useState("08:00"); // 사용자 지정 알림 시간으로 수정 예정
  const [showTimePicker, setShowTimePicker] = useState(false); // 알림 시간 선택을 위한 타임피커 표시 여부

  const [isOn, setIsOn] = useState<boolean>(false);

  // 알림 설정 여부 불러오기
  useEffect(() => {
    (async () => {
      try {
        const res = await axiosInstance.get("/api/v1/user/reminder-setting");
        console.log("알림 설정 응답:", res.data.data);
        setIsOn(
          res.data.data.enableAlarms === true ||
            res.data.data.enableAlarms === "true"
        );

        if (res.data.data.alarmTime) {
          const alarmtime = res.data.data.alarmTime.slice(0, 5);
          setSelectedTime(alarmtime);
        }
      } catch (err) {
        console.error("알림 설정 여부 불러오기 오류: ", err);
      }
    })();
  }, []);

  // 알림 여부 설정 토글 버튼
  const handleToggle = async () => {
    const updatedValue = !isOn;
    setIsOn(updatedValue);

    try {
      await axiosInstance.patch("/api/v1/user/reminder-setting", {
        enableAlarms: updatedValue,
      });
    } catch (err) {
      console.error("서버 저장 실패:", err);
      setIsOn(!updatedValue);
    }
  };

  return (
    <div className="p-4">
      {/* 유저 프로필 및 닉네임 */}
      <div className="mt-10 flex flex-col items-center mb-3">
        {/* 유저 프로필 */}
        <div className="relative w-36 h-36">
          <img
            src={user.imageUrl}
            className="rounded-full w-36 h-36 object-cover"
            alt="프로필 이미지"
          />
          <img
            src={profileImageEdit}
            alt="프로필 수정"
            className="absolute bottom-1 right-1 w-10"
            onClick={() => setModalOpen(true)}
          />
        </div>
        {/* 닉네임 */}
        <p className="font-semibold text-2xl text-center mt-3">
          {user.nickname}
        </p>
      </div>

      <div className="flex items-center place-content-between px-2">
        <p className="text-gray-500 font-semibold">알림 설정</p>
        <TimeOnOffToggleButton isOn={isOn} onToggle={handleToggle} />
      </div>
      {isOn && (
        <div className="flex items-center justify-between p-2">
          <p className="text-gray-500 font-semibold">지정 기록 </p>
          <div className="relative">
            <button
              onClick={() => setShowTimePicker((prev) => !prev)}
              className="w-32 text-right text-darkmint font-semibold text-lg mr-1"
            >
              {selectedTime}
            </button>
            {showTimePicker && (
              <TimePicker
                selectedTime={selectedTime}
                onChange={async (time) => {
                  setSelectedTime(time);
                  setShowTimePicker(false);

                  try {
                    await axiosInstance.patch("/api/v1/user/reminder-time", {
                      alarmTime: time,
                    });
                    // console.log("알림 시간 저장 완료:", time);
                  } catch (err) {
                    console.error("알림 시간 저장 실패:", err);
                  }
                }}
              />
            )}
          </div>
        </div>
      )}

      <div className="mt-3 thema-list-padding">
        <ThemaList />
      </div>

      {/* 프로필 수정 모달 */}
      {modalOpen && (
        <ProfileEditModal
          key={user.nickname + user.imageUrl}
          userName={user.nickname}
          userImage={user.imageUrl}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  );
}
