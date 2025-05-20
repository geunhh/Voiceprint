import { Typewriter } from "react-simple-typewriter";

import { useEffect } from "react";
import { useNavigate } from "react-router";
import googleIcon from "../assets/icons/google.png";
import kakaoIcon from "../assets/icons/kakao.png";

export default function HomePage() {
  const navigate = useNavigate();
  const token = localStorage.getItem("Authorization");

  // 로그인한 사용자 접근 제한
  useEffect(() => {
    if (token) {
      navigate("/main", { replace: true });
    }
  }, [token, navigate]);

  //  로그인 성공 후 access 토큰이 쿼리로 들어왔을 때 처리
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const accessToken = params.get("access");

    if (accessToken) {
      localStorage.setItem("Authorization", accessToken);

      const redirectPath = localStorage.getItem("redirectAfterLogin");
      if (redirectPath) {
        localStorage.removeItem("redirectAfterLogin");
        navigate(redirectPath, { replace: true });
      } else {
        navigate("/main");
      }

      // URL 정리 (access 파라미터 제거) 후 /main 이동
      window.history.replaceState({}, "", "/");
    }
  }, [navigate]);

  const handleSocialLogin = (provider: "kakao" | "google") => {
    const BASE_URL = import.meta.env.VITE_API_BASE_URL;
    window.location.href = `${BASE_URL}/api/v1/user/${provider}`;
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen px-4 bg-lightyellow">
      {/* 내용 */}
      <div className="flex flex-col text-center w-full">
        <div className="flex flex-col items-center space-y-1">
          <h1 className="text-5xl font-extrabold font-muruk text-customyellow mb-3">
            말자국
          </h1>
          <p className="text-lg text-gray-600 font-bold">
            <Typewriter
              words={["말하는 순간이 기록이 되는 시간"]}
              loop={false}
              cursor
              cursorStyle=""
              typeSpeed={100}
              deleteSpeed={0}
              delaySpeed={100000}
            />
          </p>
        </div>

        <div className="mt-8 flex flex-col gap-4 w-full px-4">
          <p className="text-base text-gray-600 leading-relaxed font-medium text-center">
            간편하게 로그인하고
            <br />
            다양한 서비스를 이용해보세요
          </p>
        </div>

        {/* 소셜 버튼들*/}
        <div className="mt-6 w-full">
          <div className="flex flex-col gap-4 w-full max-w-md mx-auto px-4">
            <button
              onClick={() => handleSocialLogin("kakao")}
              className="
              flex items-center justify-center w-full py-3 
              bg-[#FEE500] text-black rounded-full shadow-md 
              transform transition-transform duration-200 ease-out
              hover:scale-105
            "
            >
              <img src={kakaoIcon} alt="Kakao" className="w-6 h-6 mr-2" />
              카카오로 시작하기
            </button>

            <button
              onClick={() => handleSocialLogin("google")}
              className="
              flex items-center justify-center w-full py-3 
              bg-white text-black rounded-full shadow-md 
              transform transition-transform duration-200 ease-out
              hover:scale-105
            "
            >
              <img src={googleIcon} alt="Google" className="w-6 h-6 mr-2" />
              Google로 시작하기
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}
