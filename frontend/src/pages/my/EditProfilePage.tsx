import { useState } from "react";
import { useSelector } from "react-redux";
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

  const [isOn, setIsOn] = useState(false); // 사용자 알림 여부로 수정 예정

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
        <TimeOnOffToggleButton isOn={isOn} onToggle={() => setIsOn(!isOn)} />
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
                onChange={(time) => {
                  setSelectedTime(time);
                  setShowTimePicker(false);
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
