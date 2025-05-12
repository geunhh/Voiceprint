import { Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
// import Appbar from "../components/common/Appbar";
import { Toaster } from "react-hot-toast";
import Tabbar from "../components/common/Tabbar";

import axios from "axios";
import { useEffect } from "react";
import { useDispatch, useSelector } from "react-redux";
import { RootState } from "../store/store";
import { setUser } from "../store/userSlice";

/* ---------- 더미 페이지 임포트 ---------- */
import HomePage from "../pages/HomePage";
import MainPage from "../pages/MainPage";

import DiaryChatPage from "../pages/diary/DiaryChatPage";
import DiaryDetailPage from "../pages/diary/DiaryDetailPage";
import DiaryEditPage from "../pages/diary/DiaryEditPage";
import DiaryFriendPage from "../pages/diary/DiaryFriendPage";
import DiaryTempPage from "../pages/diary/DiaryTempPage";
import DiaryThemePage from "../pages/diary/DiaryThemePage";
import DiaryVoicePage from "../pages/diary/DiaryVoicePage";
import AudioRecorder from "../components/audio/AudioRecorder";

import GroupCreatePage from "../pages/group/GroupCreatePage";
import GroupDetailPage from "../pages/group/GroupDetailPage";
import GroupDiaryDetailPage from "../pages/group/GroupDiaryDetailPage";
import GroupEditPage from "../pages/group/GroupEditPage";
import GroupInvitePage from "../pages/group/GroupInvitePage";
import GroupMainPage from "../pages/group/GroupMainPage";

import EditProfilePage from "../pages/my/EditProfilePage";
import MyPage from "../pages/my/MyPage";

import NotFound from "../pages/NotFound";

/* ---------- 중첩 라우트용 래퍼 ---------- */
const DiaryOutlet = () => <Outlet />;
const GroupOutlet = () => <Outlet />;
const MyOutlet = () => <Outlet />;

const Layout = () => {
  const location = useLocation();
  const path = location.pathname;

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
      try {
        const res = await axios.get(
          `${import.meta.env.VITE_API_BASE_URL}/api/v1/user/profile`,
          {
            headers: {
              Authorization: `Bearer ${token}`,
            },
          }
        );
        const { userId, nickname, imageUrl } = res.data.data;
        dispatch(setUser({ userId, nickname, imageUrl }));
      } catch (err) {
        console.error("유저 정보 불러오기 실패:", err);
      }
    };

    fetchUser();
  }, [dispatch, userId]);

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

      {/* 404 */}
      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Route>
  </Routes>
);

export default AppRoutes;
