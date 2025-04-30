import { Navigate, Outlet, Route, Routes, useLocation } from "react-router-dom";
// import Appbar from "../components/common/Appbar";
import Tabbar from "../components/common/Tabbar";

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

import GroupCreatePage from "../pages/group/GroupCreatePage";
import GroupDetailPage from "../pages/group/GroupDetailPage";
import GroupDiaryDetailPage from "../pages/group/GroupDiaryDetailPage";
import GroupEditPage from "../pages/group/GroupEditPage";
import GroupMainPage from "../pages/group/GroupMainPage";

import EditProfilePage from "../pages/my/EditProfilePage";
import MyDiaryDetailPage from "../pages/my/MyDiaryDetailPage";
import MyPage from "../pages/my/MyPage";

import NotFound from "../pages/NotFound";

/* ---------- 중첩 라우트용 래퍼 ---------- */
const DiaryOutlet = () => <Outlet />;
const GroupOutlet = () => <Outlet />;
const MyOutlet = () => <Outlet />;

const Layout = () => {
  const location = useLocation();
  const path = location.pathname;

  const getCurrentType = (): "Main" | "Temp" | "Create" | "Group" | "My" => {
    if (path.startsWith("/diary/temp")) return "Temp";
    if (path.startsWith("/diary/setting")) return "Create";
    if (path.startsWith("/group")) return "Group";
    if (path.startsWith("/my")) return "My";
    return "Main";
  };

  const currentType = getCurrentType();

  return (
    <div className="w-screen min-h-dvh flex justify-center bg-neutral-50">
      <div className="w-full max-w-[393px] min-h-screen bg-white shadow-lg flex flex-col">
        {/* <Appbar /> */}
        <div className="flex-1 flex flex-col">
          <Outlet />
        </div>

        {/* 고정 탭바 */}
        <div className="fixed bottom-0 w-full max-w-[393px]">
          <Tabbar type={currentType} onClick={() => {}} />
        </div>
      </div>
    </div>
  );
};

const AppRoutes = () => (
  <Routes>
    <Route element={<Layout />}>
      {/* 홈 / 메인 */}
      <Route path="/" element={<HomePage />} />
      <Route path="/main" element={<MainPage />} />

      {/* ────────────────── Diary ────────────────── */}
      <Route path="/diary" element={<DiaryOutlet />}>
        <Route path="setting/theme" element={<DiaryThemePage />} />
        <Route path="setting/friend" element={<DiaryFriendPage />} />
        <Route path="voice" element={<DiaryVoicePage />} />
        <Route path="chat" element={<DiaryChatPage />} />
        <Route path="temp" element={<DiaryTempPage />} />
        <Route path=":diaryId" element={<DiaryDetailPage />} />
        <Route path=":diaryId/edit" element={<DiaryEditPage />} />
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
      </Route>

      {/* ────────────────── My (마이페이지) ────────────────── */}
      <Route path="/my" element={<MyOutlet />}>
        <Route index element={<MyPage />} />
        <Route path="edit" element={<EditProfilePage />} />
        <Route path="diary/:diaryId" element={<MyDiaryDetailPage />} />
      </Route>

      {/* 404 */}
      <Route path="/404" element={<NotFound />} />
      <Route path="*" element={<Navigate to="/404" replace />} />
    </Route>
  </Routes>
);

export default AppRoutes;
