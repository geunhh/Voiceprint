import axios from "axios";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  withCredentials: true,
});

// 요청 인터셉터
axiosInstance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("Authorization");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 응답 인터셉터: accessToken 만료 시 자동 재발급
axiosInstance.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;

    // accessToken 만료 시
    if (
      error.response?.status === 401 &&
      !originalRequest._retry // 무한루프 방지
    ) {
      console.log("reissue 요청");
      originalRequest._retry = true;

      try {
        const res = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/user/reissue`,
          {
            method: "GET",
            credentials: "include", // 쿠키 포함
          }
        );

        if (res.ok) {
          const data = await res.json();
          const newAccessToken = data.accessToken;
          console.log("accessToken 재발급 성공", newAccessToken);
          localStorage.setItem("Authorization", newAccessToken);

          // Authorization 헤더 갱신해서 재요청
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
          return axiosInstance(originalRequest);
        } else if (res.status === 401) {
          // 리프레시 토큰도 만료된 경우
          console.log("리프레시 토큰 만료");
          localStorage.removeItem("Authorization");
          window.location.href = "/"; // home으로 이동
        }
      } catch (e) {
        console.error("토큰 재발급 실패", e);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;
