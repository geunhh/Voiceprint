import {
  Navigate,
  Outlet,
  Route,
  Routes,
  useLocation,
  useNavigate,
} from "react-router-dom";
// import Appbar from "../components/common/Appbar";
import toast, { Toaster } from "react-hot-toast";
import Tabbar from "../components/common/Tabbar";

import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import axiosInstance from "../api/axiosInstance";
import { RootState } from "../store/store";
import { setUser } from "../store/userSlice";

import NotificationItem from "../components/notification/notificationItem";

/* ---------- 더미 페이지 임포트 ---------- */
import HomePage from "../pages/HomePage";
import MainPage from "../pages/MainPage";

import DiaryChatPage from "../pages/diary/DiaryChatPage";
import DiaryDetailPage from "../pages/diary/DiaryDetailPage";
import DiaryEditPage from "../pages/diary/DiaryEditPage";
import DiaryFriendPage from "../pages/diary/DiaryFriendPage";
import DiaryTempPage from "../pages/diary/DiaryTempPage";
import DiaryThemePage from "../pages/diary/DiaryThemePage";
// import DiaryVoicePage from "../pages/diary/DiaryVoicePage";
import AudioRecorder from "../components/audio/AudioRecorder";

import GroupCreatePage from "../pages/group/GroupCreatePage";
import GroupDetailPage from "../pages/group/GroupDetailPage";
import GroupDiaryDetailPage from "../pages/group/GroupDiaryDetailPage";
import GroupEditPage from "../pages/group/GroupEditPage";
import GroupInvitePage from "../pages/group/GroupInvitePage";
import GroupMainPage from "../pages/group/GroupMainPage";

import EditProfilePage from "../pages/my/EditProfilePage";
import MyPage from "../pages/my/MyPage";

import NotificationPage from "../pages/NotificationPage";

import NotFound from "../pages/NotFound";

/* ---------- 중첩 라우트용 래퍼 ---------- */
const DiaryOutlet = () => <Outlet />;
const GroupOutlet = () => <Outlet />;
const MyOutlet = () => <Outlet />;

