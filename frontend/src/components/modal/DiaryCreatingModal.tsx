import React from "react";
import spinnerIcon from "../../assets/icons/modal/spinner.gif"; // 로딩용 gif or svg 아이콘 경로 확인
import { useNavigate } from "react-router-dom";
import Button from "../common/Button";

export default function DiaryCreatingModal() {
  const navigate = useNavigate();

  // 확인 버튼 -> 일기 임시 저장 페이지로 이동하겠금
  const handleConfirm = () => {
    navigate("/diary/temp");
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-8 w-[85vw] max-w-sm flex flex-col items-center justify-center space-y-6 text-center">
        <img
          src={spinnerIcon}
          alt="로딩 중"
          className="w-20 h-20 animate-spin"
        />
        <h2 className="text-lg font-semibold text-gray-800">
          일기를 생성 중입니다...
        </h2>
        <p className="text-sm text-gray-500">잠시만 기다려주세요!</p>

        {/* 확인 버튼 */}
        <div className="w-full">
          <Button text="확인" type="fill" size="L" onClick={handleConfirm} />
        </div>
      </div>
    </div>
  );
}
