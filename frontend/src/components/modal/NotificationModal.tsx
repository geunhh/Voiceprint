import axiosInstance from "../../api/axiosInstance";
import Button from "../common/Button";

interface NotificationModalProps {
  onUpdate: (value: true | false) => void;
}

function NotificationModal({ onUpdate }: NotificationModalProps) {
  const handleReminderSetting = async (enable: boolean) => {
    const requestBody = {
      enableAlarms: enable,
    };

    try {
      console.log("알림 설정 값:", requestBody.enableAlarms);
      const res = await axiosInstance.patch(
        "/api/v1/user/reminder-setting",
        requestBody
      );

      const updatedValue: true | false = enable;
      onUpdate(updatedValue);
      console.log("알림 설정 완료:", res.data);
    } catch (err) {
      console.error("알림 설정 요청 실패:", err);
    }
  };

  return (
    <div className="fixed inset-0 z-50 bg-black/50 flex justify-center items-center text-center">
      <div className="w-4/5 max-w-[320px] rounded-xl bg-white flex flex-col py-6 h-64 relative overflow-y-auto">
        {/* 제목 */}
        <p className="text-xl font-bold text-center mb-6 mt-6">알림 설정</p>

        {/* 안내 */}
        <div className="text-gray-700 mb-7">
          <p>알림을 허용하시겠습니까?</p>
          <p>기본 일기 알림은 20시로 설정됩니다.</p>
        </div>

        {/* 버튼 */}
        <div className="flex px-8 justify-between">
          <Button
            text="거부"
            type="line"
            size="M"
            onClick={() => handleReminderSetting(false)}
          />
          <Button
            text="허용"
            type="fill"
            size="M"
            onClick={() => handleReminderSetting(true)}
          />
        </div>
      </div>
    </div>
  );
}

export default NotificationModal;
