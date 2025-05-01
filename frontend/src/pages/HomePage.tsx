import React from "react";
import Button from "../components/common/Button";
import kakaoIcon from "../assets/icons/kakao.png";
import googleIcon from "../assets/icons/google.png";
import bg from "../assets/icons/bg.png";

export default function HomePage() {
  return (
    <div
      className="flex flex-col items-center justify-center min-h-screen px-4 bg-yellow-50"
      style={{
        backgroundImage: `url(${bg})`,
        backgroundPosition: "center",
        backgroundRepeat: "no-repeat",
        backgroundSize: "cover",
      }}
    >
      {/* 내용 */}
      <div className="absolute inset-x-0 bottom-0 flex flex-col items-center text-center px-4 pb-24">
        <div className="flex flex-col items-center space-y-1">
          <h1 className="text-5xl font-extrabold font-onemobile">말자국</h1>
          <p className="text-lg text-gray-700 font-onemobile">
            말하는 순간이 기록이 되는 시간
          </p>
        </div>

        <div className="mt-8 flex flex-col gap-4 w-full max-w-xs">
          <p className="text-base text-gray-600 leading-relaxed">
            간편하게 로그인하고
            <br />
            다양한 서비스를 이용해보세요
          </p>
        </div>

        {/* 소셜 버튼들*/}
        <div className="mt-6 flex flex-col gap-4 w-full max-w-xs">
          <button
            className="
              flex items-center justify-center w-full py-3 
              bg-[#FEE500]          /* 카카오 실제 색상 */
              text-black rounded-full shadow-md 
              transform transition-transform duration-200 ease-out
              hover:scale-105
            "
            onClick={() => console.log("카카오 로그인")}
          >
            <img src={kakaoIcon} alt="Kakao" className="w-6 h-6 mr-2" />
            카카오로 시작하기
          </button>

          <button
            className="flex items-center justify-center w-full py-3 bg-white text-black rounded-full shadow-md transform transition-transform duration-200 ease-out
              hover:scale-105"
            onClick={() => console.log("구글 로그인")}
          >
            <img src={googleIcon} alt="Google" className="w-6 h-6 mr-2" />
            Google로 시작하기
          </button>
        </div>
      </div>
    </div>
  );
}