const Layout = () => {
  const location = useLocation();
  const path = location.pathname;

  const navigate = useNavigate();

  // 홈("/") 경로인지 검사해서 탭바를 보여줄지 결정
  const showTabbar = path !== "/";

  const getCurrentType = (): "Main" | "Temp" | "Create" | "Group" | "My" => {
    if (path.startsWith("/diary/temp")) return "Temp";
    if (path.startsWith("/diary/setting")) return "Create";
    if (path.startsWith("/group")) return "Group";
    if (path.startsWith("/my")) return "My";
    return "Main";
  };

  const currentType = getCurrentType();

  const dispatch = useDispatch();
  const userId = useSelector((state: RootState) => state.user.userId);

  useEffect(() => {
    const token = localStorage.getItem("Authorization");
    if (!token || userId) return;

    const fetchUser = async () => {
      if (location.pathname === "/") return; // 로그인 화면에서는 진행 X
      try {
        const res = await axiosInstance.get("/api/v1/user/profile");

        const { userId, nickname, imageUrl } = res.data.data;
        dispatch(setUser({ userId, nickname, imageUrl }));
      } catch (err) {
        console.error("유저 정보 불러오기 실패:", err);
      }
    };

    fetchUser();
  }, [dispatch, userId]);

  useEffect(() => {
    const controller = new AbortController();

    const connectSSE = async () => {
      if (location.pathname === "/") return; // 로그인 화면에서는 알림 보이지 않도록

      try {
        const token = localStorage.getItem("Authorization");

        const res = await fetch(
          `${import.meta.env.VITE_API_BASE_URL}/api/notifications/subscribe/test`,
          {
            method: "GET",
            headers: {
              Accept: "text/event-stream", // 실시간 알림
              Authorization: `Bearer ${token}`,
            },
            signal: controller.signal, // 이후 연결을 중단할 수 있도록 설정
          }
        );

        if (!res.body) {
          console.error("SSE 연결 실패");
          return;
        }

        const reader = res.body.getReader();
        const decoder = new TextDecoder("utf-8");
        let buffer = "";

        while (true) {
          // 서버에서 실시간으로 알림이 올 경우 읽기
          const { done, value } = await reader.read();
          // done일 경우 연결이 끊어진 것
          if (done) break;

          buffer += decoder.decode(value, { stream: true }); // 서버에서 받은 데이터를 텍스트로 바꾸고 buffer에 누적

          let eventEndIndex;
          while ((eventEndIndex = buffer.indexOf("\n\n")) !== -1) {
            // SSE 메시지는 \n\n 으로 구분
            // data: -> 줄 하나 이상이 모이면 하나의 메시지로 판단
            const rawEvent = buffer.slice(0, eventEndIndex).trim();
            buffer = buffer.slice(eventEndIndex + 2); // 다음 메시지를 위해 buffer 자르기

            const lines = rawEvent.split("\n");
            const dataLine = lines.find((line) => line.startsWith("data:")); // data:로 시작하는 줄을 찾아 파싱 대상으로 설정

            if (dataLine) {
              const jsonStr = dataLine.replace("data:", "").trim();
              try {
                // data: 부분만 잘라  JSON 파싱 후 콘솔 출력
                const parsed = JSON.parse(jsonStr);
                console.log("알림 수신:", parsed);

                toast.custom((t) => (
                  <div
                    className={`w-[85vw] max-w-[370px] min-w-[320px] transition-all duration-300 ${
                      t.visible ? "opacity-100" : "opacity-0"
                    }`}
                  >
                    <NotificationItem
                      type={parsed.type}
                      message={parsed.message}
                      groupId={parsed.metadata?.groupId}
                      diaryId={parsed.metadata?.diaryId}
                      onClick={() => {
                        toast.dismiss(t.id); // 닫고
                        if (
                          parsed.type === "newComment" &&
                          parsed.metadata?.groupId &&
                          parsed.metadata?.diaryId
                        ) {
                          navigate(
                            `/group/${parsed.metadata.groupId}/diary/${parsed.metadata.diaryId}`
                          );
                        }
                      }}
                    />
                  </div>
                ));
              } catch (e) {
                console.error("JSON 파싱 실패:", e);
                console.log("파싱 실패한 원본:", jsonStr);
              }
            }
          }
        }
      } catch (err) {
        console.error("SSE 연결 오류:", err);
      }
    };

    connectSSE();

    return () => controller.abort(); // 컴포넌트가 언마운트될 때 서버와 SSE 연결 해제
  }, []);

  return (
    <div className="w-full min-h-dvh flex justify-center bg-neutral-50 overflow-x-hidden">
      <div className="w-full max-w-[393px] min-h-screen bg-white shadow-lg flex flex-col">
        <Toaster position="top-center" />
        {/* <Appbar /> */}
        <div className="flex-1 flex flex-col">
          <Outlet />
        </div>

        {/* 고정 탭바 */}
        {showTabbar && (
          <div className="fixed bottom-0 w-full max-w-[393px]  mx-auto left-1/2 -translate-x-1/2">
            <Tabbar type={currentType} onClick={() => {}} />
          </div>
        )}
      </div>
    </div>
  );
};

const AppRoutes = () => (
  <Routes>
    <Route element={<Layout />}>
      {/* 홈 / 메인 */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login-success" element={<HomePage />} />
      <Route path="/main" element={<MainPage />} />

      {/* ────────────────── Diary ────────────────── */}
      <Route path="/diary" element={<DiaryOutlet />}>
        <Route path="setting/theme" element={<DiaryThemePage />} />
        <Route path="setting/friend" element={<DiaryFriendPage />} />
        {/* DiaryVoicePage */}
        <Route path="voice" element={<AudioRecorder />} />
        <Route path="chat" element={<DiaryChatPage />} />
        <Route path="temp" element={<DiaryTempPage />} />
        <Route path="temp/edit" element={<DiaryEditPage />} />
        <Route path=":diaryId" element={<DiaryDetailPage />} />
      </Route>

      {/* ────────────────── Group ────────────────── */}
      <Route path="/group" element={<GroupOutlet />}>
        <Route index element={<GroupMainPage />} />
        <Route path="create" element={<GroupCreatePage />} />
        <Route path=":groupId" element={<GroupDetailPage />} />
        <Route path=":groupId/edit" element={<GroupEditPage />} />
        <Route
          path=":groupId/diary/:diaryId"
          element={<GroupDiaryDetailPage />}
        />
        <Route path=":groupId/invite/:inviteId" element={<GroupInvitePage />} />
      </Route>

      {/* ────────────────── My (마이페이지) ────────────────── */}
      <Route path="/my" element={<MyOutlet />}>
        <Route index element={<MyPage />} />
        <Route path="edit" element={<EditProfilePage />} />
      </Route>

      <Route path="/notification" element={<NotificationPage />} />

      {/* 404 */}
      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Route>
  </Routes>
);

export default AppRoutes;
